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

/**
 * The view of a field.
 *
 * @author Tim Anderson
 */
public interface FieldView
{

    /**
     * Returns the field.
     *
     * @return the field
     */
    Field getField();

    /**
     * Updates the field from the view.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    boolean updateField();

    /**
     * Updates the view from the field.
     *
     * @return {@code true} if the view was updated
     */
    boolean updateView();

    /**
     * Determines if the view is being displayed.
     *
     * @return {@code true} if the view is being displayed
     */
    boolean isDisplayed();

    /**
     * Determines if the view is being displayed.
     *
     * @param displayed {@code true} if the view is being displayed
     */
    void setDisplayed(boolean displayed);

}
