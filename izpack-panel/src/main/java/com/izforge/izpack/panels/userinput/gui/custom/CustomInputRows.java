package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.Column;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPanel that contains the possible rows of fields defined by the user.
 *
 * GUICustomField
 * ===============================|
 * |CustomInputRows               |
 * |------------------------------|
 * |          Row 1               |
 * |          Row 2               |
 * |------------------------------|
 * |ControlButtons                |
 * |------------------------------|
 * |            |  Add  | Remove  |
 * |==============================|
 */

public class CustomInputRows extends JPanel
{
    private final UserInputPanelSpec userInputPanelSpec;

    private final IXMLElement spec;

    private final FieldCommand createField;

    private static int numberOfRows = 0;

    private final int numberOfColumns;

    private Map<Integer, List<GUIField>> guiFields;

    private final CustomField customInfoField;

    private final int maxRow;

    private final int minRow;

    private final List<String> labels;

    private final List<String> variables;

    private GUIInstallData installData;

    public CustomInputRows(FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec, GUIInstallData installData)
    {
        super();
        this.spec = spec;
        this.userInputPanelSpec = userInputPanelSpec;
        this.createField = createField;
        this.customInfoField = createCustomField(userInputPanelSpec, spec);
        this.numberOfColumns = getNumberOfColumns(customInfoField);
        this.maxRow = getMaxRow(customInfoField);
        this.minRow = getMinRow(customInfoField);
        this.labels = getLabels(customInfoField);
        this.variables = getVariables(customInfoField);
        this.guiFields = new HashMap<Integer, List<GUIField>>();
        this.installData = installData;

        super.setLayout(new GridLayout(0, numberOfColumns));
        addRow(true);
    }

    /**
     * Add an additional row of fields defined by the user.
     */
    public void addRow(boolean first)
    {
        numberOfRows++;
        List<GUIField> fields;

        if (guiFields.size() >= numberOfRows)
        {
            fields = guiFields.get(numberOfRows);
        }
        else
        {
            fields = new ArrayList<GUIField>();
            for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
            {

                GUIField guiField = createField.createGuiField(field);
                guiField.setVariable(guiField.getVariable() + "." + numberOfRows);
                fields.add(guiField);
            }
            guiFields.put(numberOfRows, fields);
        }

        for (GUIField field : fields)
        {
            //TODO: Check for the always display option to show as disabled
            //TODO: Check for condition
            //if (guiField.getField().isConditionTrue())
            field.setDisplayed(true);
            for (Component component : field.getComponents())
            {
                JComponent jComponent = component.getComponent();
                Object jConstraints = component.getConstraints();

                if (!(jComponent instanceof JLabel))
                {
                    this.add(jComponent, jConstraints);
                }
            }
        }

        revalidate();
        repaint();
    }
    public void addRow()
    {
        addRow(false);
    }


    /**
     * Remove last added row of fields defined by the user.
     */
    public void removeRow()
    {
        for (int colNumber = numberOfColumns; colNumber > 0; colNumber--)
        {
            this.remove(this.getComponentCount() - colNumber);
        }

        guiFields.remove(numberOfRows);
        numberOfRows--;

        revalidate();
        repaint();
    }

    public boolean atMax()
    {
        return (numberOfRows < maxRow);
    }

    public boolean atMin()
    {
        return (numberOfRows > minRow);
    }

    /**
     * Validate and update installData
     * @param prompt
     * @return
     */
    public boolean updateField(Prompt prompt)
    {
        boolean valid = true;
        installData.setVariable(customInfoField.getVariable(), numberOfRows+"");
        for (int i = 1; i <= numberOfRows; i++)
        {
            for(GUIField guiField : guiFields.get(i))
            {
                if (guiField.isDisplayed())
                {
                    if (!guiField.updateField(prompt))
                    {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Retrive a list of labels
     * @param customInfoField
     * @return
     */
    public List<String> getLabels(CustomField customInfoField)
    {
        List<String> labels = new ArrayList<String>();
        for (Field field : customInfoField.getFields())
        {
            GUIField guiField = createField.createGuiField(field);
            labels.add(guiField.getSummaryKey());
        }
        return  labels;
    }
    public List<String> getLabels()
    {
        return this.labels;
    }

    /**
     * Retrive a list of variables
     * @param customInfoField
     * @return
     */
    public List<String> getVariables(CustomField customInfoField)
    {
        List<String> variables = new ArrayList<String>();
        for (Field field : customInfoField.getFields())
        {
            GUIField guiField = createField.createGuiField(field);
            variables.add(guiField.getVariable());
        }
        return  variables;
    }
    public List<String> getVariables()
    {
        List<String> countedVariables = new ArrayList<String>();
        for (int i=1; i<= numberOfRows; i++)
        {

        }
        for (int i = 1; i <= numberOfRows; i++)
        {
            for(GUIField guiField : guiFields.get(i))
            {
                if (guiField.isDisplayed())
                {
                   countedVariables.add(guiField.getVariable());
                }
            }
        }
        return countedVariables;
    }

    /**
     * Find the number of columns required for this custom field
     *
     * @param customInfoField
     * @return
     */
    public int getNumberOfColumns(CustomField customInfoField)
    {
       return customInfoField.getFields().size();
    }

    /**
     * Generate a new custom field.
     * @return
     */
    private CustomField createCustomField(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        List<Field> fields = userInputPanelSpec.createFields(spec);
        for (Field field : fields)
        {
            if (field instanceof CustomField)
            {
                return (CustomField) field;
            }
        }
        return  null;
    }

    public JPanel getHeader()
    {
        JPanel header = new JPanel(new GridLayout(1, numberOfColumns));

        List<Column> columns = createCustomField(userInputPanelSpec, spec).getColumns();
        for (Column column : columns)
        {
            JLabel label = new JLabel(column.getId());
            header.add(label);
        }

        return header;
    }

    /**
     * Get the minimum amount of rows that must be displayed
     * @param customFieldInfo
     * @return
     */
    private int getMinRow(CustomField customFieldInfo)
    {
        return customFieldInfo.getMinRow();
    }

    /**
     * Get the maximum amount of rows that can be displayed
     * @param customFieldInfo
     * @return
     */
    private int getMaxRow(CustomField customFieldInfo)
    {
        return customFieldInfo.getMaxRow();
    }
}
