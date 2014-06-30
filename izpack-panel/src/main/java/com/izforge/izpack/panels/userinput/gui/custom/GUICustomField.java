package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.util.List;

public class GUICustomField extends GUIField
{
    public GUICustomField(CustomField customField, List<GUIField> fields,  GUIInstallData installData, IzPanel parent)
    {
        super(customField);
        addComponent(new JTextField("Test Me"), new TwoColumnConstraints(TwoColumnConstraints.BOTH));
        init();
    }

    public void init()
    {

    }
}
