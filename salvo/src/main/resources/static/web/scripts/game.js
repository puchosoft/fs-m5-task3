$(function(){

  columns = ['1','2','3','4','5','6','7','8','9','10'];
  rows = ['A','B','C','D','E','F','G','H','I','J'];

  gameInfo = new Vue({
    el:   '#grids',
    data: {
      you:              '',
      enemy:            '',
      gameStatus:       0, // OPEN
      columns:          columns,
      rows:             rows,
      shipVisibility:   Array(),
      remainingShips:   Array(),
      new_ships:        Array(),
      dragShip:         -1,
      dragType:         '',
      dragLength:       0,
      dragHorizontal:   false,
      dragRemaining:    false,
      salvoVisibility:  Array()
    },
    methods:  {
      logout: function(){
        logout();
      },
      allowDrop:function(event){
        event.preventDefault();
      },
      dragStart:function(v,event){
        this.dragType = event.target.dataset.type;
        this.dragLength = Number(event.target.dataset.length);
        this.dragHorizontal = (event.target.dataset.horizontal == 'true');
        this.dragRemaining = (event.target.dataset.remaining == 'true');
        this.dragShip = Number(event.target.dataset.ship);
      },
      drop:function(x, y){
        putShip(x,y);
      },
      rotate:function(v,event){
        var ship = Number(event.target.dataset.ship);
        this.dragHorizontal = (event.target.dataset.horizontal == 'false');
        this.dragLength = this.new_ships[ship].locations.length;
        var loc = this.new_ships[ship].locations[0];
        var y = loc2Y(loc);
        var x = loc2X(loc);
        // Si hay lugar para rotar la nave
        if (verifyPlace(x,y)){
          //Borra la visibilidad de la nave antes de rotar
          //this.new_ships[ship].locations.forEach((loc) => this.shipVisibility[loc2Y(loc)][loc2X(loc)]=null);

          this.dragRemaining = false;
          this.dragType = this.new_ships[ship].type;
          this.dragShip = ship;
          putShip(x,y);
        }
      }
    }
  });

  // Inicializa la visibilidad de ships y salvoes en null
  rows.forEach(r => {
    var a = Array();
    columns.forEach(c => {
      a.push(null);
    });
    gameInfo.shipVisibility.push(a.slice());
    gameInfo.salvoVisibility.push(a.slice());
  });

  loadData();

});

function loc2X(loc){
  return Number(loc.slice(1))-1;
}

function loc2Y(loc){
  return loc.slice(0,1).charCodeAt(0) - 'A'.charCodeAt(0);
}

function xy2Loc(x,y){
  return rows[y] + (x+1).toString();
}

function putShip(x, y){
  if (verifyPlace(x,y)){
    var incX = gameInfo.dragHorizontal?1:0;
    var incY = 1 - incX;
    var length = gameInfo.dragLength;
    var ship = gameInfo.dragShip;

    // Si el drag proviene de la lista de remanentes, elimina la nave de la lista
    if (gameInfo.dragRemaining){
      var i = gameInfo.remainingShips.map(ship => ship.type).indexOf(gameInfo.dragType);
      if ((--gameInfo.remainingShips[i].quantity)==0){
        gameInfo.remainingShips.splice(i,1);
      }
    }
    // Si el drag proviene de la grilla, calcula la ubicacion actual
    else {
      var loc = gameInfo.new_ships[ship].locations[0];
      var oldY = loc2Y(loc);
      var oldX = loc2X(loc);
      var oldHorizontal = document.getElementById(loc).dataset.horizontal == 'true';
      var oldIncX = oldHorizontal?1:0;
      var oldIncY = 1 - oldIncX;
    }

    // Crea un objeto nave para enviar al backed
    var locations = Array();
    // Setea los data-x de los elementos DIV que componen la nave
    for(i = 0; i < length; i++){
      var location = xy2Loc(x+i*incX,y+i*incY);
      locations.push(location);
      var div_ship = document.getElementById(location);
      div_ship.dataset.ship = ship<0? gameInfo.new_ships.length : ship;
      div_ship.dataset.type = gameInfo.dragType;
      div_ship.dataset.length = length;
      div_ship.dataset.horizontal = gameInfo.dragHorizontal;
      div_ship.dataset.remaining = false;
      div_ship.draggable=true;
    }
    var obj={type: gameInfo.dragType, locations: locations};
    if (gameInfo.dragRemaining){
      gameInfo.new_ships.push(obj);
    }
    else {
      gameInfo.new_ships.splice(ship,1,obj);
    }

    // Setea la visibilidad de la nueva ubicacion en la grilla y borra la anterior si corresponde
    for(i = 0; i < length; i++){
      if (ship >=0){
        Vue.set(gameInfo.shipVisibility[oldY+i*oldIncY], oldX+i*oldIncX, null);
      }
      Vue.set(gameInfo.shipVisibility[y+i*incY], x+i*incX, -1);
    }
  }
}

function verifyPlace(x, y){

  // Verifica si el origen esta fuera de la grilla
  if (x<0 || x>9 || y<0 || y>9){
    return false;
  }

  var incX = gameInfo.dragHorizontal?1:0;
  var incY = 1 - incX;
  var length = gameInfo.dragLength;

  // Verifica si el objeto se sale parcialmente de la grilla
  if((x+(length-1)*incX)>9 || (y+(length-1)*incY)>9){
    return false;
  }

  // Verifica si todas las casillas estan disponibles
  for(var i = 1; i < length; i++){
    if(gameInfo.shipVisibility[y+i*incY][x+i*incX] != null){
      return false;
    }
  }
  // Si nada es incorrecto
  return true;
}

function loadData(){
  if(location.search.startsWith("?gp=")){
    id = location.search.slice(4);
    $.getJSON("/api/game_view/"+id)
    .done(
      function(gameViewData){
        gameInfo.gameStatus = gameViewData.status;
        showPlayersInfo(id,gameViewData.gamePlayers);
        showGrids(gameViewData.ships, gameViewData.salvoes, gameViewData.remainingShips);
      }
    )
    .fail(
      function(data){
        alert('Unauthorized user');
      }
    );
  }
}

//Muestra la informacion de los Players
function showPlayersInfo(id, gamePlayers){
  index=(gamePlayers[0].id != id);
  youID = gamePlayers[Number(index)].player.id;
  gameInfo.you = gamePlayers[Number(index)].player.email;
  enemyID = 0;
  gameInfo.enemy = '-nobody-';
  if(gamePlayers.length > 1){
    enemyID = gamePlayers[Number(!index)].player.id;
    gameInfo.enemy = gamePlayers[Number(!index)].player.email;
  }
}

// Marca con (0) las ubicaciones de los ships en la grilla
function setShipsVisibility(ships){
  ships.forEach(ship => {
    ship.locations.forEach(loc => {
      gameInfo.shipVisibility[loc2Y(loc)][loc2X(loc)] = 0;
    });
  });
}

function setShipsDamage(enemySalvoes){
  enemySalvoes.forEach(t => {
    t.shots.forEach(shot => {
      var y = loc2Y(shot);
      var x = loc2X(shot);
      if(gameInfo.shipVisibility[y][x]==0){
        gameInfo.shipVisibility[y][x] = t.turn;
      }
    });
  });
}

// Marca con (nº turno) las ubicaciones de los salvos en la grilla
function setSalvoesVisibility(salvoes){
  salvoes.forEach(turn => {
    turn.shots.forEach(shot => {
      gameInfo.salvoVisibility[loc2Y(shot)][loc2X(shot)] = turn.turn;
    });
  });
}

function setRemainingShips(ships){
  gameInfo.remainingShips = ships.slice();
}

function showGrids(ships, salvoes, remainingShips){
  setShipsVisibility(ships);
  if(enemyID > 0){
    setShipsDamage(salvoes.filter( p => p.playerID == enemyID)[0].turns);
  }
  if (gameInfo.gameStatus > 1){
    setSalvoesVisibility(salvoes.filter( s => s.playerID == youID)[0].turns);
  }
  if (gameInfo.gameStatus < 2){
    setRemainingShips(remainingShips);
  }
}

