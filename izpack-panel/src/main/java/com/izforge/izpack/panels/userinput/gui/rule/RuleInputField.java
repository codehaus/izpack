/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.panels.userinput.gui.rule;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.regexp.RE;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.gui.FlowLayout;
import com.izforge.izpack.panels.userinput.field.rule.FieldLayout;
import com.izforge.izpack.panels.userinput.field.rule.FieldSpec;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFormat;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

/**
 * This class assists the user in entering serial numbers. <BR>
 * <BR>
 * Serial numbers, license number, CD keys and the like are often lenghty alpha-numerical numbers.
 * In many cases they are devided into multiple parts by dash or point separators. Entering these in
 * a single text field can be a frustrating experience for the user. This class provides a way of
 * presenting the user with an assembly of input fields that are arranged in the same way as the
 * key, with the separators already in place. Immideate testing for format compliance if performed
 * ans soon as each field is completed. In addition, the cursor is automatically advanced to make
 * entering numbers as painless as possible. <br>
 * <br>
 * <b>Formatting:</b>
 * <p/>
 * <ul>
 * <li><code>N:X:Y </code>- numeric field, accepts digits only
 * <li><code>H:X:Y </code>- hex field, accepts only hexadecimal digits
 * <li><code>A:X:Y </code>- alpha field, accepts only letters, no digits
 * <li><code>AN:X:Y</code>- alpha-numeric field, accepts digits and letters
 * </ul>
 * <b>Example:</b> <br>
 * <br>
 * <code>"N:4:4 - H:6:6 - AN:3:3 x A:5:5"</code><br>
 * <br>
 * This formatting string will produce a serial number field consisting of four separate input
 * fields. The fisrt input field will accept four numeric digits, the second six hexa-decimal
 * digits, the third three alpha-numeric digits and the fourth five letters. The first three input
 * fields will be separated by '-' and the third and fourth by 'x'. The following snapshot was
 * obtained with this setting: <br>
 * <br>
 * <img src="doc-files/RuleInputField-1.gif"/>
 *
 * @author Elmar Grom
 * @version 0.0.1 / 10/19/02
 */
public class RuleInputField extends JComponent implements KeyListener, FocusListener, CaretListener
{
    private static final long serialVersionUID = 3832616275124958257L;

    private final RuleField field;

    private final InstallData installData;

    /**
     * The input fields, in the order in which they appear on the screen.
     */
    private List<JTextField> inputFields = new ArrayList<JTextField>();

    private RuleTextField activeField;

    private boolean backstep = false;

    private static final Logger logger = Logger.getLogger(RuleInputField.class.getName());


    /**
     * Constructs a {@code RuleInputField}.
     *
     * @param field       the field
     * @param installData the installation data
     */
    public RuleInputField(RuleField field, InstallData installData)
    {
        this.field = field;
        this.installData = installData;

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(com.izforge.izpack.gui.FlowLayout.LEFT);
        setLayout(layout);

        // ----------------------------------------------------
        // create the fields and field separators
        // ----------------------------------------------------
        createItems();

        String preset = field.getDefaultValue();
        if (preset != null && preset.length() > 0)
        {
            setFields(preset);
        }

        // ----------------------------------------------------
        // set the focus to the first field
        // ----------------------------------------------------
        activeField = (RuleTextField) inputFields.get(0);
        activeField.grabFocus();
    }

    public List<JTextField> getInputFields()
    {
        return inputFields;
    }

    /**
     * Returns the field contents, formatted according to its {@link RuleFormat}.
     *
     * @return the field contents
     */
    public String getText()
    {
        String[] values = getValues();
        return field.format(values);
    }

    /**
     * Returns the field values.
     *
     * @return the field values
     */
    public String[] getValues()
    {
        String[] values = new String[inputFields.size()];
        for (int i = 0; i < inputFields.size(); ++i)
        {
            values[i] = inputFields.get(i).getText();
        }
        return values;
    }

    /**
     * Creates the items that make up this field. All fields are stored in <code>inputFields</code>.
     */
    private void createItems()
    {
        FieldLayout layout = field.getLayout();
        for (Object item : layout.getLayout())
        {
            if (item instanceof FieldSpec)
            {
                FieldSpec spec = (FieldSpec) item;
                JTextField field = new RuleTextField(spec);

                inputFields.add(field);
                field.addFocusListener(this);
                field.addKeyListener(this);
                field.addCaretListener(this);
                add(field);
            }
            else
            {
                add(new JLabel((String) item));
            }
        }
    }

    /**
     * Sets each field to a pre-defined value.
     *
     * @param data a <code>String</code> containing the preset values for each field. The format
     *             of the string is as follows: The content for the individuals fields must be separated by
     *             whitespace. Each installDataGUI block is preceeded by the index of the field to set (counting starts at
     *             0) followed by a colon ':'and after that the actual installDataGUI for the field.
     */
    private void setFields(String data)
    {
        StringTokenizer tokenizer = new StringTokenizer(data);
        String token;
        String indexString;
        int index;
        boolean process = false;

        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();
            indexString = token.substring(0, token.indexOf(':'));

            try
            {
                index = Integer.parseInt(indexString);
                if (index < inputFields.size())
                {
                    String val = token.substring((token.indexOf(':') + 1), token.length());
                    String className = "";
                    if (val.contains(":"))
                    {
                        className = val.substring(val.indexOf(":") + 1);
                        val = val.substring(0, val.indexOf(":"));
                    }

                    if (!"".equals(className) && !process)
                    {
                        process = true;
                    }
                    val = installData.getVariables().replace(val);
                    inputFields.get(index).setText(val);
                }
            }
            catch (Throwable exception)
            {
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }

        if (process)
        {
            tokenizer = new StringTokenizer(data);
            while (tokenizer.hasMoreTokens())
            {
                token = tokenizer.nextToken();
                indexString = token.substring(0, token.indexOf(':'));

                try
                {
                    index = Integer.parseInt(indexString);
                    if (index < inputFields.size())
                    {
                        String val = token.substring((token.indexOf(':') + 1), token.length());
                        String className = "";
                        String presult = "";
                        if (val.contains(":"))
                        {
                            className = val.substring(val.indexOf(":") + 1);
                        }

                        if (!"".equals(className))
                        {
                            Processor processor = (Processor) Class.forName(className).newInstance();
                            presult = processor.process(new ValuesProcessingClient(getValues()));
                        }
                        String[] td = new RE("\\*").split(presult);
                        inputFields.get(index).setText(td[index]);
                    }
                }
                catch (Throwable exception)
                {
                    logger.log(Level.WARNING, exception.getMessage(), exception);
                }
            }
        }
    }

    /*---------------------------------------------------------------------------*
     Implementation for KeyListener
     *---------------------------------------------------------------------------*/

    /**
     * This method is invoked when a key has been typed. The event occurs when a key press is
     * followed by a key release.
     *
     * @param event the key event forwarded by the system.
     */
    @Override
    public void keyTyped(KeyEvent event)
    {
    }

    /**
     * This method is invoked when a key has been pressed. This method verifies the condition of the
     * input field in focus. Once the column count in the field has reached the specified maximum,
     * the rule specified for the field in question is invoked. In case the test result is positive,
     * focus is set to the next field. If hte test result is negative, the field content is marked
     * and the caret set to the start of the field.
     *
     * @param event the key event forwarded by the system.
     */
    @Override
    public void keyPressed(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
        {
            int caretPosition = activeField.getCaretPosition();

            if (caretPosition == 0)
            {
                int activeIndex = inputFields.indexOf(activeField);

                if (activeIndex > 0)
                {
                    activeIndex--;
                    backstep = true;
                    activeField = (RuleTextField) inputFields.get(activeIndex);
                    activeField.grabFocus();
                }
            }
        }
    }

    /**
     * This method is invoked when a key has been released.
     *
     * @param event the key event forwarded by the system.
     */
    @Override
    public void keyReleased(KeyEvent event)
    {
    }


    /*---------------------------------------------------------------------------*
     Implementation for FocusListener
     *---------------------------------------------------------------------------*/

    /**
     * Invoked when a component gains the keyboard focus.
     *
     * @param event the focus event forwardes by the sytem.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design <- keep this tag in place and don't write on this line!
     *
     * Enter design related documentation here.
     * --------------------------------------------------------------------------
     */
    @Override
    public void focusGained(FocusEvent event)
    {
        activeField = (RuleTextField) event.getSource();

        if (backstep)
        {
            activeField.setCaretPosition(activeField.getText().length());
            backstep = false;
        }
        else
        {
            activeField.selectAll();
        }
    }

    /**
     * Invoked when a component loses the keyboard focus. This method does nothing, we are only
     * interested in 'focus gained' events.
     *
     * @param event the focus event forwardes by the sytem.
     */
    @Override
    public void focusLost(FocusEvent event)
    {
    }


    /*---------------------------------------------------------------------------*
     Implementation for CaretListener
     *---------------------------------------------------------------------------*/

    /**
     * Called when the caret position is updated.
     *
     * @param event the caret event received from the text field
     */
    @Override
    public void caretUpdate(CaretEvent event)
    {
        if (activeField != null)
        {
            String text = activeField.getText();
            int fieldSize = activeField.getEditLength();
            int caretPosition = activeField.getCaretPosition();
            int selection = activeField.getSelectionEnd() - activeField.getSelectionStart();

            if ((!inputFields.get(inputFields.size() - 1).equals(activeField)) && (!activeField.unlimitedEdit()))
            {
                if ((text.length() == fieldSize) && (selection == 0)
                        && (caretPosition == fieldSize) && !backstep)
                {
                    activeField.transferFocus();
                }
            }
        }
    }

}
