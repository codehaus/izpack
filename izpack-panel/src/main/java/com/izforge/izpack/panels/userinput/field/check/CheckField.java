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

package com.izforge.izpack.panels.userinput.field.check;

import java.util.Collections;
import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldValidator;


/**
 * Check box field.
 *
 * @author Tim Anderson
 */
public class CheckField extends Field
{
    /**
     * Assigned to the associated variable if the check box is selected. May be {@code null}
     */
    private final String trueValue;

    /**
     * Assigned to the associated variable if the check box is unselected. May be {@code null}
     */
    private final String falseValue;


    /**
     * Constructs a {@code CheckField}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public CheckField(CheckFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        trueValue = reader.getTrueValue();
        falseValue = reader.getFalseValue();
    }

    /**
     * Constructs a {@code CheckField}.
     *
     * @param variable    the variable associated with the field
     * @param trueValue   assigned to the variable if the check box is selected. May be {@code null}
     * @param falseValue  assigned to the variable if the check box is unselected. May be {@code null}
     * @param set         the pre-set value for the field
     * @param packs       the packs that the field is associated with. If {@code null} or empty,
     *                    indicates the field applies to all packs
     * @param models      the operating systems that the field applies to. An empty list indicates it applies to all
     *                    operating systems
     * @param label       the field label. May be {@code null}
     * @param description the field description. May be {@code null}
     * @param installData the installation data
     */
    public CheckField(String variable, String trueValue, String falseValue, String set, List<String> packs,
                      List<OsModel> models, String label, String description, InstallData installData)
    {
        super(variable, set, 0, packs, models, Collections.<FieldValidator>emptyList(), null, label, description,
              false, null, installData);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * Returns the value to assign to the associated variable when the checkbox is selected (i.e. is 'true').
     *
     * @return the 'true' value
     */
    public String getTrueValue()
    {
        return trueValue;
    }

    /**
     * Returns the value to assign to the associated variable when the checkbox is selected (i.e. is 'false').
     *
     * @return the 'false' value
     */
    public String getFalseValue()
    {
        return falseValue;
    }

    /**
     * Returns the initial selection of the check box.
     * <p/>
     * The initial selection is determined by the {@link #getInitialValue initial value}. If this is the same as the
     * {@link #getTrueValue() 'true value'} or {@code "true"} then the check box should be selected.
     *
     * @return {@code true} if the check box should be selected, {@code false} if it should be deselected.
     */
    public boolean getInitialSelection()
    {
        String value = getInitialValue();
        boolean result = false;
        if (value != null)
        {
            result = value.equals(trueValue) || Boolean.valueOf(value);
        }
        return result;
    }
}
