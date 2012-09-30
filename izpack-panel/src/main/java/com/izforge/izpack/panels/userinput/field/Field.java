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

package com.izforge.izpack.panels.userinput.field;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.core.rules.process.ExistsCondition;

/**
 * Describes a user input field.
 *
 * @author Tim Anderson
 */
public abstract class Field
{

    /**
     * The variable. May be {@code null}.
     */
    private final String variable;

    /**
     * Specifies the default (or set) value for the field.
     */
    private final String set;

    /**
     * The field size.
     */
    private final int size;

    /**
     * The packs that the field applies to. May be {@code null} or empty to indicate all packs.
     */
    private final List<String> packs;

    /**
     * The the operating systems that the field applies to. An empty list indicates it applies to all operating systems
     */
    private final List<OsModel> models;

    /**
     * The field validators.
     */
    private final List<FieldValidator> validators;

    /**
     * The field processor. May be {@code null}
     */
    private final FieldProcessor processor;

    /**
     * The field label. May be {@code null}
     */
    private final String label;

    /**
     * The field description. May be {@code null}
     */
    private final String description;

    /**
     * Determines if field updates trigger re-validation.
     */
    private final boolean revalidate;

    /**
     * Condition that determines if the field is displayed or not.
     */
    private final String condition;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Field.class.getName());

    /**
     * Constructs a {@code Field}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public Field(FieldReader reader, InstallData installData)
    {
        this(reader.getVariable(), reader.getDefaultValue(), reader.getSize(), reader.getPacks(), reader.getOsModels(),
             reader.getValidators(), reader.getProcessor(), reader.getLabel(), reader.getDescription(),
             reader.getRevalidate(), reader.getCondition(), installData);
    }


    /**
     * Constructs a {@code Field}.
     *
     * @param variable    the variable associated with the field
     * @param set         the pre-set value for the field
     * @param size        the field size
     * @param packs       the packs that the field is associated with. If {@code null} or empty,
     *                    indicates the field applies to all packs
     * @param models      the operating systems that the field applies to. An empty list indicates it applies to all
     *                    operating systems
     * @param validators  the field validators. May be empty
     * @param processor   the field processor. May be {@code null}
     * @param label       the field label. May be {@code null}
     * @param description the field description. May be {@code null}
     * @param revalidate  determines if field updates trigger re-validation
     * @param condition   condition that determines if the field is displayed or not
     * @param installData the installation data
     */
    public Field(String variable, String set, int size, List<String> packs, List<OsModel> models,
                 List<FieldValidator> validators, FieldProcessor processor, String label, String description,
                 boolean revalidate, String condition, InstallData installData)
    {
        this.variable = variable;
        this.set = set;
        this.size = size;
        this.packs = packs;
        this.models = models;
        this.validators = validators;
        this.processor = processor;
        this.label = label;
        this.description = description;
        this.revalidate = revalidate;
        this.condition = condition;
        this.installData = installData;

        if (variable != null)
        {
            addExistsCondition();
        }
    }

    /**
     * Returns the variable.
     *
     * @return the variable. May be {@code null}
     */
    public String getVariable()
    {
        return variable;
    }

    /**
     * Returns all variables that this field updates.
     *
     * @return all variables that this field updates
     */
    public List<String> getVariables()
    {
        return variable != null ? Arrays.asList(variable) : Collections.<String>emptyList();
    }

    /**
     * Returns the packs that the field applies to.
     *
     * @return the pack names
     */
    public List<String> getPacks()
    {
        return packs;
    }

    /**
     * Returns the operating systems that the field applies to.
     *
     * @return the OS family names
     */
    public List<OsModel> getOsModels()
    {
        return models;
    }

    /**
     * Returns the default value of the field.
     *
     * @return the default value. May be {@code null}
     */
    public String getDefaultValue()
    {
        return set;
    }

    /**
     * Returns the initial value.
     * <p/>
     * If the field is associated with a variable, and the variable value is non-null, this is returned, otherwise
     * {@link #getDefaultValue} is returned.
     *
     * @return the initial value
     */
    public String getInitialValue()
    {
        String result = getValue();
        if (result == null)
        {
            result = getDefaultValue();
        }
        return result;
    }

    /**
     * Returns the variable value.
     *
     * @return the variable value. May be {@code null}
     */
    public String getValue()
    {
        return installData.getVariable(variable);
    }

    /**
     * Sets the variable value.
     *
     * @param value the variable value. May be {@code null}
     */
    public void setValue(String value)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Field setting variable=" + variable + " to value=" + value);
        }
        installData.setVariable(variable, value);
    }

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is defined
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Returns the field validators.
     *
     * @return the field validators
     */
    public List<FieldValidator> getValidators()
    {
        return validators;
    }

    /**
     * Returns the first field validator.
     *
     * @return the first field validator. May be {@code null}
     */
    public FieldValidator getValidator()
    {
        return !validators.isEmpty() ? validators.get(0) : null;
    }

    /**
     * Returns the field processor.
     *
     * @return the field processor. May be {@code null}
     */
    public FieldProcessor getProcessor()
    {
        return processor;
    }

    /**
     * Returns the field label.
     *
     * @return the field label. May be {@code null}
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the field description.
     *
     * @return the field description. May be {@code null}
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Determines if the field triggers revalidation on update.
     *
     * @return {@code true} if the field triggers revalidation
     */
    public boolean getRevalidate()
    {
        return revalidate;
    }

    /**
     * Determines if the condition associated with the field is true.
     *
     * @return {@code true} if the condition evaluates {true} or if the field has no condition
     */
    public boolean isConditionTrue()
    {
        return (condition == null || installData.getRules().isConditionTrue(condition, installData));
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    public InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Adds an 'exists' condition for the variable.
     */
    private void addExistsCondition()
    {
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.setContentType(ExistsCondition.ContentType.VARIABLE);
        existsCondition.setContent(variable);
        existsCondition.setId("izpack.input." + variable);
        existsCondition.setInstallData(installData);
        installData.getRules().addCondition(existsCondition);
    }

}
