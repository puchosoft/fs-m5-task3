function login(credential) {
  $.post("/api/login", credential)
  .done(
    function(){
      location.reload();
    }
  )
  .fail(
    function(){
      alert('Unregistered user or invalid password');
    }
  );
}

function signup(credential){
  $.post("/api/players", credential)
  .done(
    function(){
      login(credential);
    }
  )
  .fail(
    function(){
      alert('User already registered');
    }
  );
}

function logout(){
  $.post('/api/logout')
  .done(
    function(){
      location.assign('/web/games.html');
    }
  )
  .fail(
    function(){
      alert('Logout failed');
    }
  );
}

function verifiedUserAndPass(c) {
  var verified = true;

  // Ignora espacios al inicio y final
  var user = c.username.match('\\S+');
  user = (user == null)? '' : user[0];
  var pass = c.password.match('\\S+');
  pass = (pass == null)? '' : pass[0];
  // Actualiza la credencial
  c.username = user;
  c.password = pass;

  // Rechaza cadenas vacias
  if (user.length == 0 || pass.length == 0){
    verified = false;
    alert('No field can be empty');
  }

  // Rechaza cadenas con espacios intermedios
  else if (user.match('.+\\s.+')!=null || pass.match('.+\\s.+')!=null){
      verified = false;
      alert('No field can contain spaces');
  }

  // Rechaza emails invalidos
  else if (!user.match('\\w+(\\.\\w+)*@\\w+(\\.\\w+)+')){
      verified = false;
      alert('Invalid email');
  }

  return verified;
}

