package com.smu.is429.rockpaperscissorslizardspock;

import java.util.ArrayList;
import java.sql.*;
import com.google.appengine.api.utils.SystemProperty;

public class RockPaperScissorsLizardSpockDAO {
 
  private Connection conn;

  public RockPaperScissorsLizardSpockDAO() throws Exception {

    this.conn = connect();

  }

  private Connection connect() throws Exception {

    String url = null;

    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
    
      // Load the class that provides the new "jdbc:google:mysql://" prefix.
      Class.forName("com.mysql.jdbc.GoogleDriver");
      url = "jdbc:google:mysql://smu-is429-group-project:cloud/is429?user=root";
    
    } else {
    
      // Local MySQL instance to use during development.
      Class.forName("com.mysql.jdbc.Driver");
      url = "jdbc:mysql://127.0.0.1/is429?user=root";
    
    }

    return DriverManager.getConnection(url);

  }

  public int insertBot(String name, String bot, Language language) throws SQLException {

    String statement = "INSERT INTO bot (name, code, language, isVisible) VALUES(?, ?, ?, 0)";
    PreparedStatement stmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, name);
    stmt.setString(2, bot);
    stmt.setString(3, language.name());

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure inserting bot to database.");
    }

    int id = 0;
    ResultSet rs = stmt.getGeneratedKeys();
    if(rs.next()) {
      id = rs.getInt(1);
    }

    rs.close();
    stmt.close();

    return id;

  }

  public Bot getBot(int id) throws SQLException {

    String statement  = "SELECT * FROM bot WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, id);

    ResultSet rs = stmt.executeQuery();
    Bot bot = null;
    while(rs.next()) {
      bot = new Bot(rs.getInt("id"), rs.getString("name"), rs.getString("code"), Language.valueOf(rs.getString("language")), rs.getInt("level"));
    }

    rs.close();
    stmt.close();

    return bot;

  }

  // Retrieve list of bot that are available
  // to be selected
  public ArrayList<Bot> getBotList() throws SQLException {

    ArrayList<Bot> bots = new ArrayList<Bot>();

    String statement = "SELECT * FROM bot WHERE isVisible=1";
    PreparedStatement stmt = conn.prepareStatement(statement);

    ResultSet rs = stmt.executeQuery();
    while(rs.next()) {
      bots.add(new Bot(rs.getInt("id"), rs.getString("name"), rs.getString("code"), Language.valueOf(rs.getString("language")), rs.getInt("level")));
    }
    
    rs.close();
    stmt.close();

    return bots;

  }

  public void insertRound(String id, int playerBotId, int aiBotId, int roundNo, Move playerMove, Move aiMove, int score) throws SQLException {

    // Creating a new game is basically just inserting the very first round of
    // the game into database. More tedious because there is no existing value
    // for us to take from
    String statement = "INSERT INTO game_session VALUES(?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setString(1, id);
    stmt.setInt(2, playerBotId);
    stmt.setInt(3, aiBotId);
    stmt.setInt(4, roundNo);
    stmt.setString(5, playerMove.name());
    stmt.setString(6, aiMove.name());
    stmt.setInt(7, score);

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure inserting game session to database.");
    }

    stmt.close();

  }

  public GameSession getLatestRound(String id) throws SQLException {

    String statement = "SELECT * FROM game_session WHERE id=? ORDER BY roundNo DESC LIMIT 1";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setString(1, id);

    ResultSet rs = stmt.executeQuery();
    GameSession session = null;
    while(rs.next()) {
      session = new GameSession(rs.getString("id"), rs.getInt("roundNo"), rs.getInt("playerBotId"), rs.getInt("aiBotId"), Move.valueOf(rs.getString("playerMove")), Move.valueOf(rs.getString("aiMove")), rs.getInt("score"));
    }

    rs.close();
    stmt.close();

    return session;

  }

  public ArrayList<GameSession> getAllRounds(String id) throws SQLException {

    String statement = "SELECT * FROM game_session WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setString(1, id);

    ResultSet rs = stmt.executeQuery();
    ArrayList<GameSession> sessions = new ArrayList<GameSession>();
    while(rs.next()) {

      sessions.add(new GameSession(rs.getString("id"), rs.getInt("roundNo"), rs.getInt("playerBotId"), rs.getInt("aiBotId"), Move.valueOf(rs.getString("playerMove")), Move.valueOf(rs.getString("aiMove")), rs.getInt("score")));

    }
    
    rs.close();
    stmt.close();

    return sessions;

  }

  // If player's bot wins, update status
  // of player's bot so that it can be selected
  // from list
  public void updateBotStatus(int botId, int isVisible, int aiBotId) throws SQLException {

    String statement = "SELECT level FROM bot WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, aiBotId);

    ResultSet rs = stmt.executeQuery();
    int level = -2;
    while(rs.next()) {
      level = rs.getInt("level");
    }

    rs.close();
    stmt.close();

    // Level will remain as -2 if we cannot retrieve the opponent bot
    // If that is the case, the player bot is going against an bot that
    // does not exists so that is invalid in the first place. End 
    // execution immediately
    if(level == -2) {
      return;
    }

    statement = "UPDATE bot SET isVisible=?, level=? WHERE id=?";
    stmt = conn.prepareStatement(statement);
    stmt.setInt(1, isVisible);
    stmt.setInt(2, level + 1);
    stmt.setInt(3, botId);

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure updating bot status in database.");
    }

    stmt.close();

  }
  
  public void updateBotStatistics(int botId, String gameId, int win, int draw, int loss) throws SQLException {

    String statement = "INSERT INTO bot_stats (botId, gameId, win, draw, loss) VALUES (?, ?, ?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, botId);
    stmt.setString(2, gameId);
    stmt.setInt(3, win);
    stmt.setInt(4, draw);
    stmt.setInt(5, loss);

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure updating bot statistics in database.");
    }

    stmt.close();

  }

  public int getTotalScore(String gameId) throws SQLException {
   
    String statement = "SELECT SUM(score) AS 'result' FROM game_session WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setString(1, gameId);
    
    int result = -9999;
    ResultSet rs = stmt.executeQuery();
    while(rs.next()) {
      result = rs.getInt("result");
    }
    
    rs.close();
    stmt.close();
    
    return result;
    
  }
  
  public ArrayList<Move> getAiMoveHistory(int botId) throws SQLException {

    String statement = "SELECT aiMove from game_session WHERE aiBotId=? ORDER BY id LIMIT 10";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, botId);
    
    ArrayList<Move> history = new ArrayList<Move>();
    ResultSet rs = stmt.executeQuery();
    while(rs.next()) {
      history.add(Move.valueOf(rs.getString("aiMove")));
    }

    rs.close();
    stmt.close();

    return history;

  }

  public ArrayList<Move> getAiMoveHistory(int botId, String gameId) throws SQLException {

    String statement = "SELECT aiMove from game_session WHERE aiBotId=? AND id=? ORDER BY roundNo DESC LIMIT 10";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, botId);
    stmt.setString(2, gameId);
    
    ArrayList<Move> history = new ArrayList<Move>();
    ResultSet rs = stmt.executeQuery();
    while(rs.next()) {
      history.add(Move.valueOf(rs.getString("aiMove")));
    }

    rs.close();
    stmt.close();

    return history;

  }
  
  public void deleteGame(String gameID) throws SQLException {

    String statement = "DELETE FROM game_session WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setString(1, gameID);

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure deleting game from database");
    }

    stmt.close();

  }

  public void deleteBot(int botID) throws SQLException {

    String statement = "DELETE FROM bot WHERE id=?";
    PreparedStatement stmt = conn.prepareStatement(statement);
    stmt.setInt(1, botID);

    int success = stmt.executeUpdate();

    if(success == 0) {
      throw new SQLException("Failure deleting bot from database.");
    }

    stmt.close();

  }
  
  public void close() throws SQLException {
    conn.close();
  }

}