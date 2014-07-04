package com.izforge.izpack.panels.userinput.console.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.util.Console;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ConsoleCustomField extends ConsoleField
{
    private final UserInputPanelSpec userInputPanelSpec;

    private final IXMLElement spec;

    private final FieldCommand createField;

    private int numberOfRows = 0;

    private final CustomField customField;

    /**
     * Constructs a {@code ConsoleField}.
     *
     * @param customField   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleCustomField(CustomField customField, Console console, Prompt prompt,
                              FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        super(customField, console, prompt);
        this.spec = spec;
        this.userInputPanelSpec = userInputPanelSpec;
        this.createField = createField;
        this.customField = customField;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public CustomField getField()
    {
        return (CustomField) super.getField();
    }

    public boolean addRow()
    {
        numberOfRows++;
        List<ConsoleField> fields = new ArrayList<ConsoleField>();
        
        for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
        {
            field.setVariable(field.getVariable() + "." + numberOfRows);
            ConsoleField consoleField = createField.createConsoleField(field);
            fields.add(consoleField);
        }

        for(ConsoleField field: fields)
        {
            field.setDisplayed(true);
            field.display();
        }
        int value = -1;
        while(value == -1)
        {
            value = prompt("Enter 1 to add another module, 2 to continue", 1, 2, -1, -1);
        }

        if(value == 1)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean display()
    {
        numberOfRows = 0;

        while(addRow())
        {
            //Keep adding rows until the user is done or max limit is reached
        }
        customField.setValue(numberOfRows+"");
        return true;
    }

    /**
     * Generate a new custom field.
     * @return
     */
    private CustomField createCustomField(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        List<Field> fields = userInputPanelSpec.createFields(spec);
        for (Field field : fields)
        {
            if (field instanceof CustomField)
            {
                return (CustomField) field;
            }
        }
        return  null;
    }
}
