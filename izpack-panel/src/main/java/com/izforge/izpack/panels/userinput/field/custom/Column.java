package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;

import java.util.List;

public class Column
{
    private final String id;
    public Column(List<IXMLElement> field, String variable, String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
}
