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
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.check.CheckFieldReader;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.combo.ComboFieldReader;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.divider.DividerReader;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.DirFieldReader;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldReader;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileFieldReader;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupFieldReader;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.radio.RadioFieldReader;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFieldReader;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.search.SearchFieldReader;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.text.TextFieldReader;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.panels.userinput.field.title.TitleFieldReader;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Factory for {@link Field}s.
 *
 * @author Tim Anderson
 */
public class FieldFactory
{

    /**
     * The field types.
     */
    enum Type
    {
        CHECK, COMBO, DIR, DIVIDER, FILE, MULTIFILE, PASSWORD, RADIO, RULE, SPACE, SEARCH, STATICTEXT, TEXT, TITLE
    }

    /**
     * Creates a new field.
     *
     * @param element     the field element
     * @param config      the configuration
     * @param installData the installation data
     * @param matcher     the platform-model matcher
     * @return a new field
     * @throws IzPackException if the field is invalid
     */
    public Field create(IXMLElement element, Config config, InstallData installData, PlatformModelMatcher matcher)
    {
        Field result;
        Type type;
        String value = config.getAttribute(element, "type");
        try
        {
            type = Type.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new IzPackException("Invalid field type: " + value + " in " + config.getContext(element));
        }
        switch (type)
        {
            case CHECK:
                result = new CheckField(new CheckFieldReader(element, config));
                break;
            case COMBO:
                result = new ComboField(new ComboFieldReader(element, config, installData));
                break;
            case DIR:
                result = new DirField(new DirFieldReader(element, config));
                break;
            case DIVIDER:
                result = new Divider(new DividerReader(element, config));
                break;
            case FILE:
                result = new FileField(new FileFieldReader(element, config));
                break;
            case MULTIFILE:
                result = new MultipleFileField(new MultipleFileFieldReader(element, config));
                break;
            case PASSWORD:
                result = new PasswordGroupField(new PasswordGroupFieldReader(element, config));
                break;
            case RADIO:
                result = new RadioField(new RadioFieldReader(element, config));
                break;
            case RULE:
                result = new RuleField(new RuleFieldReader(element, config));
                break;
            case SEARCH:
                result = new SearchField(new SearchFieldReader(element, config, matcher));
                break;
            case SPACE:
                result = new Spacer(new SimpleFieldReader(element, config));
                break;
            case STATICTEXT:
                result = new StaticText(new SimpleFieldReader(element, config));
                break;
            case TEXT:
                result = new TextField(new TextFieldReader(element, config));
                break;
            case TITLE:
                result = new TitleField(new TitleFieldReader(element, config));
                break;
            default:
                throw new IzPackException("Unsupported field type: " + value + " in " + config.getContext(element));
        }
        return result;
    }
}
