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
    protected List<Pack> hiddenPacks;
    protected List<Pack> packsToInstall;

    private Map<String, Pack> installedpacks;

    // This is used to represent the status of the checkbox
    protected int[] checkValues;

    Map<String, Pack> nameToPack; // Map to hold the object name relationship
    Map<String, Integer> nameToPos; // Map to hold the object name relationship

    private InstallData idata;
    private Messages messages;
    protected RulesEngine rules; // reference to the RulesEngine for validating conditions
    protected Variables variables; // reference to the current variables, needed for condition validation

    private boolean modifyInstallation;

    public PacksModel(InstallData idata)
    {
        this.idata = idata;
        this.rules = idata.getRules();
        this.variables = idata.getVariables();
        this.packsToInstall = idata.getSelectedPacks();
        this.messages = idata.getMessages();

        this.modifyInstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        this.installedpacks = loadInstallationInformation(modifyInstallation);

        this.packs = getVisiblePacks();
        this.hiddenPacks = getHiddenPacks();
        this.nameToPos = getNametoPosMapping(packs);
        this.nameToPack = getNametoPackMapping(packs);

        setPackDependencies(packs, nameToPack);
        this.checkValues = initCheckValues(packs, packsToInstall);

        refreshPacksToInstall();
        this.updateConditions(true);
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

    private List<Pack> getVisiblePacks()
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
     * Creates the reverse dependency graph
     */
    private List<Pack> setPackDependencies(List<Pack> packs, Map<String, Pack> nameToPack)
    {
        for (Pack pack : packs)
        {
            List<String> deps = pack.getDependencies();
            for (int j = 0; deps != null && j < deps.size(); j++)
            {
                String name = deps.get(j);
                Pack parent = nameToPack.get(name);
                parent.addDependant(pack.getName());
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
            if (installedpacks.containsKey(selectedpack.getName()))
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
                            checkValues[pos] = 0;
                            changes = true;
                            // let the process start from the beginning
                            break;
                        }
                    }
                    else
                    {
                        logger.fine("Pack" + pack.getName() + " cannot be installed");
                        checkValues[pos] = -2;
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
                checkValues[i] = 1;
            }
        }

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (checkValues[i] == 0)
            {
                List<String> deps = pack.getDependants();
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = -2;
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
                            if (checkValues[q] == 1)
                            {
                                checkValues[q] = 0;
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
        checkValues[pos] = -1;
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
        return nameToPos.get(name);
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

    /*
     * @see TableModel#setValueAt(Object, int, int)
     */

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            if (aValue instanceof Integer)
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
                            checkValues[rowIndex] = -1;
                        }
                        else
                        {
                            checkValues[rowIndex] = 1;
                        }
                    }
                }
                else
                {
                    added = false;
                    checkValues[rowIndex] = 0;
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
                fireTableDataChanged();
            }
        }
    }

    private void selectionUpdate(Pack pack, Map<String, String> packsData)
    {
        for (Map.Entry<String, String> packData : packsData.entrySet())
        {
            int value, packPos;
            String packName = packData.getKey();
            String packCondition = pack.getCondition();

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

    protected void refreshPacksToInstall()
    {

        packsToInstall.clear();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if ((Math.abs(checkValues[i]) == 1) && (!installedpacks.containsKey(pack.getName())))
            {
                packsToInstall.add(pack);
            }

        }

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);

            if (installedpacks.containsKey(pack.getName()))
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
                        if (checkValues[q] == 1)
                        {
                            checkValues[q] = 0;
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
     * @return the installedpacks
     */
    public Map<String, Pack> getInstalledpacks()
    {
        return this.installedpacks;
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
}
