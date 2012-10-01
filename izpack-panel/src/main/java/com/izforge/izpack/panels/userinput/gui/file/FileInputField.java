/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * Copyright 2009 Dennis Reil
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

package com.izforge.izpack.panels.userinput.gui.file;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;

public class FileInputField extends JPanel implements ActionListener
{
    private static final long serialVersionUID = 4673684743657328492L;

    private static final transient Logger logger = Logger.getLogger(FileInputField.class.getName());

    private final AbstractFileField field;

    InstallerFrame parentFrame;

    IzPanel parent;

    JTextField filetxt;

    JButton browseBtn;

    String set;

    GUIInstallData installDataGUI;

    boolean allowEmpty;

    protected static final int INVALID = 0, EMPTY = 1;

    public FileInputField(AbstractFileField field, IzPanel parent, GUIInstallData installDataGUI)
    {
        this.field = field;
        this.parent = parent;
        this.parentFrame = parent.getInstallerFrame();
        this.installDataGUI = installDataGUI;
        this.set = field.getDefaultValue();
        setAllowEmptyInput(field.getAllowEmptyValue());
        this.initialize();
    }

    private void initialize()
    {
        int size = field.getSize() < 0 ? 0 : field.getSize();
        filetxt = new JTextField(field.getDefaultValue(), size);
        filetxt.setName(field.getVariable());
        filetxt.setCaretPosition(0);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints fileTextConstraint = new GridBagConstraints();
        GridBagConstraints fileButtonConstraint = new GridBagConstraints();
        fileTextConstraint.gridx = 0;
        fileTextConstraint.gridy = 0;
        fileTextConstraint.anchor = GridBagConstraints.WEST;
        fileTextConstraint.insets = new Insets(0, 0, 0, 5);
        fileButtonConstraint.gridx = 1;
        fileButtonConstraint.gridy = 0;
        fileButtonConstraint.anchor = GridBagConstraints.WEST;

        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(installDataGUI.getMessages().get("UserInputPanel.search.browse"),
                                               installDataGUI.buttonsHColor);
        browseBtn.addActionListener(this);
        this.add(filetxt, fileTextConstraint);
        this.add(browseBtn, fileButtonConstraint);
    }

    public void setFile(String filename)
    {
        filetxt.setText(filename);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn)
        {
            logger.fine("Show directory chooser");
            String initialPath = ".";
            if (filetxt.getText() != null)
            {
                initialPath = filetxt.getText();
            }
            JFileChooser filechooser = new JFileChooser(initialPath);
            prepareFileChooser(filechooser);

            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION)
            {
                String selectedFile = filechooser.getSelectedFile().getAbsolutePath();
                filetxt.setText(selectedFile);
                logger.fine("Setting current file chooser directory to: " + selectedFile);
            }
        }
    }

    protected Field getField()
    {
        return field;
    }

    protected void prepareFileChooser(JFileChooser filechooser)
    {
        filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String fileExtension = field.getFileExtension();
        String fileExtensionDescription = field.getFileExtensionDescription();
        if ((fileExtension != null) && (fileExtensionDescription != null))
        {
            UserInputFileFilter fileFilter = new UserInputFileFilter();
            fileFilter.setFileExt(fileExtension);
            fileFilter.setFileExtDesc(fileExtensionDescription);
            filechooser.setFileFilter(fileFilter);
        }
    }

    public File getSelectedFile()
    {
        File result = null;
        if (filetxt.getText() != null)
        {
            result = new File(filetxt.getText());
        }
        return result;
    }

    protected void showMessage(int k)
    {
        if (k == INVALID)
        {
            showMessage("file.notfile");
        }
        else if (k == EMPTY)
        {
            showMessage("file.nofile");
        }
    }

    protected void showMessage(String messageType)
    {
        JOptionPane.showMessageDialog(parentFrame, parentFrame.getMessages().get("UserInputPanel." + messageType
                                                                                         + ".message"),
                                      parentFrame.getMessages().get("UserInputPanel." + messageType + ".caption"),
                                      JOptionPane.WARNING_MESSAGE);
    }

    public boolean validateField()
    {
        boolean result = false;
        String input = filetxt.getText();

        boolean empty = (input == null) || (input.length() == 0);
        if (empty && allowEmpty)
        {
            result = true;
        }
        else if (!empty)
        {
            // Expand unix home reference
            if (input.startsWith("~"))
            {
                String home = System.getProperty("user.home");
                input = home + input.substring(1);
            }

            // Normalize the path
            File file = new File(input).getAbsoluteFile();
            input = file.toString();

            filetxt.setText(input);

            if (!_validate(file))
            {
                result = false;
                showMessage(INVALID);
            }
            else
            {
                ValidationStatus status = field.validate(input);
                if (!status.isValid())
                {
                    JOptionPane.showMessageDialog(parentFrame, status.getMessage(),
                                                  parentFrame.getMessages().get("UserInputPanel.error.caption"),
                                                  JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    result = true;
                }
            }
        }
        else
        {
            showMessage(EMPTY);
        }
        return result;
    }

    protected boolean _validate(File file)
    {
        return file.isFile();
    }

    public void setAllowEmptyInput(boolean allowEmpty)
    {
        this.allowEmpty = allowEmpty;
    }
}
