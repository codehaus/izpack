package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;

import java.util.List;

/**
 * Interface that ConsoleCustomField and GUICustomField must adhere to.
 */
public interface CustomFieldType
{
    /**
     * Get all the variables from the custom fields.
     * @return
     */
    List<String> getVariables();
}
