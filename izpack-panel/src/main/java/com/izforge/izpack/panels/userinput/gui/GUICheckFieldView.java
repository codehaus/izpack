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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.check.CheckField;


/**
 * Check field view.
 *
 * @author Tim Anderson
 */
public class GUICheckFieldView extends GUIFieldView
{
    /**
     * The checkbox.
     */
    private final JCheckBox checkbox;


    /**
     * Constructs a {@code GUICheckFieldView}.
     *
     * @param field the field
     */
    public GUICheckFieldView(CheckField field)
    {
        super(field);
        checkbox = new JCheckBox(field.getLabel());
        checkbox.setName(field.getVariable());

        checkbox.setSelected(field.getInitialSelection());
        if (field.getRevalidate())
        {
            checkbox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    notifyUpdateListener();
                }
            });
        }

        addDescription();

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;
        constraints.indent = true;

        addComponent(checkbox, constraints);
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public CheckField getField()
    {
        return (CheckField) super.getField();
    }

    /**
     * Updates the field from the view.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField()
    {
        CheckField field = getField();
        if (checkbox.isSelected())
        {
            field.setValue(field.getTrueValue());
        }
        else
        {
            field.setValue(field.getFalseValue());
        }
        return true;
    }

}
