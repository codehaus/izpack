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

package com.izforge.izpack.panels.userinput.field.radio;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;

/**
 * Radio field.
 *
 * @author Tim Anderson
 */
public class RadioField extends Field
{

    /**
     * The choices.
     */
    private final List<RadioChoice> choices;

    /**
     * The initial selected index.
     */
    private final int selected;

    /**
     * Constructs a {@code ComboField}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public RadioField(RadioFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        this.choices = reader.getChoices();
        this.selected = reader.getSelectedIndex();
    }

    /**
     * Returns the choices.
     *
     * @return the choices
     */
    public List<RadioChoice> getChoices()
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
