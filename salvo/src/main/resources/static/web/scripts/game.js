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
      salvoVisibility:  Array()
    },
    methods:  {
      logout: function(){
        logout();
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

function loadData(){
  if(location.search.startsWith("?gp=")){
    id = location.search.slice(4);
    $.getJSON("/api/game_view/"+id)
    .done(
      function(gameViewData){
        gameInfo.gameStatus = gameViewData.status;
        showPlayersInfo(id,gameViewData.gamePlayers);
        showGrids(gameViewData.ships, gameViewData.salvoes);
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
      var row = loc.slice(0,1).charCodeAt(0) - 'A'.charCodeAt(0);
      var col = loc.slice(1)-1;
      gameInfo.shipVisibility[row][col] = 0;
    });
  });
}

function setShipsDamage(enemySalvoes){
  enemySalvoes.forEach(t => {
    t.shots.forEach(s => {
      var row = s.slice(0,1).charCodeAt(0) - 'A'.charCodeAt(0);
      var col = s.slice(1)-1;
      if(gameInfo.shipVisibility[row][col]==0){
        gameInfo.shipVisibility[row][col] = t.turn;
      }
    });
  });
}

// Marca con (nº turno) las ubicaciones de los salvos en la grilla
function setSalvoesVisibility(salvoes){
  salvoes.forEach(turn => {
    turn.shots.forEach(shot => {
      var row = shot.slice(0,1).charCodeAt(0) - 'A'.charCodeAt(0);
      var col = shot.slice(1)-1;
      gameInfo.salvoVisibility[row][col] = turn.turn;
    });
  });
}

function showGrids(ships, salvoes){
  setShipsVisibility(ships);
  if(enemyID > 0){
    setShipsDamage(salvoes.filter( p => p.playerID == enemyID)[0].turns);
  }
  if (gameInfo.gameStatus > 1){
    setSalvoesVisibility(salvoes.filter( s => s.playerID == youID)[0].turns);
  }
}

