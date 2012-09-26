/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2008 Piotr Skowronek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.userinput.processorclient;

import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.gui.FlowLayout;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.validator.Validator;

/**
 * This class is a wrapper for JTextField to allow field validation.
 * Based on RuleInputField.
 *
 * @author Piotr Skowronek
 */
public class TextInputField extends JComponent implements ProcessingClient
{
    private static final long serialVersionUID = 8611515659787697087L;

    private static final transient Logger logger = Logger.getLogger(TextInputField.class.getName());

    /**
     * Validator parameters.
     */
    private Map<String, String> validatorParams;

    /**
     * Holds an instance of the <code>Validator</code> if one was specified.
     */
    private Validator validator;

    /**
     * This composite can only contain one component ie JTextField
     */
    private JTextField field;

    /**
     * Constructs a text input field.
     *
     * @param model       the field model
     * @param installData the installation data
     */
    public TextInputField(TextField model, InstallData installData)
    {
        FieldValidator fieldValidator = model.getValidator();
        if (fieldValidator != null)
        {
            validator = fieldValidator.create();
            validatorParams = fieldValidator.getParameters();
        }

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEADING);
        layout.setVgap(0);
        setLayout(layout);

        // ----------------------------------------------------
        // construct the UI element and add it to the composite
        // ----------------------------------------------------
        field = new JTextField(model.getInitialValue(installData), model.getSize());
        field.setName(model.getVariable());
        field.setCaretPosition(0);
        add(field);
    }

    /**
     * Returns the validator parameters, if any. The caller should check for the existence of
     * validator parameters via the <code>hasParams()</code> method prior to invoking this method.
     *
     * @return a java.util.Map containing the validator parameters.
     */
    @Override
    public Map<String, String> getValidatorParams()
    {
        return validatorParams;
    }

    /**
     * Returns the field contents, assembled acording to the encryption and separator rules.
     *
     * @return the field contents
     */
    @Override
    public String getText()
    {
        return field.getText();
    }

    public void setText(String value)
    {
        field.setText(value);
    }

    @Override
    public String getFieldContents(int index)
    {
        return field.getText();
    }

    @Override
    public int getNumFields()
    {
        // We've got only one field
        return 1;
    }

    /**
     * This method validates the field content. Validating is performed through a user supplied
     * service class that provides the validation rules.
     *
     * @return <code>true</code> if the validation passes or no implementation of a validation
     *         rule exists. Otherwise <code>false</code> is returned.
     */
    public boolean validateContents()
    {
        if (validator != null)
        {
            logger.fine("Validating contents");
            return validator.validate(this);
        }
        else
        {
            logger.fine("Not validating contents");
            return true;
        }
    }

    // javadoc inherited

    @Override
    public boolean hasParams()
    {
        return validatorParams != null && !validatorParams.isEmpty();
    }
}
