package com.codeoftheweb.salvo.Entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Entity
public class Ship {

  // ID automatico para la tabla "ships"
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  private String shipType;

  // Relacion con la tabla "gamePlayers"
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gamePlayer_id")
  private GamePlayer gamePlayer;

  @ElementCollection
  @Column(name="location")
  private Set<String> locations = new TreeSet<>();

  public Ship(){
  }

  public Ship(String shipType, GamePlayer gamePlayer, Set<String> locations){
    this.shipType = shipType;
    this.gamePlayer = gamePlayer;
    this.locations = new TreeSet<>(locations);

  }

  public long getId() {
    return this.id;
  }

  public String getShipType() {
    return this.shipType;
  }

  public GamePlayer getGamePlayer() {
    return this.gamePlayer;
  }

  public Set<String> getLocations(){
    return locations;
  }

  // Salida DTO para los objetos Ship
  public Map<String, Object> toDTO() {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("type", this.shipType);
    dto.put("locations", this.locations);
    return dto;
  }

  public boolean isValid(){
    // Si alguna ubicacion cae fuera de la grilla
    boolean outOfRange = this.locations.stream()
      .filter( loc -> loc.charAt(0) < 'A'  || loc.charAt(0) > 'J' || Integer.valueOf(loc.substring(1)) < 1 || Integer.valueOf(loc.substring(1)) > 10)
      .count() != 0;
    if(outOfRange){
      return false;
    }

    Set<String> rows = this.locations.stream().map(loc -> loc.substring(0,1)).sorted().collect(toSet());
    Set<Integer> cols = this.locations.stream().map(loc -> Integer.valueOf(loc.substring(1))).sorted().collect(toSet());

    // Si hay mas de una columna y mas de una fila al mismo tiempo (hay alguna diagonal)
    if((rows.size()>1) && (cols.size()>1)){
      return false;
    }

    int shipSize = ShipsValidation.getShipLength(shipType);

    // Si la nave tiene una cantidad de ubicaciones distinta que la longitud maxima
    if((rows.size()>shipSize) || (cols.size()>shipSize) || (cols.size()<shipSize && rows.size()==1) || (rows.size()<shipSize && cols.size()==1)){
      return false;
    }

    int maxRow = rows.stream().max(Comparator.comparing(String::valueOf)).get().charAt(0);
    int minRow = rows.stream().min(Comparator.comparing(String::valueOf)).get().charAt(0);
    int maxCol = cols.stream().max(Comparator.comparingInt(Integer::valueOf)).get();
    int minCol = cols.stream().min(Comparator.comparingInt(Integer::valueOf)).get();

    // Si la separacion de las ubicaciones es mayor que la longitud maxima
    if((maxRow - minRow +1)>shipSize || (maxCol-minCol+1)>shipSize){
      return false;
    }

    return true;
  }

}
