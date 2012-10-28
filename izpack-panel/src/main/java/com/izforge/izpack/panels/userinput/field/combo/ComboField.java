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
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceField;


/**
 * Combo field.
 *
 * @author Tim Anderson
 */
public class ComboField extends ChoiceField<Choice>
{

    /**
     * Constructs a {@code ComboField}.
     *
     * @param reader the reader to get field information from
     * @throws IzPackException if the field cannot be read
     */
    public ComboField(ComboFieldReader reader, InstallData installData)
    {
        super(reader, installData);
    }

    /**
     * Constructs a {@code ComboField}.
     *
     * @param variable    the variable associated with the field
     * @param choices     the choices
     * @param selected    The selected choice, or {@code -1} if no choice is selected
     * @param packs       the packs that the field is associated with. If {@code null} or empty,
     *                    indicates the field applies to all packs
     * @param models      the operating systems that the field applies to. If {@code null} or empty, indicates it
     *                    applies to all operating systems
     * @param label       the field label. May be {@code null}
     * @param description the field description. May be {@code null}
     * @param installData the installation data
     */
    public ComboField(String variable, List<Choice> choices, int selected, List<String> packs, List<OsModel> models,
                      String label, String description, InstallData installData)
    {
        super(variable, choices, selected, packs, models, label, description, installData);
    }
}
