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

/**
 * Directory field.
 *
 * @author Tim Anderson
 */
public class DirField extends AbstractFileField
{

    /**
     * Determines if directories must exist.
     */
    private final boolean mustExist;

    /**
     * Determines if directories can be created if they don't exist.
     */
    private final boolean create;


    /**
     * Constructs a {@code DirField}.
     *
     * @param reader      the reader to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public DirField(DirFieldReader reader, InstallData installData)
    {
        super(reader, installData);
        mustExist = reader.getMustExist();
        create = reader.getCreate();
    }

    /**
     * Determines if directories must exist.
     *
     * @return {@code true} if the directories must exist; otherwise {@code false}
     */
    public boolean getMustExist()
    {
        return mustExist;
    }

    /**
     * Determines if directories can be created if they don't exist.
     *
     * @return {@code true} if directories can be created if they don't exist
     */
    public boolean getCreate()
    {
        return create;
    }
}
