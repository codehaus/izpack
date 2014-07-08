package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

import java.util.List;

/**
 * Holds data on a column.
 * Add logic here when added mor attributes to the column
 */
public class Column
{
    private final String id;

    private final List<FieldValidator> validators;

    public Column(String id, List<FieldValidator> validators)
    {
        this.id = id;
        this.validators = validators;
    }

    /**
     * Label to represent a column.
     * @return
     */
    public String getId()
    {
        return id;
    }

    /**
     * Validate based on column.
     * @param values
     * @return
     */
    public ValidationStatus validate(String... values)
    {
        for (FieldValidator validator : validators)
        {
            if(!validator.validate(new ValuesProcessingClient(values)))
            {
                return ValidationStatus.failed(validator.getMessage());
            }
        }
        return ValidationStatus.success(values);
    }
}
