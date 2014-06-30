package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;

import java.util.List;

public class CustomField extends Field
{
    private final List<Column> columns;
    private final List<Field> fields;
    /**
     * Constructs an {@code CustomField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public CustomField(CustomFieldConfig config, InstallData installData)
    {
        super(config, installData);
        columns = config.getColumns();
        fields = config.getFields();
    }

    public List<Field> getFields()
    {
        return fields;
    }
}
