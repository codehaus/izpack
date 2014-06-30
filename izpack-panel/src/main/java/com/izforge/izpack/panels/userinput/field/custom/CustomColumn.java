package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.ElementReader;

public class CustomColumn extends ElementReader
{
    private final IXMLElement column;

    public CustomColumn(IXMLElement column, Config config)
    {
        super(config);
        this.column = column;
    }

    /**
     * Get the header text for this column
     *
     * @return
     */
    public String getLabel()
    {
        return getConfig().getText(column);
    }

}
