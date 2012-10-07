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

package com.izforge.izpack.panels.userinput.rule;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.rule.check.CheckField;
import com.izforge.izpack.panels.userinput.rule.check.CheckFieldReader;
import com.izforge.izpack.panels.userinput.rule.combo.ComboField;
import com.izforge.izpack.panels.userinput.rule.combo.ComboFieldReader;
import com.izforge.izpack.panels.userinput.rule.divider.Divider;
import com.izforge.izpack.panels.userinput.rule.divider.DividerReader;
import com.izforge.izpack.panels.userinput.rule.file.DirField;
import com.izforge.izpack.panels.userinput.rule.file.DirFieldReader;
import com.izforge.izpack.panels.userinput.rule.file.FileField;
import com.izforge.izpack.panels.userinput.rule.file.FileFieldReader;
import com.izforge.izpack.panels.userinput.rule.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.rule.file.MultipleFileFieldReader;
import com.izforge.izpack.panels.userinput.rule.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.rule.password.PasswordGroupFieldReader;
import com.izforge.izpack.panels.userinput.rule.radio.RadioField;
import com.izforge.izpack.panels.userinput.rule.radio.RadioFieldReader;
import com.izforge.izpack.panels.userinput.rule.rule.RuleField;
import com.izforge.izpack.panels.userinput.rule.rule.RuleFieldReader;
import com.izforge.izpack.panels.userinput.rule.search.SearchField;
import com.izforge.izpack.panels.userinput.rule.search.SearchFieldReader;
import com.izforge.izpack.panels.userinput.rule.space.Spacer;
import com.izforge.izpack.panels.userinput.rule.statictext.StaticText;
import com.izforge.izpack.panels.userinput.rule.text.TextField;
import com.izforge.izpack.panels.userinput.rule.text.TextFieldReader;
import com.izforge.izpack.panels.userinput.rule.title.TitleField;
import com.izforge.izpack.panels.userinput.rule.title.TitleFieldReader;
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
     * The configuration.
     */
    private final Config config;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;


    /**
     * Constructs a {@code FieldFactory}.
     *
     * @param config      the configuration
     * @param installData the installation data
     * @param matcher     the platform-model matcher
     */
    public FieldFactory(Config config, InstallData installData, PlatformModelMatcher matcher)
    {
        this.config = config;
        this.installData = installData;
        this.matcher = matcher;
    }

    /**
     * Creates a new field.
     *
     * @param element the field element
     * @return a new field
     * @throws IzPackException if the field is invalid
     */
    public Field create(IXMLElement element)
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
                result = new CheckField(new CheckFieldReader(element, config), installData);
                break;
            case COMBO:
                result = new ComboField(new ComboFieldReader(element, config, installData), installData);
                break;
            case DIR:
                result = new DirField(new DirFieldReader(element, config), installData);
                break;
            case DIVIDER:
                result = new Divider(new DividerReader(element, config), installData);
                break;
            case FILE:
                result = new FileField(new FileFieldReader(element, config), installData);
                break;
            case MULTIFILE:
                result = new MultipleFileField(new MultipleFileFieldReader(element, config), installData);
                break;
            case PASSWORD:
                result = new PasswordGroupField(new PasswordGroupFieldReader(element, config), installData);
                break;
            case RADIO:
                result = new RadioField(new RadioFieldReader(element, config), installData);
                break;
            case RULE:
                result = new RuleField(new RuleFieldReader(element, config), installData, config.getFactory());
                break;
            case SEARCH:
                result = new SearchField(new SearchFieldReader(element, config, matcher), installData);
                break;
            case SPACE:
                result = new Spacer(new SimpleFieldReader(element, config), installData);
                break;
            case STATICTEXT:
                result = new StaticText(new SimpleFieldReader(element, config), installData);
                break;
            case TEXT:
                result = new TextField(new TextFieldReader(element, config), installData);
                break;
            case TITLE:
                result = new TitleField(new TitleFieldReader(element, config), installData);
                break;
            default:
                throw new IzPackException("Unsupported field type: " + value + " in " + config.getContext(element));
        }
        return result;
    }
}
