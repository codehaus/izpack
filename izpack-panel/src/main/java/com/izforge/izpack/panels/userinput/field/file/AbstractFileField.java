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

package com.izforge.izpack.panels.userinput.field.file;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;


/**
 * Common file field functionality.
 *
 * @author Tim Anderson
 */
public class AbstractFileField extends Field
{

    /**
     * The file extension. May be {@code null}.
     */
    private final String fileExtension;

    /**
     * The file extension description. May be {@code null}.
     */
    private final String fileExtensionDescription;

    /**
     * Determines if empty input values are allowed.
     */
    protected final boolean allowEmptyValue;


    /**
     * Constructs an {@code AbstractFileField}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public AbstractFileField(AbstractFileFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        fileExtension = reader.getFileExtension();
        fileExtensionDescription = reader.getFileExtensionDescription();
        allowEmptyValue = reader.getAllowEmptyValue();
    }

    /**
     * Returns the file extension.
     *
     * @return the file extension. May be {@code null}
     */
    public String getFileExtension()
    {
        return fileExtension;
    }

    /**
     * Returns the file extension description.
     *
     * @return the file extension description. May be {@code null}
     */
    public String getFileExtensionDescription()
    {
        return fileExtensionDescription;
    }

    /**
     * Determines if empty input values are allowed.
     *
     * @return {@code true} if empty input values are allowed; otherwise {@code false}
     */
    public boolean getAllowEmptyValue()
    {
        return allowEmptyValue;
    }
}
