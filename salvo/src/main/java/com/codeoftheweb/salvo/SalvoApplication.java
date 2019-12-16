package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Entities.*;
import com.codeoftheweb.salvo.Repositories.*;
import org.hibernate.mapping.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@SpringBootApplication
public class SalvoApplication {

  public static void main(String[] args) {
    SpringApplication.run(SalvoApplication.class, args);
  }


  @Bean
  public PasswordEncoder passwordEncoder(){
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  // Este codigo se ejecuta al inicio de la aplicacion
  @Bean
  public CommandLineRunner initData(PlayerRepository playerRepo,
                                    GameRepository gameRepo,
                                    GamePlayerRepository gamePlayerRepo,
                                    ShipRepository shipRepo,
                                    SalvoRepository salvoRepo,
                                    ScoreRepository scoreRepo) {
    return (args) -> {

      // guardamos algunos players
      Player jack = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
      Player chloe = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
      Player kim = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
      Player tony = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));
      playerRepo.save(jack);
      playerRepo.save(chloe);
      playerRepo.save(kim);
      playerRepo.save(tony);

      // guardamos algunos games
      Game game1 = new Game();
      Game game2 = new Game(1*3600);
      Game game3 = new Game(2*3600);
      Game game4 = new Game(3*3600);
      Game game5 = new Game(4*3600);
      Game game6 = new Game(5*3600);
      Game game7 = new Game(6*3600);
      Game game8 = new Game(7*3600);
      gameRepo.save(game1);
      gameRepo.save(game2);
      gameRepo.save(game3);
      gameRepo.save(game4);
      gameRepo.save(game5);
      gameRepo.save(game6);
      gameRepo.save(game7);
      gameRepo.save(game8);

      // guardamos algunos gamePlayers
      gamePlayerRepo.save(new GamePlayer(game1, jack));
      gamePlayerRepo.save(new GamePlayer(game1, chloe));
      gamePlayerRepo.save(new GamePlayer(game2, jack));
      gamePlayerRepo.save(new GamePlayer(game2, chloe));
      gamePlayerRepo.save(new GamePlayer(game3, chloe));
      gamePlayerRepo.save(new GamePlayer(game3, tony));
      gamePlayerRepo.save(new GamePlayer(game4, chloe));
      gamePlayerRepo.save(new GamePlayer(game4, jack));
      gamePlayerRepo.save(new GamePlayer(game5, tony));
      gamePlayerRepo.save(new GamePlayer(game5, jack));
      gamePlayerRepo.save(new GamePlayer(game6, kim));
      gamePlayerRepo.save(new GamePlayer(game7, tony));
      gamePlayerRepo.save(new GamePlayer(game8, kim));
      gamePlayerRepo.save(new GamePlayer(game8, tony));

      // guardamos algunas ships
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game1, jack), new TreeSet<>(Arrays.asList("H2", "H3", "H4"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game1, jack), new TreeSet<>(Arrays.asList("E1", "F1", "G1"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game1, jack), new TreeSet<>(Arrays.asList("B4", "B5"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game1, chloe), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game1, chloe), new TreeSet<>(Arrays.asList("F1", "F2"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game2, jack), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game2, jack), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game2, chloe), new TreeSet<>(Arrays.asList("A2", "A3", "A4"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game2, chloe), new TreeSet<>(Arrays.asList("G6", "H6"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game3, chloe), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game3, chloe), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game3, tony), new TreeSet<>(Arrays.asList("A2", "A3", "A4"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game3, tony), new TreeSet<>(Arrays.asList("G6", "H6"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game4, chloe), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game4, chloe), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game4, jack), new TreeSet<>(Arrays.asList("A2", "A3", "A4"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game4, jack), new TreeSet<>(Arrays.asList("G6", "H6"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game5, tony), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game5, tony), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game5, jack), new TreeSet<>(Arrays.asList("A2", "A3", "A4"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game5, jack), new TreeSet<>(Arrays.asList("G6", "H6"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game6, kim), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game6, kim), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Destroyer", gamePlayerRepo.findByGameAndPlayer(game8, kim), new TreeSet<>(Arrays.asList("B5", "C5", "D5"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game8, kim), new TreeSet<>(Arrays.asList("C6", "C7"))));
      shipRepo.save(new Ship("Submarine", gamePlayerRepo.findByGameAndPlayer(game8, tony), new TreeSet<>(Arrays.asList("A2", "A3", "A4"))));
      shipRepo.save(new Ship("Patrol Boat", gamePlayerRepo.findByGameAndPlayer(game8, tony), new TreeSet<>(Arrays.asList("G6", "H6"))));

      // guardamos algunos salvoes
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game1, jack), 1, new String[]{"B5", "C5", "F1"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game1, chloe), 1, new String[]{"B4", "B5", "B6"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game1, jack), 2, new String[]{"F2", "D5"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game1, chloe), 2, new String[]{"E1", "H3", "A2"}));

      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game2, jack), 1, new String[]{"A2", "A4", "G6"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game2, chloe), 1, new String[]{"B5", "D5", "C7"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game2, jack), 2, new String[]{"H3", "H6"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game2, chloe), 2, new String[]{"C5", "C6"}));

      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game3, chloe), 1, new String[]{"G6", "H6", "A4"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game3, tony), 1, new String[]{"H1","H2","H3"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game3, chloe), 2, new String[]{"A2", "A3", "D8"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game3, tony), 2, new String[]{"E1","F2","G3"}));

      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game4, chloe), 1, new String[]{"A3","A4","F7"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game4, jack), 1, new String[]{"B5","C6","H1"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game4, chloe), 2, new String[]{"A2","G6","H6"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game4, jack), 2, new String[]{"C5","C7","D5"}));

      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game5, tony), 1, new String[]{"A1","A2","A3"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game5, jack), 1, new String[]{"B5","B6","C7"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game5, tony), 2, new String[]{"G6","G7","G8"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game5, jack), 2, new String[]{"C6","D6","E6"}));
      salvoRepo.save(new Salvo(gamePlayerRepo.findByGameAndPlayer(game5, jack), 3, new String[]{"H1","H8"}));

      scoreRepo.save(new Score(game1,jack,1.0f));
      scoreRepo.save(new Score(game1,chloe,0.0f));
      scoreRepo.save(new Score(game2,jack,0.5f));
      scoreRepo.save(new Score(game2,chloe,0.5f));
      scoreRepo.save(new Score(game3,chloe,1.0f));
      scoreRepo.save(new Score(game3,tony,0.0f));
      scoreRepo.save(new Score(game4,chloe,0.5f));
      scoreRepo.save(new Score(game4,jack,0.5f));

    };
  }
}

@Configuration
class WebSecurityAuthentication extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  PlayerRepository playerRepo;

  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(inputName-> {
      Player player = playerRepo.findByUsername(inputName);
      if (player != null) {
        return new User(player.getUsername(),player.getPassword(),
            AuthorityUtils.createAuthorityList("USER"));
      } else {
        throw new UsernameNotFoundException("Unknown user: " + inputName);
      }
    });
  }
}

@EnableWebSecurity
@Configuration
class WebSecurityAuthorization extends WebSecurityConfigurerAdapter {

  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/web/games.html", "/web/styles/**",  "/web/img/**", "/web/scripts/games.js", "/web/scripts/authentication.js", "/api/games", "/api/login", "/api/players").permitAll()
        .antMatchers("/rest/**").hasAuthority("ADMIN")
        .antMatchers("/api/**", "/web/game.html", "/web/scripts/game.js").hasAuthority("USER")
        .anyRequest().denyAll();

    http.formLogin()
        .usernameParameter("username")
        .passwordParameter("password")
        .loginPage("/api/login");

    http.logout().logoutUrl("/api/logout");

    // turn off checking for CSRF tokens
    http.csrf().disable();

    // if user is not authenticated, just send an authentication failure response
    http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

    // if login is successful, just clear the flags asking for authentication
    http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

    // if login fails, just send an authentication failure response
    http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

    // if logout is successful, just send a success response
    http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
  }

  private void clearAuthenticationAttributes(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
  }
}
