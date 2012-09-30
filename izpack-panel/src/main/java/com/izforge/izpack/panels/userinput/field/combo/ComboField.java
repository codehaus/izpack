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

package com.izforge.izpack.panels.userinput.field.combo;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.Field;

/**
 * Combo field.
 *
 * @author Tim Anderson
 */
public class ComboField extends Field
{

    /**
     * The available choices.
     */
    private final List<Choice> choices;

    /**
     * The selected index.
     */
    private final int selected;


    /**
     * Constructs a {@code ComboField}.
     *
     * @param reader the reader to get field information from
     * @throws IzPackException if the field cannot be read
     */
    public ComboField(ComboFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        this.choices = reader.getChoices();
        this.selected = reader.getSelectedIndex();
    }

    /**
     * Returns the choices.
     *
     * @return the choices. The key is the choice identifier, the value, the display text
     */
    public List<Choice> getChoices()
    {
        return choices;
    }

    /**
     * Returns the selected choice.
     *
     * @return the selected choice, or {2code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
    }

}
