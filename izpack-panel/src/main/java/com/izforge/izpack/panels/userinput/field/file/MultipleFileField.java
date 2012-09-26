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

import com.izforge.izpack.api.exception.IzPackException;


/**
 * Multiple file field.
 *
 * @author Tim Anderson
 */
public class MultipleFileField extends AbstractFileField
{

    /**
     * The number of visible rows.
     */
    private final int visibleRows;

    /**
     * The preferred width of the field.
     */
    private final int width;

    /**
     * The preferred height of the field.
     */
    private final int height;

    /**
     * Determines if multiple variables should be created to hold the selected files.
     */
    private boolean multipleVariables;

    /**
     * Constructs a {@code MultipleFileField}.
     *
     * @param reader the reader to get field information from
     * @throws IzPackException if the field cannot be read
     */
    public MultipleFileField(MultipleFileFieldReader reader)
    {
        super(reader);
        visibleRows = reader.getVisibleRows();
        width = reader.getPreferredWidth();
        height = reader.getPreferredHeight();
    }

    /**
     * Returns the number of visible rows.
     *
     * @return the number of visible rows, or {@code -1} if none is specified
     */
    public int getVisibleRows()
    {
        return visibleRows;
    }

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred width, or {@code -1} if none is specified
     */
    public int getPreferredWidth()
    {
        return width;
    }

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred height, or {@code -1} if none is specified
     */
    public int getPreferredHeight()
    {
        return height;
    }

    /**
     * Determines if multiple variables should be created to hold the selected files.
     *
     * @return {@code true} if multiple variables should be created; {@code false} if a single variable should be used
     */
    public boolean getCreateMultipleVariables()
    {
        return multipleVariables;
    }
}
