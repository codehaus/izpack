/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.panels.treepacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Console implementation for the TreePacksPanel.
 * <p/>
 * Based on PacksConsolePanelHelper
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class TreePacksConsolePanel extends AbstractConsolePanel implements ConsolePanel
{
    private final Prompt prompt;

    private static final int SELECTED = 1;
    private static final int DESELECTED = 0;

    private static final String REQUIRED = "required";
    private static final String DONE = "Done!";

    private static final String CONFIRM = "TreePacksPanel.confirm";
    private static final String NUMBER = "TreePacksPanel.no.number";
    private static final String PROMPT = "TreePacksPanel.prompt";
    private static final String INVALID = "TreePacksPanel.invalid";
    private static final String REQUIRED_SPACE = "TreePacksPanel.space.required";
    
    /**
     * Constructs a {@code TreePacksConsolePanel}.
     *
     * @param panel  the parent panel/view. May be {@code null}
     * @param prompt the prompt
     */
    public TreePacksConsolePanel(PanelView<ConsolePanel> panel, Prompt prompt)
    {
        super(panel);
        this.prompt = prompt;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    public boolean run(InstallData installData, Console console)
    {
        List<Pack> selectedPacks;
        HashMap<String, List<String>> treeData = createTreeData(installData);

        selectedPacks = selectPacks(treeData, installData);
        out(Type.INFORMATION, DONE);

        installData.setSelectedPacks(selectedPacks);

        if (selectedPacks.size() == 0)
        {
            out(Type.WARNING, "You have not selected any packs!");
            out(Type.WARNING, "Are you sure you want to continue?");
        }

        return promptEndPanel(installData, console);
    }

    /**
     * Get information on the children each parent pack has.
     *
     * @param installData
     * @return Map that contains information on the parent pack and its children
     */
    private HashMap<String, List<String>> createTreeData(InstallData installData)
    {
        HashMap<String, List<String>> treeData = new HashMap<String, List<String>>();

        for (Pack pack : getAvailableShowablePacks(installData))
        {
            if (pack.getParent() != null)
            {
                List<String> kids;
                if (treeData.containsKey(pack.getParent()))
                {
                    kids = treeData.get(pack.getParent());
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.getName());
                treeData.put(pack.getParent(), kids);
            }
        }

        return treeData;
    }

    private void out(Type type, String message)
    {
        prompt.message(type, message);
    }

    /**
     * Helper method to ask/check if the pack can/needs to be installed
     * If top-level pack, square brackets will be placed in between
     * the pack id.
     *
     * It asks the user if it wants to install the pack if:
     * 1. the pack is not required
     * 2. the pack has no condition string
     *
     * @param treeData          - Map that contains information on the parent pack and its children
     * @param installData       - Database of izpack
     */
    private List<Pack> selectPacks(final Map<String, List<String>> treeData, final InstallData installData)
    {
        java.io.Console console = System.console();

        List<Pack> packs = new ArrayList<Pack>();
        List<Pack> selectedPacks = new LinkedList<Pack>();

        for(Pack pack : getAvailableShowablePacks(installData))
        {
            packs.add(pack);
        }

        int mapChoiceToSelection[] = new int[packs.size()];
        int selected[] = new int[packs.size()];
        int choiceCount = 0;
        Map<String, Integer> idPos = new HashMap<String, Integer>();

        /**
         * Generate mapping from choice to selection.
         * Figure out which packs are already selected.
         */
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            Boolean conditionSatisfied = checkCondition(installData, pack);
            idPos.put(pack.getName(), i);

            if (pack.hasCondition() && conditionSatisfied)
            {
                selected[i] = SELECTED;
            }
            else if (pack.isPreselected())
            {
                selected[i] = SELECTED;
            }

            if (!pack.isHidden())
            {
                mapChoiceToSelection[choiceCount] = i;
                choiceCount++;
            }
        }

        int packNum = printPackMenu(installData, packs, selected, mapChoiceToSelection);

        //Allow user to select/deselect packs
        while (true)
        {
            int choice = -1;

            try
            {
                choice = (Integer.parseInt(console.readLine())) -1;
            }
            catch(NumberFormatException e)
            {
                out(Type.WARNING, installData.getMessages().get(NUMBER));
                continue;
            }

            if (choice < packNum && choice >= 0)
            {
                choice = mapChoiceToSelection[choice];
                if (packs.get(choice).isRequired())
                {
                    out(Type.WARNING, installData.getMessages().get(INVALID));
                }
                else
                {
                    //Toggle between selected and unselected
                    selected[choice] = (selected[choice] + 1) % 2;

                    //Check if what the user selected is a parent
                    if (treeData.containsKey(packs.get(choice).getName()))
                    {
                        // If parent selected select/deselect all its children
                        for (String child : treeData.get(packs.get(choice).getName()))
                        {
                            selected[idPos.get(child)] = selected[choice];
                        }
                    }

                    // Check if this pack is a child of a parent, and that the parent is not required
                    else if(packs.get(choice).getParent() != null &&
                            !packs.get(idPos.get(packs.get(choice).getParent())).isRequired())
                    {
                        //Select parent
                        selected[idPos.get(packs.get(choice).getParent())] = SELECTED;

                        // If at least one child is unselected ensure parent is unselected
                        for (String child : treeData.get(packs.get(choice).getParent()))
                        {
                            if(selected[idPos.get(child)] == DESELECTED)
                            {
                                selected[idPos.get(packs.get(choice).getParent())] = DESELECTED;
                            }
                        }
                    }

                    printPackMenu(installData, packs, selected, mapChoiceToSelection);
                }
            }
            else if (choice == -1)
            {
                break;
            }
            else
            {
                out(Type.WARNING, installData.getMessages().get(INVALID));
            }

        }

        for (int i = 0; i < selected.length; i++)
        {
            if (selected[i] == SELECTED)
            {
                selectedPacks.add(packs.get(i));
            }
        }
        return selectedPacks;
    }

    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData - the data of izpack
     * @param pack        - the pack whose condition needs to be checked·
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(InstallData installData, Pack pack)
    {
        if (pack.hasCondition())
        {
            return installData.getRules().isConditionTrue(pack.getCondition());
        }
        else
        {
            return null;
        }
    }

    private List<Pack> getAvailableShowablePacks(InstallData installData)
    {
        List<Pack> packs = new ArrayList<Pack>();
        List<Pack> availablePacks = installData.getAvailablePacks();

        for (Pack pack : availablePacks)
        {
            if (!pack.isHidden())
            {
                packs.add(pack);
            }
        }

        return packs;
    }

    /**
     * Method will print the pack selection state onto the console.
     *
     * @param installData the data of izpack
     * @param packs list of available packs
     * @param selected  holds selection status for available packs
     * @param choiceMap holds mapping of visible packs to their index in selected
     */
    public int printPackMenu(InstallData installData, List<Pack> packs, int[] selected, int[] choiceMap)
    {
        long totalSize = 0;
        int packnum = 1;

        for (Pack pack : packs)
        {
            if (pack.isHidden())
            {
                continue;
            }
            else if (pack.isRequired())
            {
                System.out.printf("%-4d [%s] %-15s [%s] (%-4s)\n", packnum, (selected[choiceMap[packnum - 1]] == SELECTED ? "x" : " "),
                        installData.getMessages().get(REQUIRED), installData.getMessages().get(pack.getName()), pack.toByteUnitsString(pack.getSize()));
            }
            else
            {
                System.out.printf("%-4d [%s] %-15s [%s] (%-4s)\n", packnum, (selected[choiceMap[packnum - 1]] == SELECTED ? "x" : " "),
                        "", installData.getMessages().get(pack.getName()), pack.toByteUnitsString(pack.getSize()));
            }

            if (selected[choiceMap[packnum-1]] == SELECTED)
            {
                totalSize += pack.getSize();
            }
            packnum++;
        }

        System.out.println(installData.getMessages().get(REQUIRED_SPACE) + " " + Pack.toByteUnitsString(totalSize));
        System.out.println(installData.getMessages().get(CONFIRM));
        System.out.println(installData.getMessages().get(PROMPT));
        return packnum-1;
    }
}
