package com.izforge.izpack.api.jaxb.wrapper;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.izforge.izpack.api.jaxb.adapter.MapAdapter;
import com.izforge.izpack.api.rules.base.Condition;

@XmlRootElement(name = "rules-wrapper")
public class RulesWrapper implements JAXBWrapper
{

    private Map<String, Condition> rules;

    public RulesWrapper(Map<String, Condition> rules)
    {
        super();
        this.rules = rules;
    }

    public RulesWrapper()
    {
        rules = new HashMap<String, Condition>();
    }

    @XmlJavaTypeAdapter(MapAdapter.class)
    public Map<String, Condition> getRules()
    {
        return rules;
    }

    public void setRules(Map<String, Condition> rules)
    {
        this.rules = rules;
    }

    @Override
    public final Class<?> getImplementingInterface()
    {
        return Condition.class;
    }

    @Override
    public Package[] getPackages()
    {
        return new Package[] {
                Package.getPackage("com.izforge.izpack.api.rules.base"),
                Package.getPackage("com.izforge.izpack.core.rules.logic"),
                Package.getPackage("com.izforge.izpack.core.rules.process")
                };
    }



}