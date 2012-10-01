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

package com.izforge.izpack.panels.userinput.processorclient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFormat;
import com.izforge.izpack.panels.userinput.gui.rule.RuleInputField;
import com.izforge.izpack.util.Platforms;


/**
 * Tests the {@link com.izforge.izpack.panels.userinput.gui.rule.RuleInputField}.
 *
 * @author Tim Anderson
 */
public class RuleInputFieldTest
{

    /**
     * Tests {@link com.izforge.izpack.panels.userinput.gui.rule.RuleInputField} support for entering IP addresses.
     */
    @Test
    public void testIPAddressRuleInputField()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String set = "0:192 1:168 2:0 3:1";              // default value
        String separator = null;

        GUIInstallData installData = new GUIInstallData(new DefaultVariables(), Platforms.HP_UX);
        RuleField model = new RuleField(layout, RuleFormat.DISPLAY_FORMAT, set, separator, null, null, installData);

        RuleInputField field = new RuleInputField(model, installData);

        // check default value
        assertEquals("192.168.0.1", field.getText());

        String[] values = field.getValues();
        assertEquals(4, values.length);
        assertEquals("192", values[0]);
        assertEquals("168", values[1]);
        assertEquals("0", values[2]);
        assertEquals("1", values[3]);

        // TODO - need to provide methods to update fields and verify field formats
    }
}
