package com.izforge.izpack.panels.userinput.console.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.custom.Column;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.GUIField;
import com.izforge.izpack.util.Console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleCustomField extends ConsoleField
{
    private final UserInputPanelSpec userInputPanelSpec;

    private final IXMLElement spec;

    private final FieldCommand createField;

    private int numberOfRows = 0;

    private int numberOfColumns = 0;

    private final CustomField customField;

    private final static int INVALID = -1;

    private final static int CONTINUE = 1;

    private final static int ADD_MODULE = 2;

    private final static int REDISPLAY = 3;

    Map<Integer, List<ConsoleField>> consoleFields;

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
        this.numberOfColumns = customField.getFields().size();
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
        boolean onModule = true;
        List<ConsoleField> fields = new ArrayList<ConsoleField>();
        
        for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
        {
            field.setVariable(field.getVariable() + "." + numberOfRows);
            ConsoleField consoleField = createField.createConsoleField(field);
            fields.add(consoleField);
        }

        consoleFields.put(numberOfRows, fields);

        int value = INVALID;
        while (onModule)
        {
            value = INVALID;
            for (ConsoleField field : fields)
            {
                field.setDisplayed(true);
                while (!field.display())
                {
                    //Continue to ask for input if it was invalid
                }
            }

            while (value == INVALID)
            {
                value = prompt("Enter 1 continue, or 2 to add another module, 3 to redisplay", 1, 3, -1, -1);
            }
            if(value != REDISPLAY)
            {
                onModule = false;
            }
        }

        if(value == ADD_MODULE)
        {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean display()
    {
        numberOfRows = 0;
        consoleFields = new HashMap<Integer, List<ConsoleField>>();

        while(addRow())
        {
            //Keep adding rows until the user is done or max limit is reached
        }
        customField.setValue(numberOfRows + "");

        if(!columnsAreValid())
        {
            this.display();
        }

        return true;
    }

    private boolean columnsAreValid()
    {
        List<Column> columns = customField.getColumns();
        String [] columnVariables = getVariablesByColumn();
        for (int i = 0; i < columnVariables.length; i++)
        {
            ValidationStatus status = columns.get(i).validate(columnVariables[i]);
            if (!status.isValid())
            {
                System.out.println(status.getMessage());
                return false;
            }
        }
        return true;
    }

    //TODO: Refactor duplicated code form CustomInputRows
    private String[] getVariablesByColumn()
    {
        String[] columnVariables = new String[numberOfColumns];

        for(int col=0; col < numberOfColumns; col++)
        {
            columnVariables[col] = "";
            for (int row=1; row <= numberOfRows; row++)
            {
                ConsoleField consoleField = consoleFields.get(row).get(col);
                if (consoleField.isDisplayed())
                {
                    columnVariables[col] += getField().getInstallData().getVariable(consoleField.getVariable()) + ",";
                }
            }
        }
        for (int i=0; i < columnVariables.length; i++)
        {
            String v = columnVariables[i];
            columnVariables[i] = v.substring(0, v.length()-1);
        }
        return columnVariables;
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
