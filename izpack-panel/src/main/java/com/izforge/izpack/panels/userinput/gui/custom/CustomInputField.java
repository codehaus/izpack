package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * The CustomInputField is a JPanel that encapsulates the user defined fields that can be added and removed by a click
 * of a button.
 */
public class CustomInputField extends JPanel implements ActionListener
{
    private GUIInstallData installData;

    private IzPanel parent;

    private List<GUIField> fields;

    private final static String addCommand = "addComponent";

    private final static String removeCommand = "removeCompoent";

    private JPanel controlPanel;

    private CustomInputRows rows;

    public CustomInputField(List<GUIField> fields, IzPanel parent, GUIInstallData installData)
    {
        this.fields = fields;
        this.parent = parent;
        this.installData = installData;
        this.rows = new CustomInputRows(fields);
        this.controlPanel = initializeControlPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 190 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
        gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        
        this.setLayout(gridBagLayout);
        this.addComponents(rows, controlPanel);
    }

    /**
     * Add components to panel
     *
     * @param rows the groups of components that can be dynamically added and removed
     * @param controlPanel buttons that control when components are added or removed
     */
    public void addComponents(JPanel rows, JPanel controlPanel)
    {
        GridBagConstraints rowConstraints = new GridBagConstraints();
        rowConstraints.fill = GridBagConstraints.BOTH;
        rowConstraints.anchor = GridBagConstraints.CENTER;
        rowConstraints.gridx = 0;
        rowConstraints.gridy = 0;

        add(rows, rowConstraints);

        GridBagConstraints controlPanelConstraints = new GridBagConstraints();
        controlPanelConstraints.fill = GridBagConstraints.NONE;
        controlPanelConstraints.anchor = GridBagConstraints.EAST;
        controlPanelConstraints.gridx = 0;
        controlPanelConstraints.gridy = 1;

        add(controlPanel, controlPanelConstraints);
    }

    /**
     * Initialize the control panel
     * The control panel is the row that contains the buttons to add and remove a row.
     * @return
     */
    private JPanel initializeControlPanel()
    {
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));

        JButton addButton = new JButton("add");
        addButton.setActionCommand(addCommand);
        addButton.addActionListener(this);

        JButton removeButton = new JButton("remove");
        removeButton.setActionCommand(removeCommand);
        removeButton.addActionListener(this);


        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        return controlPanel;
    }

    /**
     * Action events for the add and remove buttons.
     *
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        boolean update = false;
        String actionCommand = actionEvent.getActionCommand();

        if (actionCommand.equals(addCommand))
        {
            rows.addRow();
            update = true;
        }
        else if (actionCommand.equals(removeCommand))
        {
            rows.removeRow();
            update = true;
        }

        if (update)
        {
            revalidate();
            repaint();
        }
    }
}

