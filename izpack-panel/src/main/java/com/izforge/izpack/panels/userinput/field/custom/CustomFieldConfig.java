package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldConfig;

import java.util.List;

public interface CustomFieldConfig extends FieldConfig
{
    /**
     * @return list of columns
     */
    List<Column> getColumns();

    /**
     * @return list of fields
     */
    List<Field> getFields();
}
