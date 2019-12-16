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
    // ¿Tiene 2 jugadores?
    boolean hasTwoPlayers = this.getPlayers().size() > 1;

    // ¿Tiene todas la naves completas?
    boolean hasAllTheShips = this.getGamePlayers().stream()
      .filter(gp -> !gp.isFullOfShips())
      .count() == 0;

    // ¿Tiene disparos?
    boolean hasSalvoes = this.getGamePlayers().stream()
        .filter(gp -> gp.getSalvoes().size() > 0)
        .count() > 0;

    // ¿Tiene puntajes?
    boolean hasScores = this.getScores().size() > 0;

    if (hasScores) return 3; // Si tiene puntajes -> status = CLOSED
    if (hasSalvoes || hasTwoPlayers && hasAllTheShips) return 2; // Si tiene disparos o tiene todas las naves -> status = READY
    if (hasAllTheShips) return 1; // Si tiene todas las naves pero 1 jugador -> status = WAITING

    return 0; // status = OPENED
  }

  // Salida DTO para los objetos Game
  public Map<String, Object> toDTO() {
    Map<String, Object> dto = new LinkedHashMap<>();

    dto.put("id", this.id);
    dto.put("created", this.creationDate);
    dto.put("gamePlayers", this.getGamePlayers()
        .stream()
        .map(gp -> gp.toDTO()));
    return dto;
  }

}
