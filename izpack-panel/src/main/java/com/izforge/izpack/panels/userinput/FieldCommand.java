package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.gui.GUIField;

/**
 * Wrapper to allow passing the create method from GUIFieldFactory.
 * This easily generate new fields, without cloning.
 * Cloning is not available with JComponent classes, and using serialization to clone can to unexpected error.
 */
public interface FieldCommand
{
    public GUIField execute(Field field);
}
