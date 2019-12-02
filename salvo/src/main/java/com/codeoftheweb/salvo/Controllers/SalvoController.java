package com.codeoftheweb.salvo.Controllers;

import com.codeoftheweb.salvo.Entities.Game;
import com.codeoftheweb.salvo.Entities.GamePlayer;
import com.codeoftheweb.salvo.Entities.Player;
import com.codeoftheweb.salvo.Entities.ShipsValidation;
import com.codeoftheweb.salvo.Repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.Repositories.GameRepository;
import com.codeoftheweb.salvo.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping("/api") // Todos los controladores cuelgan de /api
public class SalvoController {

  @Autowired
  private GameRepository gameRepo;

  @Autowired
  private GamePlayerRepository gamePlayerRepo;

  @Autowired
  private PlayerRepository playerRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  // Genera un JSON con la informacion de los games en la URL /api/games
  @RequestMapping(path = "/games", method = RequestMethod.GET)
  public Map<String, Object> getGameInfo(Authentication auth) {
    Map<String, Object> gameInfo = new LinkedHashMap<>();
    gameInfo.put("player", isGuest(auth)? null : getCurrentPlayer(auth).toDTO());
    gameInfo.put("games", gameRepo
        .findAll()
        .stream()
        .map(game -> game.toDTO())
        .collect(toList()));
    return gameInfo;
  }

  // Genera un nuevo game en el repositorio usando la URL /api/games
  @RequestMapping(path = "/games", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> createGame(Authentication auth){
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    long gpid=-1;
    Map<String, Object> json = new LinkedHashMap<>();

    if( !isGuest(auth) ){
      Player player = getCurrentPlayer(auth);
      Game game = new Game();
      gameRepo.save(game);
      GamePlayer gp = new GamePlayer(game, player);
      gamePlayerRepo.save(gp);
      gpid = gp.getId();
      status = HttpStatus.CREATED;
    }

    json.put("gpid", gpid);
    return new ResponseEntity<>(json, status);
  }

  // Genera un JSON con la informacion de un game especifico en la URL /api/game_view/nn
  @RequestMapping("/game_view/{gamePlayerId}")
  public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long gamePlayerId, Authentication auth) {
    GamePlayer gamePlayer = gamePlayerRepo.getOne(gamePlayerId);

    Map<String, Object> gameDTO = new LinkedHashMap<>();

    if(auth==null || gamePlayer.getPlayer().getId() != getCurrentPlayer(auth).getId()){
      gameDTO.put("Cheat", 401);
      return new ResponseEntity<>(gameDTO, HttpStatus.UNAUTHORIZED );
    }

    gameDTO = gamePlayer.getGame().toDTO();

    gameDTO.put("ships", gamePlayer.getShips()
        .stream()
        .map(ship -> ship.toDTO())
    );

    gameDTO.put("salvoes", gamePlayer.getGame().getGamePlayers()
        .stream()
        .map(game_gamePlayer -> game_gamePlayer.toSalvoDTO())
        .collect(toSet())
    );

    return new ResponseEntity<>(gameDTO,HttpStatus.OK);
  }

  private boolean isGuest(Authentication auth) {
    return auth == null || auth instanceof AnonymousAuthenticationToken;
  }

  private Player getCurrentPlayer(Authentication auth) {
    return isGuest(auth)? null : playerRepo.findByUsername(auth.getName());
  }

  @RequestMapping(path = "/players", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String username, @RequestParam String password, Authentication auth){
    Map<String, Object> map = new LinkedHashMap<>();
    HttpStatus status;

    if(!isGuest(auth)){ // Si ya hay un usuario conectado ...
      map.put("error", "User logged in ");
      status = HttpStatus.CONFLICT;
    }
    else if (username.isEmpty()){ // Si el username está vacio
      map.put("error", "No name");
      status = HttpStatus.EXPECTATION_FAILED;
    }
    else if (playerRepo.findByUsername(username) != null){ // Si el username ya está en uso
      map.put("error", "Name in use");
      status = HttpStatus.FORBIDDEN;
    }
    else { // Si es correcto ...
      Player player = playerRepo.save(new Player(username, passwordEncoder.encode(password)));
      map.put("username", player.getUsername());
      status = HttpStatus.CREATED;
    }
    return new ResponseEntity<>(map, status);
  }

  @RequestMapping("/game/{gameId}/players")
  public ResponseEntity<Map<String, Object>> joinGame(@PathVariable long gameId, Authentication auth){
    Map<String, Object> json = new LinkedHashMap<>();
    HttpStatus status;

    if( isGuest(auth) ) { // Si no hay usuario logueado
      status = HttpStatus.UNAUTHORIZED;
      json.put("error", "No user logged in");
    }
    else {
      Optional<Game> game = gameRepo.findById(gameId);
      if (!game.isPresent()){ // Si no existe el juego
        status = HttpStatus.FORBIDDEN;
        json.put("error", "No such game");
      }
      else if(game.get().getPlayers().size() == 2) { // Si el juego esta completo
        status = HttpStatus.FORBIDDEN;
        json.put("error", "Game is full");
      }
      else {
        Player player = getCurrentPlayer(auth);
        if(game.get().getPlayers().get(0).getId() == player.getId()){ // Si el jugador ya esta en el juego
          status = HttpStatus.FORBIDDEN;
          json.put("error", "The player is already in the game");
        }
        else { // Si corresponde agrega el usuario al juego
          status = HttpStatus.CREATED;
          GamePlayer gp = new GamePlayer(game.get(), player);
          gamePlayerRepo.save(gp);
          json.put("gpid", gp.getId());
        }
      }
    }

    return new ResponseEntity<>(json, status);
  }

}
