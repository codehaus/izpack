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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.processorclient.TextInputField;


/**
 * Text field view.
 *
 * @author Tim Anderson
 */
public class GUITextFieldView extends GUIFieldView
{

    /**
     * The component.
     */
    private final TextInputField text;

    /**
     * Constructs a {@code GUITextFieldView}.
     *
     * @param field the field
     */
    public GUITextFieldView(TextField field)
    {
        super(field);
        text = new TextInputField(field);
        text.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent event)
            {
            }

            @Override
            public void focusLost(FocusEvent event)
            {
                checkUpdate();
            }
        });
        addField(text);
    }

    /**
     * Updates the field from the view.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField()
    {
        boolean result = text.validateContents();
        if (result)
        {
            getField().setValue(text.getText());
        }
        else
        {
            FieldValidator validator = getField().getValidator();
            String message = (validator != null) ? validator.getMessage() : null;
            if (message == null)
            {
                message = "Text entered did not pass validation.";
            }
            warning(message);
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
            text.setText(replaceVariables(value));
            result = true;
        }
        return result;
    }

    /**
     * Determines if the field has updated. If so, it notifies any registered listener.
     */
    private void checkUpdate()
    {
        String value = text.getText();
        String existing = getField().getValue();
        if ((value != null && !value.equals(existing)) || (value == null && existing != null))
        {
            notifyUpdateListener();
        }
    }
}
