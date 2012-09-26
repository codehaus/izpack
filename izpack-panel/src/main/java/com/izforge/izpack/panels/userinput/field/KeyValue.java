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
 * A key-value pair.
 *
 * @author Tim Anderson
 */
public class KeyValue
{

    /**
     * The key.
     */
    private final String key;

    /**
     * The value.
     */
    private final String value;

    /**
     * Constructs a {@code KeyValue}.
     *
     * @param key   the key
     * @param value the value
     */
    public KeyValue(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String toString()
    {
        return value;
    }

}
