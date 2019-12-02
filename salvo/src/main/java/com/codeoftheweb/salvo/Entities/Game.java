package com.codeoftheweb.salvo.Entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Game {

  // ID automatico para la tabla "games"
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  // Relacion con la tabla "gamePlayers"
  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
  private Set<GamePlayer> gamePlayers;

  // Relacion con la tabla "scores"
  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
  private Set<Score> scores;

  private Date creationDate;

  public Game() {
    this.creationDate = new Date();
  }

  public Game(long seconds) {
    seconds = (Math.abs(seconds) > 11*3600 ? 0 : seconds);
    this.creationDate = Date.from(new Date().toInstant().plusSeconds(seconds));
  }

  public Date getCreationDate() {
    return this.creationDate;
  }

  public long getId() {
    return this.id;
  }

  public List<Player> getPlayers() {
    return this.getGamePlayers().stream()
        .map(gp -> gp.getPlayer()).collect(Collectors.toList());
  }

  public Set<GamePlayer> getGamePlayers(){
    return this.gamePlayers
        .stream()
        .sorted((gp1,gp2) -> (int)(gp1.getId() - gp2.getId()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Set<Score> getScores(){
    return this.scores
        .stream()
        .sorted((score1,score2) -> (int)(score1.getId() - score2.getId()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public int getStatus(){
    int status = 0; // Inicializa status = OPENED

    if(this.getPlayers().size() > 1){
      status = 1; // Si hay 2 players -> status = COMPLETED

      GamePlayer[] gps = this.getGamePlayers().toArray(new GamePlayer[0]);
      if(gps[0].isFull() && gps[1].isFull()) {
        status = 2; // Si ambos gamePlayers tienen flota completa -> status = READY
      }
    }

    if(this.getScores().size() != 0){
      status = 3; // Si hay scores -> status = CLOSED
    }
    return status;
  }

  // Salida DTO para los objetos Game
  public Map<String, Object> toDTO() {
    Map<String, Object> dto = new LinkedHashMap<>();

    dto.put("id", this.id);
    dto.put("created", this.creationDate);
    dto.put("status", this.getStatus());
    dto.put("gamePlayers", this.getGamePlayers()
        .stream()
        .map(gp -> gp.toDTO()));
    return dto;
  }

}
