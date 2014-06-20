/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Wolschon
 * Copyright 2002 Jan Blok
 * Copyright 2004 Gaganis Giorgos
 * Copyright 2006,2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.packs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackColor;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.util.PackHelper;

/**
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:33:21 AM
 */
public class PacksModel extends AbstractTableModel
{
    private static final long serialVersionUID = 3258128076746733110L;
    private static final transient Logger logger = Logger.getLogger(PacksModel.class.getName());

    protected List<Pack> packs;
    protected List<Pack> allPacks;
    protected List<Pack> hiddenPacks;
    protected List<Pack> packsToInstall;

    private Map<String, Pack> installedPacks;

    protected int[] checkValues; // This is used to represent the status of the checkbox

    private Map<String, Pack> nameToPack; // Map to hold the object name relationship
    private Map<String, Integer> namesToRowNumbers; // Map to hold the object name relationship

    private InstallData idata;
    private Messages messages;
    protected RulesEngine rules;
    protected Variables variables;

    private boolean modifyInstallation;

    public final int PARTIAL_SELECTED = 2;
    public final int SELECTED = 1;
    public final int DESELECTED = 0;
    public final int REQUIRED_SELECTED = -1;
    public final int DEPENDENT_DESELECTED = -2;

    public PacksModel(InstallData idata)
    {
        this.idata = idata;
        this.rules = idata.getRules();
        this.variables = idata.getVariables();
        this.packsToInstall = idata.getSelectedPacks();
        this.messages = idata.getMessages();

        this.modifyInstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        this.installedPacks = loadInstallationInformation(modifyInstallation);

        this.packs = getVisiblePacks();
        this.hiddenPacks = getHiddenPacks();
        this.allPacks = idata.getAvailablePacks();
        this.namesToRowNumbers = getNametoPosMapping(packs);
        this.nameToPack = getNametoPackMapping(allPacks);

        this.packs = setPackProperties(packs, nameToPack);
        this.checkValues = initCheckValues(packs, packsToInstall);

        refreshPacksToInstall();
        updateConditions(true);
        refreshPacksToInstall();
    }


    private List<Pack> getHiddenPacks()
    {
        List<Pack> hiddenPacks = new ArrayList<Pack>();
        for (Pack availablePack : idata.getAvailablePacks())
        {
            if (availablePack.isHidden())
            {
                hiddenPacks.add(availablePack);
            }
        }
        return hiddenPacks;
    }

    public List<Pack> getVisiblePacks()
    {
        List<Pack> visiblePacks = new ArrayList<Pack>();
        for (Pack availablePack : idata.getAvailablePacks())
        {
            if (!availablePack.isHidden())
            {
                visiblePacks.add(availablePack);
            }
        }
        return visiblePacks;
    }

    /**
     * Generate a map from a pack's name to its pack object.
     *
     * @param packs list os pack objects
     * @return map from a pack's name to its pack object.
     */
    private Map<String, Pack> getNametoPackMapping(List<Pack> packs)
    {
        Map <String, Pack> nameToPack = new HashMap<String, Pack>();
        for (Pack pack : packs)
        {
            nameToPack.put(pack.getName(), pack);
        }
        return nameToPack;
    }

    private Map<String, Integer> getNametoPosMapping(List<Pack> packs)
    {
        Map<String, Integer> nameToPos = new HashMap<String, Integer>();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            nameToPos.put(pack.getName(), i);
        }
        return nameToPos;
    }


    /**
     * Ensure that parent packs know which packs are their children.
     * Ensure that packs who have dependants know which packs depend on them
     *
     * @param packs packs visible to the user
     * @param nameToPack mapping from pack names to pack objects
     * @return packs
     */
    private List<Pack> setPackProperties(List<Pack> packs, Map<String, Pack> nameToPack)
    {
        Pack parent;
        for (Pack pack : packs)
        {
            if (pack.hasParent())
            {
                String parentName = pack.getParent();
                parent = nameToPack.get(parentName);
                parent.addChild(pack.getName());
            }

            if (pack.hasDependencies())
            {
                for (String name : pack.getDependencies())
                {
                    parent = nameToPack.get(name);
                    parent.addDependant(pack.getName());
                }
            }
        }
        return packs;
    }


    public Pack getPackAtRow(int row)
    {
        return this.packs.get(row);
    }

    private void removeAlreadyInstalledPacks(List<Pack> selectedpacks)
    {
        List<Pack> removepacks = new ArrayList<Pack>();

        for (Pack selectedpack : selectedpacks)
        {
            if (installedPacks.containsKey(selectedpack.getName()))
            {
                // pack is already installed, remove it
                removepacks.add(selectedpack);
            }
        }
        for (Pack removepack : removepacks)
        {
            selectedpacks.remove(removepack);
        }
    }

    public void updateConditions()
    {
        this.updateConditions(false);
    }

    private void updateConditions(boolean initial)
    {
        boolean changes = true;

        while (changes)
        {
            changes = false;
            // look for packages,
            for (Pack pack : packs)
            {
                int pos = getPos(pack.getName());
                logger.fine("Conditions fulfilled for: " + pack.getName() + "?");
                if (!rules.canInstallPack(pack.getName(), variables))
                {
                    logger.fine("no");
                    if (rules.canInstallPackOptional(pack.getName(), variables))
                    {
                        logger.fine("optional");
                        logger.fine(pack.getName() + " can be installed optionally.");
                        if (initial)
                        {
                            checkValues[pos] = DESELECTED;
                            changes = true;
                            // let the process start from the beginning
                            break;
                        }
                    }
                    else
                    {
                        logger.fine("Pack" + pack.getName() + " cannot be installed");
                        checkValues[pos] = DEPENDENT_DESELECTED;
                        changes = true;
                        // let the process start from the beginning
                        break;
                    }
                }
            }
            refreshPacksToInstall();
        }
    }

    private int[] initCheckValues(List<Pack> packs, List<Pack> packsToInstall)
    {
        int[] checkValues = new int[packs.size()];

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (packsToInstall.contains(pack))
            {
                checkValues[i] = SELECTED;
            }
        }

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (checkValues[i] == DESELECTED)
            {
                List<String> deps = pack.getDependants();
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = DEPENDENT_DESELECTED;
                }
            }

            // for mutual exclusion, uncheck uncompatible packs too
            // (if available in the current installGroup)
            if (checkValues[i] > 0 && pack.getExcludeGroup() != null)
            {
                for (int q = 0; q < packs.size(); q++)
                {
                    if (q != i)
                    {
                        Pack otherpack = packs.get(q);
                        if (pack.getExcludeGroup().equals(otherpack.getExcludeGroup()))
                        {
                            if (checkValues[q] == SELECTED)
                            {
                                checkValues[q] = DESELECTED;
                            }
                        }
                    }
                }
            }
        }

        for (Pack pack : packs)
        {
            if (pack.isRequired())
            {
                checkValues = propRequirement(pack.getName(), checkValues);
            }
        }

        return checkValues;
    }

    private int [] propRequirement(String name, int[] checkValues)
    {

        final int pos = getPos(name);
        checkValues[pos] = REQUIRED_SELECTED;
        List<String> deps = packs.get(pos).getDependencies();
        for (int i = 0; deps != null && i < deps.size(); i++)
        {
            String s = deps.get(i);
            return propRequirement(s, checkValues);
        }
        return checkValues;

    }

    /**
     * Given a map of names and Integer for position and a name it return the position of this name
     * as an int
     *
     * @return position of the name
     */
    private int getPos(String name)
    {
        return namesToRowNumbers.get(name);
    }

    /*
     * @see TableModel#getRowCount()
     */

    @Override
    public int getRowCount()
    {
        return packs.size();
    }

    /*
     * @see TableModel#getColumnCount()
     */

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    /*
     * @see TableModel#getColumnClass(int)
     */

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                return Integer.class;

            default:
                return String.class;
        }
    }

    /*
     * @see TableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if (checkValues[rowIndex] < 0)
        {
            return false;
        }
        else
        {
            return columnIndex == 0;
        }
    }

    /*
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Pack pack = packs.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return checkValues[rowIndex];

            case 1:
                return PackHelper.getPackName(pack, messages);

            case 2:
                return Pack.toByteUnitsString(pack.getSize());

            default:
                return null;
        }
    }

    public void toggleValueAt(int rowIndex)
    {
        if  (checkValues[rowIndex] == SELECTED)
        {
            setValueAt(DESELECTED, rowIndex, 0);
        }
        else
        {
            setValueAt(SELECTED, rowIndex, 0);
        }

    }
    /*
     * @see TableModel#setValueAt(Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex != 0 || !(aValue instanceof Integer))
        {
            return;
        }
        else
        {
            Pack pack = packs.get(rowIndex);

            boolean added;
            if ((Integer) aValue == 1)
            {
                added = true;
                String name = pack.getName();
                if (rules.canInstallPack(name, variables) || rules.canInstallPackOptional(name, variables))
                {
                    if (pack.isRequired())
                    {
                        checkValues[rowIndex] = REQUIRED_SELECTED;
                    }
                    else
                    {
                        checkValues[rowIndex] = SELECTED;
                    }
                }
            }
            else
            {
                added = false;
                checkValues[rowIndex] = DESELECTED;
            }
            updateExcludes(rowIndex);
            updateDeps();

            if (added)
            {
                onSelectionUpdate(rowIndex);
            }
            else
            {
                onDeselectionUpdate(rowIndex);

            }

            if (added)
            {
                // temporarily add pack to packstoinstall
                this.packsToInstall.add(pack);
            }
            else
            {
                // temporarily remove pack from packstoinstall
                this.packsToInstall.remove(pack);
            }
            updateConditions();
            if (added)
            {
                // redo
                this.packsToInstall.remove(pack);
            }
            else
            {

                // redo
                this.packsToInstall.add(pack);
            }
            refreshPacksToInstall();
            if (pack.hasParent())
            {
                updateParent(pack);
            }
            else if (pack.hasChildren())
            {
                updateChildren(pack);
            }
            fireTableDataChanged();
        }
    }

    /**
     * Set the value of the parent pack of the given pack to SELECTED, PARTIAL_SELECT, or DESELECTED.
     * Value of the pack is dependent of its children values.
     *
     * @param childPack
     */
    private void updateParent(Pack childPack)
    {
        String parentName = childPack.getParent();
        Pack parentPack = nameToPack.get(parentName);
        int parentPosition = namesToRowNumbers.get(parentName);

        int childrenSelected = 0;
        for (String childName : parentPack.getChildren())
        {
            int childPosition = namesToRowNumbers.get(childName);
            if (checkValues[childPosition] == SELECTED
                    || checkValues[childPosition] == REQUIRED_SELECTED)
            {
                childrenSelected += 1;
            }
        }

        if (parentPack.getChildren().size() == childrenSelected)
        {
            checkValues[parentPosition] = SELECTED;
        }
        else if (childrenSelected > 0)
        {
            checkValues[parentPosition] = PARTIAL_SELECTED;
        }
        else
        {
            checkValues[parentPosition] = DESELECTED;
        }
    }


    /**
     * Set the value of children packs to the same value as the parent pack.
     *
     * @param parentPack
     */
    private void updateChildren(Pack parentPack)
    {
        String parentName = parentPack.getName();
        int parentPosition = namesToRowNumbers.get(parentName);
        int parentValue = checkValues[parentPosition];

        for (String childName : parentPack.getChildren())
        {
            int childPosition = namesToRowNumbers.get(childName);
            checkValues[childPosition] = parentValue;
        }
    }


    private void selectionUpdate(Pack pack, Map<String, String> packsData)
    {
        for (Map.Entry<String, String> packData : packsData.entrySet())
        {
            int value, packPos;
            String packName = packData.getKey();

            if (packName.startsWith("!"))
            {
                value = 0;
                packPos = getPos(packName.substring(1));
            }
            else
            {
                value = 1;
                packPos = getPos(packName);
            }

            checkValues[packPos] = value;
        }
    }

    protected void onSelectionUpdate(int index)
    {
        Pack pack = packs.get(index);
        Map<String, String> packsData = pack.getOnSelect();
        selectionUpdate(pack, packsData);
    }

    protected void onDeselectionUpdate(int index)
    {
        Pack pack = packs.get(index);
        Map<String, String> packsData = pack.getOnDeselect();
        selectionUpdate(pack, packsData);
    }

    public List<Pack> refreshPacksToInstall()
    {

        packsToInstall.clear();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if ((Math.abs(checkValues[i]) == 1) && (!installedPacks.containsKey(pack.getName())))
            {
                packsToInstall.add(pack);
            }

        }

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);

            if (installedPacks.containsKey(pack.getName()))
            {
                checkValues[i] = -3;
            }
        }
        // add hidden packs
        for (Pack hiddenpack : this.hiddenPacks)
        {
            if (this.rules.canInstallPack(hiddenpack.getName(), variables))
            {
                packsToInstall.add(hiddenpack);
            }
        }

        return packsToInstall;
    }


    /**
     * This function updates the checkboxes after a change by disabling packs that cannot be
     * installed anymore and enabling those that can after the change. This is accomplished by
     * running a search that pinpoints the packs that must be disabled by a non-fullfiled
     * dependency.
     */
    protected void updateDeps()
    {
        int[] statusArray = new int[packs.size()];
        for (int i = 0; i < statusArray.length; i++)
        {
            statusArray[i] = 0;
        }
        dfs(statusArray);
        for (int i = 0; i < statusArray.length; i++)
        {
            if (statusArray[i] == 0 && checkValues[i] < 0)
            {
                checkValues[i] += 2;
            }
            if (statusArray[i] == 1 && checkValues[i] >= 0)
            {
                checkValues[i] = -2;
            }

        }
        // The required ones must propagate their required status to all the ones that they depend on
        for (Pack pack : packs)
        {
            if (pack.isRequired())
            {
                String name = pack.getName();
                if (!(!rules.canInstallPack(name, variables) && rules.canInstallPackOptional(name, variables)))
                {
                    checkValues = propRequirement(name, checkValues);
                }
            }
        }

    }

    /*
     * Sees which packs (if any) should be unchecked and updates checkValues
     */

    protected void updateExcludes(int rowindex)
    {
        int value = checkValues[rowindex];
        Pack pack = packs.get(rowindex);
        if (value > 0 && pack.getExcludeGroup() != null)
        {
            for (int q = 0; q < packs.size(); q++)
            {
                if (rowindex != q)
                {
                    Pack otherpack = packs.get(q);
                    String name1 = otherpack.getExcludeGroup();
                    String name2 = pack.getExcludeGroup();
                    if (name2.equals(name1))
                    {
                        if (checkValues[q] == SELECTED)
                        {
                            checkValues[q] = DESELECTED;
                        }
                    }
                }
            }
        }
    }


    /**
     * We use a modified dfs graph search algorithm as described in: Thomas H. Cormen, Charles
     * Leiserson, Ronald Rivest and Clifford Stein. Introduction to algorithms 2nd Edition
     * 540-549,MIT Press, 2001
     */
    private int dfs(int[] status)
    {
        Map<String, PackColor> colours = new HashMap<String, PackColor>();
        for (int i = 0; i < packs.size(); i++)
        {
            for (Pack pack : packs)
            {
                colours.put(pack.getName(), PackColor.WHITE);
            }
            Pack pack = packs.get(i);
            boolean wipe = false;

            if (dfsVisit(pack, status, wipe, colours) != 0)
            {
                return -1;
            }

        }
        return 0;
    }

    private int dfsVisit(Pack u, int[] status, boolean wipe, Map<String, PackColor> colours)
    {
        colours.put(u.getName(), PackColor.GREY);
        int check = checkValues[getPos(u.getName())];

        if (Math.abs(check) != 1)
        {
            wipe = true;
        }
        List<String> deps = u.getDependants();
        if (deps != null)
        {
            for (String name : deps)
            {
                Pack v = nameToPack.get(name);
                if (wipe)
                {
                    status[getPos(v.getName())] = 1;
                }
                if (colours.get(v.getName()) == PackColor.WHITE)
                {
                    final int result = dfsVisit(v, status, wipe, colours);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        colours.put(u.getName(), PackColor.BLACK);
        return 0;
    }


    /**
     * Get previously installed packs on modifying a pre-installed application
     * @return the installedPacks
     */
    public Map<String, Pack> getInstalledPacks()
    {
        return this.installedPacks;
    }

    /**
     * @return the modifyInstallation
     */
    public boolean isModifyInstallation()
    {
        return this.modifyInstallation;
    }

    private Map<String, Pack> loadInstallationInformation(boolean modifyInstallation)
    {
        Map<String, Pack> installedpacks = new HashMap<String, Pack>();
        if (!modifyInstallation)
        {
            return installedpacks;
        }

        // installation shall be modified
        // load installation information
        ObjectInputStream oin = null;
        try
        {
            FileInputStream fin = new FileInputStream(new File(
                    idata.getInstallPath() + File.separator + InstallData.INSTALLATION_INFORMATION));
            oin = new ObjectInputStream(fin);
            List<Pack> packsinstalled = (List<Pack>) oin.readObject();
            for (Pack installedpack : packsinstalled)
            {
                installedpacks.put(installedpack.getName(), installedpack);
            }
            this.removeAlreadyInstalledPacks(idata.getSelectedPacks());
            logger.fine("Found " + packsinstalled.size() + " installed packs");

            Properties variables = (Properties) oin.readObject();

            for (Object key : variables.keySet())
            {
                idata.setVariable((String) key, (String) variables.get(key));
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (oin != null)
            {
                try { oin.close(); }
                catch (IOException e) {}
            }
        }
        return installedpacks;
    }
    public Map<String, Pack> getNameToPack()
    {
        return nameToPack;
    }

    public Map<Pack, Integer> getPacksToRowNumbers()
    {
        Map<Pack, Integer> packsToRowNumbers = new HashMap<Pack, Integer>();
        for (Map.Entry<String, Integer> entry : namesToRowNumbers.entrySet())
        {
            packsToRowNumbers.put(nameToPack.get(entry.getKey()), entry.getValue());
        }
        return packsToRowNumbers;
    }

    public Map<String, Integer> getNamesToRowNumbers()
    {
        return namesToRowNumbers;
    }

    public int getTotalByteSize(List<Pack> packs)
    {
        Map<Pack, Integer> packToRow = getPacksToRowNumbers();
        int row;
        int bytes = 0;
        for (Pack pack : packs)
        {
            row = packToRow.get(pack);
            if(checkValues[row] == SELECTED
                    || checkValues[row] == REQUIRED_SELECTED
                    || checkValues[row] == PARTIAL_SELECTED)
            {
                bytes += pack.getSize();
            }
        }
        return bytes;
    }
}
