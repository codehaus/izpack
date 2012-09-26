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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.processor.Processor;

/**
 * FieldProcessor is a factory for an {@link Processor}, specified at construction by its class name.
 *
 * @author Tim Anderson
 */
public class FieldProcessor
{

    /**
     * The configuration.
     */
    private final Config config;

    /**
     * The processor class name.
     */
    private final String className;

    /**
     * Constructs a {@code FieldProcessor}.
     *
     * @param processor the processor element
     * @param config    the configuration
     */
    public FieldProcessor(IXMLElement processor, Config config)
    {
        className = config.getAttribute(processor, "class");
        this.config = config;
    }

    /**
     * Creates an instance of the processor.
     *
     * @return the new processor
     */
    public Processor create()
    {
        return config.getFactory().create(className, Processor.class);
    }
}
