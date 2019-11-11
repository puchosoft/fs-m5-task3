$(function(){

  function showUserLogged(player){
    var userLogged = new Vue({
      el:   '#userInfo',
      data:
       {
       player: player==null?"guest":player.email,
       credential:  {username:'', password:''}
       },
      methods: {
        login:  function(){
          if(verifiedUserAndPass(this.credential)){
            login(this.credential);
          }
        },
        signup: function(){
          if(verifiedUserAndPass(this.credential)){
            signup(this.credential);
          }
        },
        logout: function(){
          logout();
        }
      }
    });
  }

  function getTotal(name){
    return scoreList.filter(s => s.name == name).reduce((total, s) => total + s.score, 0);
  }

  function getWons(name){
    return scoreList.filter(s => s.name == name && s.score == 1).length;
  }

  function getLosts(name){
    return scoreList.filter(s => s.name == name && s.score == 0).length;
  }

  function getTieds(name){
      return scoreList.filter(s => s.name == name && s.score == 0.5).length;
  }

  // Devuelve un JSON con la tabla de jugadores con puntajes mas altos
  function getLeaderTable(gameList){
    scoreList=[]; // Lista de nombres y puntajes de todos los juegos
    var nameList=[]; // Lista de nombres de jugadores (no repetidos)
    var leaderTable=[]; // JSON de la LeaderTable

    // Obtiene la lista de jugadores con puntajes
    gameList.forEach(game => {
      game.gamePlayers.forEach(gp => {
        scoreList.push(
          {
            name: gp.player.email,
            score: gp.score
          }
        );
      });
    });
    // Elimina de la lista los puntajes de juegos no terminados
    scoreList = scoreList.filter(player => player.score != null);

    // Obtiene la lista de nombres unicos (no repetidos)
    nameList = scoreList.map(s => s.name).
      filter((n,i,nl) => nl.indexOf(n)==i);

    // Para cada jugador de la lista, agrega una linea de datos al JSON
    nameList.forEach(name => {
      leaderTable.push(
        {
          name: name,
          total: getTotal(name).toFixed(1),
          won: getWons(name),
          lost: getLosts(name),
          tied: getTieds(name)
        }
      );
    });

    // Ordena las lineas del JSON por puntajes decrecientes
    return leaderTable.sort((l1, l2) => l2.total - l1.total);
  }

  function getGamePlayerId(player, game){
    if(player != null){
      var gp = game.gamePlayers.find(gp => gp.player.id == player.id);
    }
    return (gp === undefined)? null : gp.id;
  }

  function getGameViewUrl(player,game){
    var url = null;
    var gp = getGamePlayerId(player,game);
    if (gp != null){
        url = '/web/game.html?gp='+ gp;
    }
    return url;
  }

  // Construye una lista ordenada de Games con su informacion y la devuelve al HTML
  function showGameList(gameList, player){
    gList = [];
    gameList.forEach(g => {
      var date = new Date(g.created).toLocaleString();
      var players = g.gamePlayers.map(gp => gp.player.email);
      players.push('');

      gList.push({
        'viewUrl':  getGameViewUrl(player,g),
        'gameId':   g.id,
        'created':  date,
        'player1':  players[0],
        'player2':  players[1],
        'join':     player!=null && players[1]=='' && getGamePlayerId(player,g)==null
      });
    });

    var divGameList = new Vue({
      el:'#gameList',
      data: {
        titles: ['Nº','Created','Player 1','Player 2'],
        player: player==null?"guest":player.email,
        games:  gList
      },
      methods: {
        joinGame: function(gameId){
          $.post('/api/game/'+gameId+'/players')
          .done(
            function(json){
              location.assign('/web/game.html?gp='+ json.gpid);
            }
          )
          .fail(
            function(json){
              alert('Error: '+ json.error);
            }
          );
        },
        createGame: function(){
          $.post('/api/games')
          .done(
            function(data){
              location.assign('/web/game.html?gp='+ data.gpid);
            }
          )
          .fail(
            function(){
              alert('Game creation failed');
            }
          );
        }
      }
    });
  }

  // Construye una tabla de estadisticas de puntajes de jugadores y la devuelve al HTML
  function showLeaderBoard(gameList){
    var tableLeaderBoard = new Vue({
      el:   '#leaderBoard',
      data: {
        titles: ['Name','Total','Won','Lost','Tied'],
        players: getLeaderTable(gameList)
      }
    });
  }

  function loadData(){
      $.getJSON("/api/games")
      .done(
        function(data) {
          showUserLogged(data.player);
          showGameList(data.games, data.player);
          showLeaderBoard(data.games);
        }
      );
  }

  loadData();
});
