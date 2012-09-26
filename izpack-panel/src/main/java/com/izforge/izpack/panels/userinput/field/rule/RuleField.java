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

package com.izforge.izpack.panels.userinput.field.rule;

import java.util.Arrays;
import java.util.Collections;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldProcessor;
import com.izforge.izpack.panels.userinput.field.FieldValidator;


/**
 * Rule field.
 *
 * @author Tim Anderson
 */
public class RuleField extends Field
{
    /**
     * The rule field layout.
     */
    private final String layout;

    /**
     * The rule format.
     */
    private final RuleFormat format;

    /**
     * The field separator.
     */
    private final String separator;


    /**
     * Constructs a {@code RuleField}.
     *
     * @param reader the reader to get field information from
     * @throws IzPackException if the field cannot be read
     */
    public RuleField(RuleFieldReader reader)
    {
        super(reader);
        this.layout = reader.getLayout();
        this.format = reader.getFormat();
        this.separator = reader.getSeparator();
    }

    /**
     * Constructs a {@code RuleField}.
     *
     * @param layout    the field layout. May be {@code null}
     * @param format    the rule format
     * @param set       the initial value. May be {@code null}
     * @param separator the field separator
     * @param validator the field validator
     * @param processor the field processor
     */
    public RuleField(String layout, RuleFormat format, String set, String separator, FieldValidator validator,
                     FieldProcessor processor)
    {
        super(null, set, 0, null, null,
              validator != null ? Arrays.asList(validator) : Collections.<FieldValidator>emptyList(),
              processor, null, null, false, null);
        this.layout = layout;
        this.separator = separator;
        this.format = format;
    }

    /**
     * Returns the field layout.
     *
     * @return the field layout. May be {@code null}
     */
    public String getLayout()
    {
        return layout;
    }

    /**
     * Returns the field separator.
     * <p/>
     * This is a string used for separating the contents of individual fields.
     *
     * @return the field separator. May be {@code null}
     */
    public String getSeparator()
    {
        return separator;
    }

    /**
     * Returns the field format.
     *
     * @return the field format
     */
    public RuleFormat getFormat()
    {
        return format;
    }
}