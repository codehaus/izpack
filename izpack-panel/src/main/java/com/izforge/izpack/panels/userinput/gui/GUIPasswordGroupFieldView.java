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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPasswordField;

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.PasswordGroup;
import com.izforge.izpack.panels.userinput.field.password.PasswordField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.validator.ValidatorContainer;


/**
 * The password group field view.
 *
 * @author Tim Anderson
 */
public class GUIPasswordGroupFieldView extends GUIFieldView
{

    /**
     * The passwords.
     */
    private final List<JPasswordField> passwords = new ArrayList<JPasswordField>();

    /**
     * The password group.
     */
    private final PasswordGroup group;


    /**
     * Constructs a {@code GUIPasswordGroupFieldView}.
     *
     * @param field the field
     */
    public GUIPasswordGroupFieldView(PasswordGroupField field)
    {
        super(field);
        addDescription();

        List<ValidatorContainer> validatorsList = getValidatorContainers();
        group = new PasswordGroup(validatorsList, field.getProcessor());

        int id = 1;
        for (PasswordField f : field.getPasswordFields())
        {
            JPasswordField component = new JPasswordField(f.getSet(), f.getSize());
            component.setName(field.getVariable() + "." + id++);
            component.setCaretPosition(0);

            addLabel(f.getLabel());

            passwords.add(component);
            addComponent(component, new TwoColumnConstraints(TwoColumnConstraints.EAST));
            group.addField(component);
        }
    }

    /**
     * Updates the field from the view.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField()
    {
        boolean result = false;
        int size = group.validatorSize();
        if (size == 0)
        {
            result = true;
        }
        else
        {
            for (int i = 0; i < size; ++i)
            {
                result = group.validateContents(i);
                if (!result)
                {
                    warning(group.getValidatorMessage(i));
                    break;
                }
            }
        }
        if (result)
        {
            getField().setValue(group.getPassword());
        }
        return result;
    }

    /**
     * Updates the view from the field.
     *
     * @return {@code true} if the view was updated
     */
    @Override
    public boolean updateView()
    {
        boolean result = false;
        String value = getField().getValue();
        if (value != null)
        {
            for (JPasswordField view : passwords)
            {
                view.setText(replaceVariables(value));
            }
            result = true;
        }
        return result;
    }
}
