package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
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
    private final List<Field> fields;

    private final FieldCommand createField;

    private int numberOfRows = 1;

    private final int numberOfColumns;

    public CustomInputRows(FieldCommand createField, List<Field> fields)
    {
        super();
        this.fields = fields;
        this.createField = createField;
        this.numberOfColumns = fields.size();
        super.setLayout(new GridLayout(0, fields.size()));
        addRow();
    }

    /**
     * Add an additional row of fields defined by the user.
     */
    public void addRow()
    {
        for (Field field : fields)
        {
            GUIField guiField = createField.execute(field);
            for( Component component : guiField.getComponents())
            {
                JComponent jComponent = component.getComponent();
                if (!(jComponent instanceof JLabel))
                {
                    this.add(jComponent);
                }
            }
        }

        numberOfRows++;
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

        numberOfRows--;
        revalidate();
        repaint();
    }
}
