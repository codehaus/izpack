package com.izforge.izpack.api.jaxb.adapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;

public class MapEntryType {

   @XmlAttribute
   public String key;

   @XmlElement
   public Object value;

   //FIXME isthe following really needed???

   public MapEntryType() {
       //Required by JAXB
   }

   public MapEntryType(String key, Object value) {
       this.key   = key;
       this.value = value;
   }
}