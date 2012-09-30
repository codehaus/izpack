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

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.title.TitleField;


/**
 * Factory for {@link GUIFieldView}s.
 *
 * @author Tim Anderson
 */
public class GUIFieldViewFactory
{

    /**
     * The installation data.
     */
    private final GUIInstallData installData;

    /**
     * The parent panel.
     */
    private final IzPanel parent;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;


    /**
     * Constructs a {@code GUIFieldViewFactory}.
     *
     * @param installData the installation data
     * @param parent      the parent panel
     * @param frame       the installer frame
     */
    public GUIFieldViewFactory(GUIInstallData installData, IzPanel parent, InstallerFrame frame)
    {
        this.installData = installData;
        this.parent = parent;
        this.frame = frame;
    }

    /**
     * Creates a view to display the supplied field.
     *
     * @param field the field
     * @return the view to display the field
     * @throws IzPackException if the view cannot be created
     */
    public GUIFieldView create(Field field)
    {
        GUIFieldView result;
        if (field instanceof RuleField)
        {
            result = new GUIRuleFieldView((RuleField) field, parent.getToolkit(), installData);
        }
        else if (field instanceof TextField)
        {
            result = new GUITextFieldView((TextField) field);
        }
        else if (field instanceof ComboField)
        {
            result = new GUIComboFieldView((ComboField) field);
        }
        else if (field instanceof RadioField)
        {
            result = new GUIRadioFieldView((RadioField) field, installData);
        }
        else if (field instanceof PasswordGroupField)
        {
            result = new GUIPasswordGroupFieldView((PasswordGroupField) field);
        }
        else if (field instanceof Spacer)
        {
            result = new GUISpacerView((Spacer) field);
        }
        else if (field instanceof Divider)
        {
            result = new GUIDividerView((Divider) field);
        }
        else if (field instanceof CheckField)
        {
            result = new GUICheckFieldView((CheckField) field);
        }
        else if (field instanceof StaticText)
        {
            result = new GUIStaticTextView((StaticText) field);
        }
        else if (field instanceof TitleField)
        {
            result = new GUITitleFieldView((TitleField) field, installData, frame.getIcons());
        }
        else if (field instanceof SearchField)
        {
            result = new GUISearchFieldView((SearchField) field, installData, frame);
        }
        else if (field instanceof FileField)
        {
            result = new GUIFileFieldView((FileField) field, installData, parent);
        }
        else if (field instanceof DirField)
        {
            result = new GUIDirFieldView((DirField) field, installData, parent);
        }
        else if (field instanceof MultipleFileField)
        {
            result = new GUIMultipleFileFieldView((MultipleFileField) field, installData, frame);
        }
        else
        {
            throw new IzPackException("Unsupported field type: " + field.getClass().getName());

        }
        return result;
    }
}
