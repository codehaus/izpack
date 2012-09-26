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

package com.izforge.izpack.panels.userinput.field;


import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;

/**
 * The user input panel specification.
 *
 * @author Tim Anderson
 */
public class UserInputPanelSpec
{

    /**
     * Provides access to the XML configuration.
     */
    private final Config config;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * {@link InstallData#getAttribute(String) Installation data attribute name} that tracks the number of
     * UserInputPanel panels that have been created in order to associate the correct panel specification with the
     * right panel.
     */
    private static final String PANEL_COUNT = "UserInputPanel.instanceCount";

    /**
     * The name of the XML file that specifies the panel layout.
     */
    private static final String SPEC_FILE_NAME = "userInputSpec.xml";

    /**
     * The user input language pack resource name.
     */
    private static final String LANG_FILE_NAME = "userInputLang.xml";

    /**
     * Panel attribute name.
     */
    private static final String PANEL = "panel";

    /**
     * The panel order attribute name.
     */
    private static final String ORDER = "order";

    /**
     * The panel identifier attribute name
     */
    private static final String PANEL_IDENTIFIER = "id";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UserInputPanelSpec.class.getName());


    /**
     * Constructs an {@code UserInputPanelSpec}.
     */
    public UserInputPanelSpec(Resources resources, InstallData installData, ObjectFactory factory)
    {
        Messages messages = installData.getMessages();
        try
        {
            messages = messages.newMessages(LANG_FILE_NAME);
        }
        catch (ResourceNotFoundException exception)
        {
            logger.info(exception.getMessage());
        }

        config = new Config(SPEC_FILE_NAME, resources, installData, factory, messages);
        this.installData = installData;
    }

    /**
     * Returns the specification for the supplied panel.
     *
     * @param panel the panel
     * @return the corresponding specification
     * @throws IzPackException if there is no specification for the panel
     */
    public IXMLElement getPanelSpec(Panel panel)
    {
        // extract the spec to this specific panel instance
        Integer count = (Integer) installData.getAttribute(PANEL_COUNT);
        count = (count == null) ? 0 : count + 1;

        String instance = Integer.toString(count);
        String panelId = panel.getPanelId();
        List<IXMLElement> panels = config.getRoot().getChildrenNamed(PANEL);
        IXMLElement result = null;
        for (IXMLElement spec : panels)
        {
            String order = spec.getAttribute(ORDER);
            String id = spec.getAttribute(PANEL_IDENTIFIER);

            if ((order != null && instance.equals(order)) || (id != null && panelId != null && panelId.equals(id)))
            {
                // use the current element as spec
                result = spec;
                break;
            }
        }
        if (result == null)
        {
            throw new IzPackException("No user input specification with " + PANEL_IDENTIFIER + "=" + panelId
                                              + " or " + ORDER + "=" + instance);
        }
        installData.setAttribute(PANEL_COUNT, count);
        return result;
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public Config getConfig()
    {
        return config;
    }
}
