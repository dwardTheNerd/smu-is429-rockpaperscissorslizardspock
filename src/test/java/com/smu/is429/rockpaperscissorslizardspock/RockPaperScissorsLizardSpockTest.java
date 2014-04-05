package com.smu.is429.rockpaperscissorslizardspock;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import java.util.ArrayList;
import java.sql.SQLException;

public class RockPaperScissorsLizardSpockTest {

  @Test
  public void testGetBotList() {

    RockPaperScissorsLizardSpock app = new RockPaperScissorsLizardSpock();
    ArrayList<Bot> bots = app.getBotList();

    assertThat(bots.size(), greaterThan(0));

  }

  @Test
  public void testCreateNewPythonGame() throws SQLException, Exception {

    RockPaperScissorsLizardSpock app = new RockPaperScissorsLizardSpock();

    String code = "def play_game():\\n  return 'PAPER'";

    Response response = app.newGame("JUnit Test", code, 1, Language.PYTHON);
    GameSession session = response.getGameSession();

    assertThat(response.getSuccess(), is(true));
    assertThat(session.getRoundNo(), is(1));

    // After finish testing, we need to remove the test data from database
    RockPaperScissorsLizardSpockDAO gameDAO = new RockPaperScissorsLizardSpockDAO();
    gameDAO.deleteGame(session.getId());
    gameDAO.deleteBot(session.getPlayerBotId());

    gameDAO.close();

    
  }

  @Test
  public void testCreateNewRubyGame() throws SQLException, Exception {

    RockPaperScissorsLizardSpock app = new RockPaperScissorsLizardSpock();

    String code = "def play_game()\\n  return 'PAPER'\\nend";

    Response response = app.newGame("JUnit Test", code, 1, Language.RUBY);
    GameSession session = response.getGameSession();

    assertThat(response.getSuccess(), is(true));
    assertThat(session.getRoundNo(), is(1));

    // After finish testing, we need to remove the test data from database
    RockPaperScissorsLizardSpockDAO gameDAO = new RockPaperScissorsLizardSpockDAO();
    gameDAO.deleteGame(session.getId());
    gameDAO.deleteBot(session.getPlayerBotId());

    gameDAO.close();

  }

  @Test
  public void testCreateNewJSGame() throws SQLException, Exception {

    RockPaperScissorsLizardSpock app = new RockPaperScissorsLizardSpock();

    String code = "function playGame() {\\n  return \\\"PAPER\\\";\\n}";

    Response response = app.newGame("JUnit Test", code, 1, Language.JAVASCRIPT);
    GameSession session = response.getGameSession();

    assertThat(response.getSuccess(), is(true));
    assertThat(session.getRoundNo(), is(1));

    // After finish testing, we need to remove the test data from database
    RockPaperScissorsLizardSpockDAO gameDAO = new RockPaperScissorsLizardSpockDAO();
    gameDAO.deleteGame(session.getId());
    gameDAO.deleteBot(session.getPlayerBotId());

    gameDAO.close();

    
  }

  @Test
  public void testGameMechanics() throws SQLException, Exception {

    Judge judge = new Judge();

    // ------ What if player's move is Rock...

    int hasWon = judge.hasWon(Move.ROCK, Move.ROCK);
    assertThat("ROCK and ROCK should produce a 0", hasWon, is(0));

    hasWon = judge.hasWon(Move.ROCK, Move.PAPER);
    assertThat("ROCK loses to PAPER, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.ROCK, Move.SCISSORS);
    assertThat("ROCK wins SCISSORS, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.ROCK, Move.LIZARD);
    assertThat("ROCK wins LIZARD, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.ROCK, Move.SPOCK);
    assertThat("ROCK lose to SPOCK, so should produce a -1", hasWon, is(-1));

    // ------ What if player's move is Paper...
    
    hasWon = judge.hasWon(Move.PAPER, Move.ROCK);
    assertThat("PAPER wins ROCK, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.PAPER, Move.PAPER);
    assertThat("PAPER and PAPER should produce a 0", hasWon, is(0));

    hasWon = judge.hasWon(Move.PAPER, Move.SCISSORS);
    assertThat("PAPER loses to SCISSORS, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.PAPER, Move.LIZARD);
    assertThat("PAPER loses to LIZARD, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.PAPER, Move.SPOCK);
    assertThat("ROCK wins SPOCK, so should produce a 1", hasWon, is(1));

    // ------ What if player's move is Scissors...
    
    hasWon = judge.hasWon(Move.SCISSORS, Move.ROCK);
    assertThat("SCISSORS loses to ROCK, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.SCISSORS, Move.PAPER);
    assertThat("SCISSORS wins PAPER, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.SCISSORS, Move.SCISSORS);
    assertThat("SCISSORS and SCISSORS should produce a 0", hasWon, is(0));

    hasWon = judge.hasWon(Move.SCISSORS, Move.LIZARD);
    assertThat("SCISSORS wins LIZARD, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.SCISSORS, Move.SPOCK);
    assertThat("SCISSORS loses to SPOCK, so should produce a -1", hasWon, is(-1));

    // ------ What if player's move is Lizard...
    
    hasWon = judge.hasWon(Move.LIZARD, Move.ROCK);
    assertThat("LIZARD loses to ROCK, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.LIZARD, Move.PAPER);
    assertThat("LIZARD wins PAPER, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.LIZARD, Move.SCISSORS);
    assertThat("LIZARD loses to SCISSORS, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.LIZARD, Move.LIZARD);
    assertThat("LIZARD and LIZARD should produce a 0", hasWon, is(0));

    hasWon = judge.hasWon(Move.LIZARD, Move.SPOCK);
    assertThat("LIZARD wins SPOCK, so should produce a 1", hasWon, is(1));

    // ------ What if player's move is Spock...

    hasWon = judge.hasWon(Move.SPOCK, Move.ROCK);
    assertThat("SPOCK wins ROCK, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.SPOCK, Move.PAPER);
    assertThat("SPOCK loses to PAPER, so should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.SPOCK, Move.SCISSORS);
    assertThat("SPOCK wins SCISSORS, so should produce a 1", hasWon, is(1));

    hasWon = judge.hasWon(Move.SPOCK, Move.LIZARD);
    assertThat("SPOCK loses to LIZARD should produce a -1", hasWon, is(-1));

    hasWon = judge.hasWon(Move.SPOCK, Move.SPOCK);
    assertThat("SPOCK and SPOCK should produce a 0", hasWon, is(0));

  }

}