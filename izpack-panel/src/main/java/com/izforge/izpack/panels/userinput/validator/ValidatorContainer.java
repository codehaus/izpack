package com.izforge.izpack.panels.userinput.validator;

import java.util.Map;

/**
 * @author Jeff Gordon
 */
public class ValidatorContainer
{
    private Validator validator = null;
    private String message;
    private boolean hasParams = false;
    private Map<String, String> validatorParams = null;

    public ValidatorContainer(Validator validator, String message, Map<String, String> validatorParams)
    {
        this.validator = validator;
        this.message = message;
        this.validatorParams = validatorParams;
        if (validatorParams != null && !validatorParams.isEmpty())
        {
            hasParams = true;
        }
    }

    /**
     * @return true if this instance has any parameters to pass to the Validator instance.
     */
    public boolean hasParams()
    {
        return hasParams;
    }

    /**
     * Returns the validator parameters, if any. The caller should check for the existence of
     * validator parameters via the <code>hasParams()</code> method prior to invoking this method.
     *
     * @return a java.util.Map containing the validator parameters.
     */
    public Map<String, String> getValidatorParams()
    {
        return validatorParams;
    }

    public Validator getValidator()
    {
        return validator;
    }

    public String getMessage()
    {
        return message;
    }


}
