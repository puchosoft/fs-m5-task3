package com.codeoftheweb.salvo.Entities;

import java.util.*;



public class ShipsValidation {

  private static Set<Map<String, Object>> shipTypes = init();

  private static Set<Map<String, Object>> init(){
    Set<Map<String, Object>> sT = new HashSet();
    sT.add(getMap("Aircraft Carrier", 5, 1));
    sT.add(getMap("Battleship", 4, 1));
    sT.add(getMap("Submarine", 3, 1));
    sT.add(getMap("Destroyer", 3, 1));
    sT.add(getMap("Patrol Boat", 2, 1));
    return sT;
  }

  private static Map<String, Object> getMap(String t, int l, int q){
    Map<String, Object> map = new HashMap<>();
    map.put("type",t);
    map.put("length", l);
    map.put("quantity", q);
    return map;
  }

  public static Set<Map<String, Object>> getShipTypes(){
    return shipTypes;
  }
}
