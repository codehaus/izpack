/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.panels.userinput.gui;

import java.awt.Toolkit;

import javax.swing.JTextField;

import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.processorclient.RuleInputField;


/**
 * Rule field view.
 *
 * @author Tim Anderson
 */
public class GUIRuleFieldView extends GUIFieldView
{

    /**
     * The component.
     */
    private final RuleInputField component;


    /**
     * Constructs a {@code GUIRuleFieldView}.
     *
     * @param field       the field
     * @param toolkit     the toolkit
     * @param installData the installation data
     */
    public GUIRuleFieldView(RuleField field, Toolkit toolkit, GUIInstallData installData)
    {
        super(field);

        component = new RuleInputField(field, toolkit, installData);
        int id = 1;
        for (JTextField input : component.getInputFields())
        {
            input.setName(field.getVariable() + "." + id);
            ++id;
        }

        addField(component);
    }

    /**
     * Updates the field from the view.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField()
    {
        boolean result = component.validateContents();
        if (result)
        {
            getField().setValue(component.getText());
        }
        else
        {
            FieldValidator validator = getField().getValidator();
            if (validator != null)
            {
                warning(validator.getMessage());
            }
        }
        return result;
    }

}
