package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.RulesEngine;

abstract public class SimpleChoiceReader extends FieldReader
{

    /**
     * The initial selected index.
     */
    private int selected = 0;


    /**
     * Constructs a {@code SimpleChoiceReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public SimpleChoiceReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Determines if a choice is selected.
     * <p/>
     * A choice is selected if:
     * <ul>
     * <li>the variable value is the same as the choice "value" attribute; or</li>
     * <li>the "set" attribute is 'true'</li>
     * </ul>
     *
     * @param value         the choice value
     * @param choice        the choice element
     * @param variableValue the variable value. May be {@code null}
     * @return {@code true} if the choice is selected
     */
    protected boolean isSelected(String value, IXMLElement choice, String variableValue)
    {
        boolean result = false;
        if (variableValue != null)
        {
            if (variableValue.equals(value))
            {
                result = true;
            }
        }
        else
        {
            result = getConfig().getBoolean(choice, "set", false);
        }
        return result;
    }

    /**
     * Returns the index of the selected choice.
     * <p/>
     * A choice is selected if:
     * <ul>
     * <li>the variable value is the same as the choice "value" attribute; or</li>
     * <li>the "set" attribute is 'true'</li>
     * </ul>
     * <p/>
     * This is only valid after {@link #()} is invoked.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
    }

    private boolean isDisplayed(RulesEngine rules, String conditionId)
    {
        return (rules == null || conditionId == null || rules.isConditionTrue(conditionId));
    }
}
