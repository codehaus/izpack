package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    private int numberOfRows = 0;

    private final int numberOfColumns;

    private List<GUIField> guiFields;

    public CustomInputRows(FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        super();
        this.spec = spec;
        this.userInputPanelSpec = userInputPanelSpec;
        this.createField = createField;
        this.numberOfColumns = getNumberOfColumns(userInputPanelSpec, spec);
        this.guiFields = new ArrayList<GUIField>();
        super.setLayout(new GridLayout(0, numberOfColumns));
        addRow();
    }

    /**
     * Add an additional row of fields defined by the user.
     */
    public void addRow()
    {
        numberOfRows++;

        for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
        {

            GUIField guiField = createField.execute(field);
            //TODO: Check for the always display option to show as disabled
            //TODO: Check for condition
            //if (guiField.getField().isConditionTrue())

            guiField.setDisplayed(true);
            guiField.setVariable(guiField.getVariable() + "." + numberOfRows);
            guiFields.add(guiField);

            for (Component component : guiField.getComponents())
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

    /**
     * Remove last added row of fields defined by the user.
     */
    public void removeRow()
    {
        if (numberOfRows <= 1)
        {
            return;
        }

        for (int colNumber = numberOfColumns; colNumber > 0; colNumber--)
        {
            this.remove(this.getComponentCount() - colNumber);
        }
        guiFields.remove(guiFields.size() -1);

        numberOfRows--;
        revalidate();
        repaint();
    }

    /**
     * Validate and update installData
     * @param prompt
     * @return
     */
    public boolean updateField(Prompt prompt)
    {
        boolean valid = true;
        for (GUIField guiField : guiFields)
        {
            if (guiField.isDisplayed())
            {
                System.out.println("VARAIBLE IS: " + guiField.getVariable());
                if (!guiField.updateField(prompt))
                {
                    valid = false;
                }

            }
        }
        return valid;
    }

    /**
     * Find the number of columns required for this custom field
     *
     * @param userInputPanelSpec
     * @param spec
     * @return
     */
    public int getNumberOfColumns(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
       List<Field> fields = userInputPanelSpec.createFields(spec);
       for (Field field : fields)
       {
           if (field instanceof CustomField)
           {
               return ((CustomField) field).getFields().size();
           }
       }
       return 0;
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
}
