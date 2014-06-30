package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * JPanel that contains the possible rows of fields defined by the user,
 * along with control buttons to add and remove rows.
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
public class GUICustomField extends GUIField
{
    public GUICustomField(CustomField customField,FieldCommand createField, List<Field> fields,  GUIInstallData installData, IzPanel parent)
    {
        super(customField);
        addComponent(new CustomInputField(createField, fields, parent, installData), new TwoColumnConstraints(TwoColumnConstraints.BOTH));
    }
}
