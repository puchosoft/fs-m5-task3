package com.codeoftheweb.salvo.Entities;

import java.util.*;

public class ShipsValidation {

  private static ArrayList<Map<String, Object>> shipTypes = init();

  private static ArrayList<Map<String, Object>> init(){
    ArrayList<Map<String, Object>> sT = new ArrayList();
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

  public static ArrayList<Map<String, Object>> getShipTypes(){
    return shipTypes;
  }

  public static int getTotalShipsQuantity(){
    return shipTypes.stream().mapToInt(st -> (int)st.get("quantity")).sum();
  }

  public static int getShipLength(String type){
    return (int) shipTypes.stream().filter(st -> type.equals(st.get("type"))).findAny().get().get("length");
  }

  public static int getShipQuantity(String type){
    return (int) shipTypes.stream().filter(st -> type.equals(st.get("type"))).findAny().get().get("quantity");
  }

}
