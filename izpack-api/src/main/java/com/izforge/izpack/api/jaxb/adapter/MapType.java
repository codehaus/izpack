package com.izforge.izpack.api.jaxb.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class MapType<K,V> {

   @XmlElement(name="entry")
   public List<MapEntryType> entry =
      new ArrayList<MapEntryType>();

}