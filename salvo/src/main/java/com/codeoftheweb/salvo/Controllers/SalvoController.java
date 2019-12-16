package com.codeoftheweb.salvo.Controllers;

import com.codeoftheweb.salvo.Entities.*;
import com.codeoftheweb.salvo.Repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.Repositories.GameRepository;
import com.codeoftheweb.salvo.Repositories.PlayerRepository;
import com.codeoftheweb.salvo.Repositories.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.*;

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
  private ShipRepository shipRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  // Devuelve un JSON con la informacion de todos los games en la URL /api/games
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

  // Genera un nuevo juego en el repositorio usando la URL /api/games
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

  // Devuelve un JSON con la informacion de un game especifico en la URL /api/game_view/nn
  @RequestMapping("/game_view/{gamePlayerId}")
  public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long gamePlayerId, Authentication auth) {
    GamePlayer gamePlayer = gamePlayerRepo.getOne(gamePlayerId);

    Map<String, Object> gameDTO = new LinkedHashMap<>();

    if(auth==null || gamePlayer.getPlayer().getId() != getCurrentPlayer(auth).getId()){
      gameDTO.put("Cheat", 401);
      return new ResponseEntity<>(gameDTO, HttpStatus.UNAUTHORIZED );
    }

    gameDTO = gamePlayer.getGame().toDTO();

    // Si el estado del juego es OPENED pero el gamePlayer tiene todas las naves lo pone en WAITING
    int gameStatus = gamePlayer.getGame().getStatus();
    int status = (gameStatus == 0 && gamePlayer.isFullOfShips()) ? 1 : gameStatus;
    gameDTO.put("status", status);

    gameDTO.put("ships", gamePlayer.getShips()
        .stream()
        .map(ship -> ship.toDTO())
    );

    ArrayList<Map<String, Object>> remainingShips = ShipsValidation.getShipTypes()
        .stream()
        .map(HashMap<String, Object>::new)
        .collect(toCollection(ArrayList::new));

    gameDTO.put("remainingShips", remainingShips
        .stream()
        .map(shipT -> {
          int q = (int)shipT.get("quantity");
          String t = shipT.get("type").toString();
          q = q - (int)gamePlayer.getShips()
            .stream()
            .filter(ship -> ship.getShipType().equals(t))
            .count();
          shipT.replace("quantity", q);
          return shipT;
        })
        .filter(shipT -> (int)shipT.get("quantity") > 0)
    );

    gameDTO.put("salvoes", gamePlayer.getGame().getGamePlayers()
        .stream()
        .map(game_gamePlayer -> game_gamePlayer.toSalvoDTO())
        .collect(toSet())
    );

    return new ResponseEntity<>(gameDTO,HttpStatus.OK);
  }

  // Genera un nuevo jugador en el repositorio usando la URL /api/players
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
    else { // Si es correcto crea un nuevo jugador y lo agrega al repositorio.
      Player player = playerRepo.save(new Player(username, passwordEncoder.encode(password)));
      map.put("username", player.getUsername());
      status = HttpStatus.CREATED;
    }
    return new ResponseEntity<>(map, status);
  }

  // Agrega un jugador a un juego
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
        else { // Si corresponde crea un nuevo gamePlayer y los agrega al juego
          status = HttpStatus.CREATED;
          GamePlayer gp = new GamePlayer(game.get(), player);
          gamePlayerRepo.save(gp);
          json.put("gpid", gp.getId());
        }
      }
    }

    return new ResponseEntity<>(json, status);
  }

  // Agrega naves a un gamePlayer usando la URL /api/games/players/nn/ships
  @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> addShips(@PathVariable long gamePlayerId, @RequestBody Set<Ship> shipList , Authentication auth){

    Map<String, Object> json = new LinkedHashMap<>();
    HttpStatus status = HttpStatus.CREATED;
    GamePlayer gamePlayer = gamePlayerRepo.findById(gamePlayerId).orElse(null);

    // Si no hay usuario logueado o no existe el gamePlayer o el gamePlayer no coincide con el player
    if( isGuest(auth) || gamePlayer == null || !playerMatchGP(getCurrentPlayer(auth), gamePlayer)) {
      status = HttpStatus.UNAUTHORIZED;
      json.put("Error", "Unauthorized action");
      return new ResponseEntity<>(json, status);
    }

    // ¿Cantidad de naves de cada tipo correctas?
    boolean allShipsAmountValid  = ShipsValidation.getShipTypes().stream()
      .filter(st -> {
        long totalNewShips4Type = shipList.stream().filter(ship -> ship.getShipType().equals(st.get("type"))).count();
        long totalCurrentShips4Type = gamePlayer.getShips().stream().filter(ship -> ship.getShipType().equals(st.get("type"))).count();
        return (((int)st.get("quantity") - totalCurrentShips4Type - totalNewShips4Type) < 0);
      })
      .count() == 0;

    // ¿Todas las naves tienen ubicaciones validas?
    boolean allShipsValids = shipList.stream().filter(ship -> !ship.isValid()).count() == 0;

    // ¿Todas las naves no se solapan?
    List<String> locationsList = shipList.stream().map(ship -> ship.getLocations()).flatMap(locs -> locs.stream()).sorted().collect(toList());
    SortedSet<String> locationsSet = new TreeSet<>(locationsList);
    boolean allShipsWithoutOverlap = locationsList.stream().collect(joining()).equals(locationsSet.stream().collect(joining()));

    // ¿Todas las naves tienen espacio libre en la grilla?
    boolean allShipsLocationsFree = (shipList.stream().filter(ship -> !(gamePlayer.isLocationsFree(ship))).count()) == 0;

    // Si se envian demasiadas naves de un tipo, o alguna nave no es valida, o alguna nave se solapa, o alguna nave no tiene espacio libre
    if( !allShipsAmountValid || !allShipsValids || !allShipsWithoutOverlap || !allShipsLocationsFree){
      status = HttpStatus.FORBIDDEN;
      json.put("Error", "Invalid ships");
      return new ResponseEntity<>(json, status);
    }

    //Agrega las naves al repositorio
    shipList.forEach(ship -> {
      shipRepo.save(new Ship(ship.getShipType(), gamePlayer, ship.getLocations()));
    });

    json.put("Success", "Ships created");
    return new ResponseEntity<>(json, status);
  }

  // Devuelve si hay un usuario conectado o no
  private boolean isGuest(Authentication auth) {
    return auth == null || auth instanceof AnonymousAuthenticationToken;
  }

  // Devuelve, si existe, el usuario conectado actualmente. De lo contrario devuelve 'null'
  private Player getCurrentPlayer(Authentication auth) {
    return isGuest(auth)? null : playerRepo.findByUsername(auth.getName());
  }

  // Devuelve si coinciden player y gamePlayer
  private boolean playerMatchGP(Player player, GamePlayer gamePlayer) {
    return player.getId() == gamePlayer.getPlayer().getId();
  }
}
