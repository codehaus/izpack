package com.izforge.izpack.api.jaxb.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter class responsible for converting between instances of the unmappable and mappable classes
 * during JAXB (un)marshalling.
 */
public final class MapAdapter extends XmlAdapter<MapType<String,Object>,Map<String,Object>> {

   @Override
   public MapType<String,Object> marshal(Map<String,Object> map) throws Exception {
      MapType<String,Object> mapType = new MapType<String,Object>();
      for(Entry<String,Object> entry : map.entrySet())
      {
         MapEntryType mapEntryType = new MapEntryType(entry.getKey(), entry.getValue());
         mapType.entry.add(mapEntryType);
      }
      return mapType;
   }

   @Override
   public Map<String,Object> unmarshal(MapType<String,Object> arg0) throws Exception {
      HashMap<String,Object> hashMap = new HashMap<String,Object>();
      for(MapEntryType entryType : arg0.entry) {
         hashMap.put(entryType.key, entryType.value);
      }
      return hashMap;
   }

}