/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field.radio;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.SimpleChoiceReader;


/**
 * Radio field reader.
 *
 * @author Tim Anderson
 */
public class RadioFieldReader extends SimpleChoiceReader implements ChoiceFieldConfig<Choice>
{

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The initial selected index.
     */
    private int selected = 0;

    /**
     * Constructs a {@code RadioFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public RadioFieldReader(IXMLElement field, Config config, InstallData installData)
    {
        super(field, config);
        this.installData = installData;
    }

    /**
     * Returns the choices.
     *
     * @return the choices
     */
    public List<Choice> getChoices(RulesEngine rules)
    {
        selected = 0;
        List<Choice> result = new ArrayList<Choice>();
        Config config = getConfig();
        String variableValue = installData.getVariable(getVariable());

        for (IXMLElement choice : getSpec().getChildrenNamed("choice"))
        {
            String value = config.getAttribute(choice, "value");
            if (isSelected(value, choice, variableValue))
            {
                selected = result.size();
            }

            String conditionId = config.getString(choice, "conditionid", null);
            if (rules == null || conditionId == null || rules.isConditionTrue(conditionId))
            {
                result.add(new Choice(value, getText(choice)));
            }
        }

        return result;
    }

}
