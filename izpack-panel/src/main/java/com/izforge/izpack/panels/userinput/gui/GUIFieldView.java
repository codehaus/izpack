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

package com.izforge.izpack.panels.userinput.gui;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.FieldView;
import com.izforge.izpack.panels.userinput.validator.ValidatorContainer;
import com.izforge.izpack.util.HyperlinkHandler;


/**
 * GUI view of a field.
 *
 * @author Tim Anderson
 */
public abstract class GUIFieldView implements FieldView
{

    /**
     * The field.
     */
    private final Field field;

    /**
     * The components that are the view of the field.
     */
    private List<Component> components = new ArrayList<Component>();

    /**
     * Determines if the view is being displayed.
     */
    private boolean displayed = false;

    /**
     * The listener to notify of field updates.
     */
    private UpdateListener listener;


    /**
     * Constructs a {@code GUIFieldView}.
     *
     * @param field the field
     */
    public GUIFieldView(Field field)
    {
        this.field = field;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public Field getField()
    {
        return field;
    }

    /**
     * Returns the variable associated with the field.
     *
     * @return the variable, or {@code null} if the field doesn't update a variable
     */
    public String getVariable()
    {
        return field.getVariable();
    }

    /**
     * Updates the field from the view.
     * <p/>
     * This implementation simply returns {@code true}.
     *
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField()
    {
        return true;
    }

    /**
     * Updates the view from the field.
     * <p/>
     * This implementation simply returns {@code false}.
     *
     * @return {@code true} if the view was updated
     */
    @Override
    public boolean updateView()
    {
        return false;
    }

    /**
     * Returns the components that make up the view.
     *
     * @return the components
     */
    public List<Component> getComponents()
    {
        return components;
    }

    /**
     * Registers a listener to be notified of field updates.
     *
     * @param listener the listener to notify
     */
    public void setUpdateListener(UpdateListener listener)
    {
        this.listener = listener;
    }

    /**
     * Determines if the view is being displayed.
     *
     * @return {@code true} if the view is being displayed
     */
    @Override
    public boolean isDisplayed()
    {
        return displayed;
    }

    /**
     * Determines if the view is being displayed.
     *
     * @param displayed {@code true} if the view is being displayed
     */
    @Override
    public void setDisplayed(boolean displayed)
    {
        this.displayed = displayed;
    }

    /**
     * Adds the field.
     * <p/>
     * This adds the field description (if any), the field label, and field component.
     *
     * @param component the component
     */
    protected void addField(JComponent component)
    {
        addDescription();
        addLabel();
        addComponent(component);
    }

    /**
     * Adds the label for the field.
     */
    protected void addLabel()
    {
        addLabel(field.getLabel());
    }

    /**
     * Adds a label.
     *
     * @param label the label
     */
    protected void addLabel(String label)
    {
        addComponent(new JLabel(label), new TwoColumnConstraints(TwoColumnConstraints.WEST));
    }

    /**
     * Returns the validator containers.
     *
     * @return the validator containers
     */
    protected List<ValidatorContainer> getValidatorContainers()
    {
        List<ValidatorContainer> result = Collections.emptyList();
        List<FieldValidator> validators = field.getValidators();
        if (!validators.isEmpty())
        {
            result = new ArrayList<ValidatorContainer>(validators.size());
            for (FieldValidator validator : validators)
            {
                ValidatorContainer container = new ValidatorContainer(validator.create(), validator.getMessage(),
                                                                      validator.getParameters());
                result.add(container);
            }
        }
        return result;
    }

    /**
     * Adds a component.
     *
     * @param component   the component
     * @param constraints the component constraints
     */
    protected void addComponent(JComponent component, Object constraints)
    {
        components.add(new Component(component, constraints));
    }

    /**
     * Adds a field description to the list of UI elements.
     */
    protected void addDescription()
    {
        String description = field.getDescription();
        if (description != null)
        {
            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
            constraints.stretch = true;

            JTextPane label = new JTextPane();

            // Not editable, but still selectable.
            label.setEditable(false);

            // If html tags are present enable html rendering, otherwise the JTextPane
            // looks exactly like MultiLineLabel.
            if (description.startsWith("<html>") && description.endsWith("</html>"))
            {
                label.setContentType("text/html");
                label.addHyperlinkListener(new HyperlinkHandler());
            }
            label.setText(description);

            // Background color and font to match the label's.
            label.setBackground(UIManager.getColor("label.background"));
            label.setMargin(new Insets(3, 0, 3, 0));
            // workaround to cut out layout problems
            label.getPreferredSize();
            // end of workaround.

            addComponent(label, constraints);
        }
    }

    /**
     * Notifies any registered listener that the view has updated.
     */
    protected void notifyUpdateListener()
    {
        if (listener != null)
        {
            listener.updated();
        }
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return field.getInstallData();
    }

    /**
     * Helper to replace variables in a string.
     *
     * @param value the string to perform variable replacement on. May be {@code null}
     * @return the string with any variables replaced with their values
     */
    protected String replaceVariables(String value)
    {
        return getInstallData().getVariables().replace(value);
    }

    /**
     * Show localized warning message dialog basing on given parameters.
     *
     * @param message the message to print out in dialog box.
     */
    protected void warning(String message)
    {
        Messages messages = getInstallData().getMessages();
        GUIPrompt prompt = new GUIPrompt();
        prompt.message(Prompt.Type.WARNING, messages.get("UserInputPanel.error.caption"), message);
    }

    /**
     * Adds a component.
     *
     * @param component the component
     */
    private void addComponent(JComponent component)
    {
        addComponent(component, new TwoColumnConstraints(TwoColumnConstraints.EAST));
    }

}
