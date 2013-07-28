package com.izforge.izpack.panels.installationgroup;

import com.izforge.izpack.api.data.Pack;

import java.util.HashSet;
import java.util.Map;

public class GroupData
{
    static final long ONEK = 1024;
    static final long ONEM = 1024 * 1024;
    static final long ONEG = 1024 * 1024 * 1024;

    String name;
    String description;
    String sortKey;
    long size;
    HashSet<String> packNames = new HashSet<String>();

    GroupData(String name, String description, String sortKey)
    {
        this.name = name;
        this.description = description;
        this.sortKey = sortKey;
    }

    String getSizeString()
    {
        String s;
        if (size < ONEK)
        {
            s = size + " bytes";
        }
        else if (size < ONEM)
        {
            s = size / ONEK + " KB";
        }
        else if (size < ONEG)
        {
            s = size / ONEM + " MB";
        }
        else
        {
            s = size / ONEG + " GB";
        }
        return s;
    }

    public void addDependents(Pack p, Map<String, Pack> packsByName)
    {
        packNames.add(p.getName());
        size += p.getSize();
        if (p.getDependencies() == null || p.getDependencies().size() == 0)
        {
            return;
        }

        for (String dependent : p.getDependencies())
        {
            if (!packNames.contains(dependent))
            {
                Pack dependentPack = packsByName.get(dependent);
                addDependents(dependentPack, packsByName);
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuffer tmp = new StringBuffer("GroupData(");
        tmp.append(name);
        tmp.append("){description=");
        tmp.append(description);
        tmp.append(", sortKey=");
        tmp.append(sortKey);
        tmp.append(", size=");
        tmp.append(size);
        tmp.append(", sizeString=");
        tmp.append(getSizeString());
        tmp.append(", packNames=");
        tmp.append(packNames);
        tmp.append("}");
        return tmp.toString();
    }
}
