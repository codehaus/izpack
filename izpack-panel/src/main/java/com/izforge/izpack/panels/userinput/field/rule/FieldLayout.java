/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Rule field layout.
 *
 * @author Elmar Grom
 * @author Tim Anderson
 */
public class FieldLayout
{
    /**
     * The layout items. This is a mixture of field separator strings and FieldSpec instances.
     */
    private final List<Object> items = new ArrayList<Object>();

    /**
     * Constructs a  {@code FieldLayout}.
     *
     * @param layout the layout specifier
     */
    public FieldLayout(String layout)
    {
        StringTokenizer tokenizer = new StringTokenizer(layout);

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            FieldSpec spec = FieldSpec.parse(token);
            int size = items.size();
            if (spec != null)
            {
                // if the previous item is also a field, insert a space as separator
                if (size > 0 && items.get(size - 1) instanceof FieldSpec)
                {
                    items.add(" ");
                }
                items.add(spec);
            }
            else
            {
                if (!items.isEmpty() && items.get(size - 1) instanceof String)
                {
                    // if the previous item is also a separator simply concatenate the token with a space
                    // inserted in between, don't add it as new separator.
                    String last = (String) items.get(size - 1);
                    items.set(size - 1, last + " " + token);
                }
                else
                {
                    items.add(token);
                }
            }
        }
    }

    /**
     * Returns the layout items.
     *
     * @return a mixture of field separator strings and {@link FieldSpec} instances.
     */
    public List<Object> getLayout()
    {
        return items;
    }

}
