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
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
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
    private final FieldLayout layout;

    /**
     * The rule format.
     */
    private final RuleFormat format;

    /**
     * The field separator.
     */
    private final String separator;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(RuleField.class.getName());


    /**
     * Constructs a {@code RuleField}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public RuleField(RuleFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        this.layout = new FieldLayout(reader.getLayout());
        this.format = reader.getFormat();
        this.separator = reader.getSeparator();
    }

    /**
     * Constructs a {@code RuleField}.
     *
     * @param layout      the field layout
     * @param format      the rule format
     * @param set         the initial value. May be {@code null}
     * @param separator   the field separator
     * @param validator   the field validator
     * @param processor   the field processor
     * @param installData the installation data
     */
    public RuleField(String layout, RuleFormat format, String set, String separator, FieldValidator validator,
                     FieldProcessor processor, InstallData installData)
    {
        super(null, set, 0, null, null,
              validator != null ? Arrays.asList(validator) : Collections.<FieldValidator>emptyList(),
              processor, null, null, false, null, installData);
        this.layout = new FieldLayout(layout);
        this.separator = separator;
        this.format = format;
    }

    /**
     * Returns the field layout.
     *
     * @return the field layout
     */
    public FieldLayout getLayout()
    {
        return layout;
    }

    /**
     * Formats the values according to the field format.
     *
     * @param values the values to format
     * @return the formatted values
     * @throws IzPackException if formatting fails
     */
    public String format(String[] values)
    {
        String result;

        switch (format)
        {
            case PLAIN_STRING:
                result = formatPlain(values);
                break;
            case SPECIAL_SEPARATOR:
                result = formatSpecialSeparator(values);
                break;
            case PROCESSED:
                result = formatProcessed(values);
                break;
            default:
                result = formatDisplay(values);
        }

        return result;
    }

    /**
     * Specifies to return the contents of all fields together with all separators as specified in the field format
     * concatenated into one long string.
     * In this case the resulting string looks just like the user saw it during data entry.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatDisplay(String[] values)
    {
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Object item : layout.getLayout())
        {
            if (item instanceof String)
            {
                result.append(item);
            }
            else
            {
                if (index < values.length)
                {
                    result.append(values[index]);
                    ++index;
                }
            }
        }
        return result.toString();
    }

    /**
     * Plain formatting. Concatenates the values with no separators.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatPlain(String[] values)
    {
        StringBuilder result = new StringBuilder();
        for (String value : values)
        {
            result.append(value);
        }
        return result.toString();
    }

    /**
     * Concatenates the values with the separator in between.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatSpecialSeparator(String[] values)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; ++i)
        {
            if (i > 0)
            {
                result.append(separator);
            }
            result.append(values[i]);
        }
        return result.toString();
    }

    /**
     * Formats values using an {@link FieldProcessor}.
     *
     * @param values the values to format
     * @return the formatted string
     * @throws IzPackException if formatting fails
     */
    private String formatProcessed(String[] values)
    {
        String result;
        FieldProcessor processor = getProcessor();
        if (processor != null)
        {
            result = processor.process(values);
        }
        else
        {
            logger.warning("Rule field has " + format + " type, but no processor is registered");
            // fallback to display format
            result = formatDisplay(values);
        }
        return result;
    }

}