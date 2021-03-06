/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Jan Blok
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

package com.izforge.izpack.api.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.data.binding.Action;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.data.binding.OsModel;

/**
 * @author Jan Blok
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class Panel implements Serializable
{

    static final long serialVersionUID = 8886445274940938809L;

    /**
     * The panel classname.
     */
    private String className;

    /**
     * The target operation system of this panel
     */
    private List<OsModel> osConstraints = Collections.emptyList();

    /**
     * the unique id of this panel
     */
    private String panelId;

    /**
     * condition for this panel
     */
    private String condition;

    /**
     * The list of validators for this panel
     */
    private List<String> validators = new ArrayList<String>();

    /**
     * The map of validator conditions for this panel depending on the validator
     * Condition whether the validator has to be asked for validation.
     */
    private Map<Integer, String> validatorConditionIds = new HashMap<Integer, String>();


    private List<Action> actions;

    /**
     * Whether the panel has been visited for summarizing the installation story
     */
    private transient boolean visited = false;

    /**
     * list of all pre panel construction actions
     */
    private List<PanelActionConfiguration> preConstructionActions = null;

    /**
     * list of all pre panel activation actions
     */
    private List<PanelActionConfiguration> preActivationActions = null;

    /**
     * list of all pre panel validation actions
     */
    private List<PanelActionConfiguration> preValidationActions = null;

    /**
     * list of all post panel validation actions
     */
    private List<PanelActionConfiguration> postValidationActions = null;

    /**
     * A HashMap for URLs to Helpfiles, key should be iso3-code
     */
    private List<Help> helps = null;

    /**
     * Contains configuration values for a panel.
     */
    private Map<String, String> configuration = null;

    public String getClassName()
    {
        return this.className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public boolean hasPanelId()
    {
        return (panelId != null);
    }

    public String getPanelId()
    {
        return panelId;
    }

    public void setPanelId(String panelId)
    {
        this.panelId = panelId;
    }

    @Deprecated
    public String getPanelid()
    {
        return getPanelId();
    }

    @Deprecated
    public void setPanelid(String panelId)
    {
        setPanelId(panelId);
    }

    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }

    /**
     * Get validator and validator condition entries for this panel
     * @return Returns a list of validator class names and optional conditions defining
     *  whether the panel validator should be asked at all.
     */
    public List<String> getValidators()
    {
        return validators;
    }

    /**
     * Gets a validator condition
     * @param index
     * @return the validator condition of a validator at the given index for this panel
     */
    public String getValidatorCondition(int index)
    {
        return this.validatorConditionIds.get(Integer.valueOf(index));
    }

    /**
     * Adds a panel validator and a condition defining whether the panel validator should be asked at all.
     * @param validatorClassName
     * @param validatorConditionId the validator condition for this panel (set null for no condition)
     */
    public void addValidator(String validatorClassName, String validatorConditionId)
    {
        this.validators.add(validatorClassName);
        if (validatorConditionId != null)
        {
            // There must be used the index in the ordered list of validators as key, because the validator
            // has no own ID and its classname might not be unique.
            this.validatorConditionIds.put(Integer.valueOf(validators.size()-1), validatorConditionId);
        }
    }

    public List<Help> getHelps()
    {
        return helps;
    }

    public void setHelps(List<Help> helps)
    {
        this.helps = helps;
    }

    public List<PanelActionConfiguration> getPreConstructionActions()
    {
        return preConstructionActions;
    }

    public void addPreConstructionAction(PanelActionConfiguration action)
    {
        if (this.preConstructionActions == null)
        {
            this.preConstructionActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preConstructionActions.add(action);
    }

    public List<PanelActionConfiguration> getPreActivationActions()
    {
        return preActivationActions;
    }

    public void addPreActivationAction(PanelActionConfiguration action)
    {
        if (this.preActivationActions == null)
        {
            this.preActivationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preActivationActions.add(action);
    }

    public List<PanelActionConfiguration> getPreValidationActions()
    {
        return preValidationActions;
    }

    public void addPreValidationAction(PanelActionConfiguration action)
    {
        if (this.preValidationActions == null)
        {
            this.preValidationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preValidationActions.add(action);
    }

    public List<PanelActionConfiguration> getPostValidationActions()
    {
        return postValidationActions;
    }

    public void addPostValidationAction(PanelActionConfiguration action)
    {
        if (this.postValidationActions == null)
        {
            this.postValidationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.postValidationActions.add(action);
    }

    public boolean hasConfiguration()
    {
        return this.configuration != null;
    }

    public void addConfiguration(String key, String value)
    {
        if (this.configuration == null)
        {
            this.configuration = new HashMap<String, String>();
        }
        this.configuration.put(key, value);
    }

    public String getConfiguration(String key)
    {
        String result = null;
        if (this.configuration != null)
        {
            result = this.configuration.get(key);
        }
        return result;
    }

    public List<OsModel> getOsConstraints()
    {
        return osConstraints;
    }

    public void setOsConstraints(List<OsModel> osConstraints)
    {
        this.osConstraints = osConstraints;
    }

    public String getHelpUrl(String localeISO3)
    {
        if (helps == null)
        {
            return null;
        }
        for (Help help : helps)
        {
            if (help.getIso3().equals(localeISO3))
            {
                return help.getSrc();
            }
        }
        return null;
    }

    public List<Action> getActions()
    {
        return actions;
    }

    public void setActions(List<Action> actions)
    {
        this.actions = actions;
    }

    @Override
    public String toString()
    {
        return "Panel{" +
                "className='" + className + '\'' +
                ", osConstraints=" + osConstraints +
                ", panelid='" + getPanelId() + '\'' +
                ", condition='" + condition + '\'' +
                ", actions=" + actions +
                ", validator count='" + validators.size() + '\'' +
                ", helps=" + helps +
                '}';
    }

    /**
     * @return Whether the panel has been visited for summarizing the installation story
     */
    public boolean isVisited()
    {
        return visited;
    }

    /**
     * Mark panel visited for summarizing the installation story
     * @param visited
     */
    public void setVisited(boolean visited)
    {
        this.visited = visited;
    }
}
