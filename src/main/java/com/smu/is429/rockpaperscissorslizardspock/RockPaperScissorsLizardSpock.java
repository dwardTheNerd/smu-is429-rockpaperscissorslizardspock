package com.smu.is429.rockpaperscissorslizardspock;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.gson.Gson;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;

@Api(description = "API for RockPaperScissorsLizardSpock game", name = "rockpaperscissorslizardspock", version = "v1")
public class RockPaperScissorsLizardSpock {

  private final int NUMBER_OF_ROUNDS = 10;

  private final Judge judge;

  // Constructor
  public RockPaperScissorsLizardSpock() {

    judge = new Judge();    

  }

  // Start a new game with new bot
  @ApiMethod(name="newGame", path="newGame")
  public Response newGame(@Named("playerBotName") String playerBotName, @Named("playerBotCode") String playerBot, @Named("aiBotId") int aiBotId, @Named("language") Language language, @Named("userId") String userId) {

    RockPaperScissorsLizardSpockDAO gameDAO = null;

    Response response = null;
    
    // Retrieve ai bot code from the database
    try {
      
      gameDAO = new RockPaperScissorsLizardSpockDAO();
      Bot aiBot = gameDAO.getBot(aiBotId);
      
      // Get AI move history from database
      ArrayList<Move> aiMoveHistory = gameDAO.getBotMoveHistory(aiBot.getId());

      // Run code through verifier service to get move
      // Player is trying to create a new bot to play the game so there is not history
      // for ai bot to reference
      Move playerMove = runCode(playerBot, language, aiMoveHistory);
      Move aiMove = runCode(aiBot.getCode(), aiBot.getLanguage(), null);

      // At this stage, player's bot has been thoroughly validated.
      // Now we can insert player's bot code into database
      // However, we need to determine if user logged in to play the game
      // if userid is not "null", then we create a bot and loosely link it
      // to userid
      int playerBotId;
      if(userId.equals("null")) {

        playerBotId = playerBotId = gameDAO.insertBot(playerBotName, playerBot, language);

      } else {

        // Insert user into database
        // the inserPlayer() method will check if a user is already recorded in the database so it
        // will not create duplicate rows
        int userRowId = gameDAO.insertUser(userId);

        playerBotId = playerBotId = gameDAO.insertBot(playerBotName, playerBot, language, userRowId);

      }

      // Finally we can move on to find out who wins!
      int score = judge.hasWon(playerMove, aiMove);

      // Last step, we need to insert the new game session into the database
      // Generate a unique id to associate with the game session
      String id = generateID();
      gameDAO.insertRound(id, playerBotId, aiBot.getId(), 1, playerMove, aiMove, score);

      response = new Response();
      response.setSuccess(true);
      response.setGameSession(new GameSession(id, 1, playerBotId, aiBot.getId(), playerMove, aiMove, score));
      response.setTotalScore(score);
      return response;
      
    } catch(SQLException ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } catch(Exception ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          response = new Response();
          response.setSuccess(false);
          response.setMessage(ex.getMessage());
          return response;          
        }

      }

    }

  }

  // Start a new game with existing bots
  @ApiMethod(name="createNewGameWithExistingBots", path="createNewGameWithExistingBots")
  public Response createNewGameWithExistingBots(@Named("playerBotId") int playerBotId, @Named("aiBotId") int aiBotId) {

    RockPaperScissorsLizardSpockDAO gameDAO = null;

    Response response = null;
    
    // Retrieve ai bot code from the database
    try {
      
      gameDAO = new RockPaperScissorsLizardSpockDAO();
      Bot aiBot = gameDAO.getBot(aiBotId);
      Bot playerBot = gameDAO.getBot(playerBotId);

      // Since both bots already exists in the database, there must be move history on both
      // so we get both of them
      ArrayList<Move> playerMoveHistory = gameDAO.getBotMoveHistory(playerBot.getId());
      ArrayList<Move> aiMoveHistory = gameDAO.getBotMoveHistory(aiBot.getId());

      // Run code through verifier service to get move
      Move playerMove = runCode(playerBot.getCode(), playerBot.getLanguage(), aiMoveHistory);
      Move aiMove = runCode(aiBot.getCode(), aiBot.getLanguage(), playerMoveHistory);

      // Finally we can move on to find out who wins!
      int score = judge.hasWon(playerMove, aiMove);

      // Last step, we need to insert the new game session into the database
      // Generate a unique id to associate with the game session
      String id = generateID();
      gameDAO.insertRound(id, playerBot.getId(), aiBot.getId(), 1, playerMove, aiMove, score);

      response = new Response();
      response.setSuccess(true);
      response.setGameSession(new GameSession(id, 1, playerBotId, aiBot.getId(), playerMove, aiMove, score));
      response.setTotalScore(score);
      return response;
      
    } catch(SQLException ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } catch(Exception ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          response = new Response();
          response.setSuccess(false);
          response.setMessage(ex.getMessage());
          return response;          
        }

      }

    }

  }

  // Get next move for current game
  @ApiMethod(name="getNextMove", path="getNextMove")
  public Response getNextMove(@Named("gameID") String id) {

    RockPaperScissorsLizardSpockDAO gameDAO = null;
    Response response = null;
    
    try {

      gameDAO = new RockPaperScissorsLizardSpockDAO();

      // Retrieve latest round
      GameSession previousRound = gameDAO.getLatestRound(id);

      if(previousRound == null) {
        throw new Exception("Game not found!");
      }

      int totalScore = gameDAO.getTotalScore(id);

      // If all rounds has been completed, no need to process any more
      // rounds. Just inform player who has won
      if(previousRound.getRoundNo() == NUMBER_OF_ROUNDS) {
        
        // Positive score means player has won.
        if (totalScore > 0) {         
          
           response = new Response();
           response.setSuccess(true);
           response.setTotalScore(totalScore);
           response.setMessage("Congratulations! Your bot has won the game!");
           return response;

        } else if(totalScore == 0) {
          
           response = new Response();
           response.setSuccess(true);
           response.setTotalScore(totalScore);
           response.setMessage("Your bot draw the game! Don't be sad, try again!");
           return response;
           
        } else {
          
           response = new Response();
           response.setSuccess(true);
           response.setTotalScore(totalScore);
           response.setMessage("Your bot lost the game. Try modifying your codes and try again!");
           return response;
        }
        
      }

      // Retrieve bots
      Bot playerBot = gameDAO.getBot(previousRound.getPlayerBotId());
      Bot aiBot = gameDAO.getBot(previousRound.getAiBotId());

      // Since both bots already exists in the database, there must be move history on both
      // so we get both of them
      ArrayList<Move> playerMoveHistory = gameDAO.getBotMoveHistory(playerBot.getId());
      ArrayList<Move> aiMoveHistory = gameDAO.getBotMoveHistory(aiBot.getId());

      // Run code to get move
      Move playerMove = runCode(playerBot.getCode(), playerBot.getLanguage(), aiMoveHistory);
      Move aiMove = runCode(aiBot.getCode(), aiBot.getLanguage(), playerMoveHistory);

      int score = judge.hasWon(playerMove, aiMove);
      gameDAO.insertRound(id, playerBot.getId(), aiBot.getId(), previousRound.getRoundNo() + 1, playerMove, aiMove, score);
      totalScore += score;

      response = new Response();

      // The moment the last round is reached and successfully processed,
      //  we need to check for winning condition
      if((previousRound.getRoundNo() + 1) == NUMBER_OF_ROUNDS) {

        // Positive score means player has won. So player's bot will be
        // made public for other users to select
        if (totalScore > 0) {

          // Update player's bot status
          gameDAO.updateBotStatus(previousRound.getPlayerBotId(), 1, previousRound.getAiBotId());

          // Calculate ELO ratings for both player and opponent bot
          int elo_player = calculateRating(1, playerBot.getElo(), aiBot.getElo());
          int elo_ai = calculateRating(0, aiBot.getElo(), playerBot.getElo());
          
          // Update statistics for both bots
          gameDAO.updateBotStatistics(previousRound.getPlayerBotId(), playerBot.getWinCount() + 1, playerBot.getDrawCount(), playerBot.getLossCount(), elo_player);
          gameDAO.updateBotStatistics(previousRound.getAiBotId(), aiBot.getWinCount(), aiBot.getDrawCount(), aiBot.getLossCount() + 1, elo_ai);

          response.setMessage("Congratulations! Your bot has won the game!");

        } else if(totalScore == 0) {

          // Calculate ELO ratings for both player and opponent bot
          int elo_player = calculateRating(0.5, playerBot.getElo(), aiBot.getElo());
          int elo_ai = calculateRating(0.5, aiBot.getElo(), playerBot.getElo());
          
          // Update statistics for both bots
          gameDAO.updateBotStatistics(previousRound.getPlayerBotId(), playerBot.getWinCount(), aiBot.getDrawCount() + 1, aiBot.getLossCount(), elo_player);
          gameDAO.updateBotStatistics(previousRound.getAiBotId(), aiBot.getWinCount(), aiBot.getDrawCount() + 1, aiBot.getLossCount(), elo_ai);

          response.setMessage("Your bot draw the game! Don't be sad, try again!");

        } else {

          // Calculate ELO ratings for both player and opponent bot
          int elo_player = calculateRating(0, playerBot.getElo(), aiBot.getElo());
          int elo_ai = calculateRating(1, aiBot.getElo(), playerBot.getElo());
          
          // Update statistics for both bots
          gameDAO.updateBotStatistics(previousRound.getPlayerBotId(), playerBot.getWinCount(), playerBot.getDrawCount(), playerBot.getLossCount() + 1, elo_player);
          gameDAO.updateBotStatistics(previousRound.getAiBotId(), aiBot.getWinCount() + 1, aiBot.getDrawCount(), aiBot.getLossCount(), elo_ai);

          response.setMessage("Your bot lost the game. Try modifying your codes and try again!");

        }

      }
      
      response.setSuccess(true);
      response.setTotalScore(totalScore);
      response.setGameSession(new GameSession(id, previousRound.getRoundNo() + 1, playerBot.getId(), aiBot.getId(), playerMove, aiMove, score));
      return response;

    } catch(SQLException ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } catch(Exception ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          response = new Response();
          response.setSuccess(false);
          response.setMessage(ex.getMessage());
          return response;
          
        }

      }

    }

  }

  // Retrieve list of bots that player can select
  // to pit against his bot
  @ApiMethod(name="getBotList", path="getBotList")
  public ArrayList<Bot> getBotList() {

    ArrayList<Bot> bots = null;

    RockPaperScissorsLizardSpockDAO gameDAO = null;

    try {

      gameDAO = new RockPaperScissorsLizardSpockDAO();
      bots = gameDAO.getBotList();

    } catch(SQLException ex) {

      // TODO: Error handling

    } catch(Exception ex) {

      // TODO: Error handling

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          // TODO: Error handling

        }

      }

    }

    return bots;

  }

  @ApiMethod(name="getAllGameRounds", path="getAllGameRounds")
  public ArrayList<GameSession> getAllGameRounds(@Named("gameId") String id) {

    ArrayList<GameSession> sessions = null;

    RockPaperScissorsLizardSpockDAO gameDAO = null;

    try {

      gameDAO = new RockPaperScissorsLizardSpockDAO();
      sessions = gameDAO.getAllRounds(id);

    } catch(SQLException ex) {

      // TODO: Error handling

    } catch(Exception ex) {

      // TODO: Error handling

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          // TODO: Error handling

        }

      }

    }

    return sessions;

  }

  // Retrieve the score for a particular game
  // The score is obtained by summing up the scores from each round
  @ApiMethod(name="getTotalScore", path="getTotalScore")
  public Response getTotalScore(@Named("gameId") String gameId) {
    
    int totalScore = -9999;
    RockPaperScissorsLizardSpockDAO gameDAO = null;
    Response response = null;
    
    try {
      
      gameDAO = new RockPaperScissorsLizardSpockDAO();
      totalScore = gameDAO.getTotalScore(gameId);
      
      response = new Response();
      response.setSuccess(true);
      response.setTotalScore(totalScore);
      
      if(totalScore > 0) {
        response.setMessage("Player has won the game");
      } else if(totalScore == 0) {
        response.setMessage("Player drew with AI"); 
      } else {
        response.setMessage("Player lost to AI"); 
      }
      
      return response;
      
    } catch(SQLException ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } catch(Exception ex) {

      response = new Response();
      response.setSuccess(false);
      response.setMessage(ex.getMessage());
      return response;

    } finally {

      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          response = new Response();
          response.setSuccess(false);
          response.setMessage(ex.getMessage());
          return response;

        }

      }

    }
    
  }
  
  @ApiMethod(name="getBotsByUser", path="getBotsByUser")
  public ArrayList<Bot> getBotsByUser(@Named("userId") String userid) {
    
    RockPaperScissorsLizardSpockDAO gameDAO = null;
    ArrayList<Bot> bots = new ArrayList<Bot>();
    
    try {
      
      gameDAO = new RockPaperScissorsLizardSpockDAO();
      bots = gameDAO.getBotListByUser(userid);
      
    } catch(SQLException ex) {
      
      // TODO: Error handling
      
    } catch(Exception ex) {
      
      // TODO: Error handling
      
    } finally {
      
      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          // TODO: Error handling

        }

      }
    
    }
    
    return bots;
    
  }
  
  @ApiMethod(name="getTopTenBots", path="getTopTenBots")
  public ArrayList<Bot> getTopTenBots() {
    
    RockPaperScissorsLizardSpockDAO gameDAO = null;
    ArrayList<Bot> bots = new ArrayList<Bot>();
    
    try {
      
      gameDAO = new RockPaperScissorsLizardSpockDAO();
      bots = gameDAO.getTopTenBots();
      
    } catch(SQLException ex) {
      
      // TODO: Error handling
      
    } catch(Exception ex) {
      
      // TODO: Error handling
      
    } finally {
      
      if(gameDAO != null) {

        try {
          gameDAO.close();
          gameDAO = null;
        } catch(SQLException ex) {
          
          // TODO: Error handling

        }

      }
    
    }
    
    return bots;
    
  }
  
  private Move runCode(String code, Language language, ArrayList<Move>opponentMoveHistory) throws SQLException, Exception {

    String urlString = "http://162.222.183.53/";
    String parameters = "";
    String tests = "";

    String opponentMoveHistoryString = "[]";

    if(opponentMoveHistory != null) {

      opponentMoveHistoryString = convertMoveList(opponentMoveHistory);

    }

    if(language == Language.PYTHON) {

      urlString += "python";
      tests = ">>> play_game(" + opponentMoveHistoryString + ")\\n'ANYTHING'";
      parameters = "{\"tests\":\""+ tests +"\",\"solution\":\"" + code + "\"}";

    } else if(language == Language.RUBY) {

      urlString += "ruby";
      tests = "assert_equal('ANYTHING',play_game(" + opponentMoveHistoryString + "))";
      parameters = "{\"tests\":\""+ tests +"\",\"solution\":\"" + code + "\"}";

    } else {   // Any other language values we default to javascript

      urlString += "js";
      tests = "assert_equal('ANYTHING',playGame(" + opponentMoveHistoryString + "));";
      parameters = "{\"tests\":\""+ tests +"\",\"solution\":\"" + code + "\"}";

    }

    // Need to base64 encode the parameters to be sent to verifier server
    // BUT we also need to url encode the base64 encoded parameters to make it
    // safe to be sent in a URL
    parameters = "jsonrequest=" + URLEncoder.encode(base64Encode(parameters), "UTF-8");

    URL url = new URL(urlString + "?" + parameters);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // Configuring request header
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", "TornadoServer/3.2");

    int responseCode = conn.getResponseCode();
    String verifierResponseString;

    switch(responseCode) {

      case 200:
      case 201:
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
          sb.append(line + "\n");
        }
        br.close();
        verifierResponseString = sb.toString();
        break;
      default:
        throw new Exception("Unable to verify code now. Please try again.");

     }

    Gson gson = new Gson();
    VerifyServiceResponse verifierResponse = gson.fromJson(verifierResponseString, VerifyServiceResponse.class);

    // throw error message if there is one
    if(verifierResponse.getErrors() != null) {
      throw new Exception(verifierResponse.getErrors());
    }

    // Because we are using enum to specify the valid moves, if player or ai bot method return a value
    // that is not inside our specified values in enum, it will throw an exception.
    // And just in case the move returned is not in uppercase and/or it possess other extra characters, we strip it
    Move move = Move.valueOf(verifierResponse.getResults()[0].getReceived().replaceAll("[\\[\\]]", "").replaceAll("\"", "").trim().toUpperCase());

    return move;

  }

  // Generate a game session id for every new game session
  private String generateID() {
    return UUID.randomUUID().toString();
  }

  // Convert the ArrayList of bot moves to a string form
  // representing list/array in the three available languages
  private String convertMoveList(ArrayList<Move> moveList) {
    
    String moves = "[";
    
    for(int i = 0; i < moveList.size(); i++) {
      Move move = moveList.get(i); 
      moves += "'" + move.toString() + "',";
    }
    
    // Remove the last comma
    if(moves.length() > 1) {
      moves = moves.substring(0, moves.length() - 1);
    }
    
    moves += "]";
    
    return moves;
    
  }

  private String base64Encode(String original) {

    byte[] encoded = Base64.encodeBase64(original.getBytes());

    return new String(encoded);

  }

  private int calculateRating(double score, int playerBotRating, int aiBotRating) {
    if(playerBotRating > 2400 && aiBotRating > 2400) {
      return playerBotRating + eloFormula(32, score, playerBotRating, aiBotRating);
    } else if(playerBotRating < 2401 || aiBotRating < 2401) {
      return playerBotRating + eloFormula(24, score, playerBotRating, aiBotRating);
    } else {
      return playerBotRating + eloFormula(16, score, playerBotRating, aiBotRating);
    }
  }
	
  private int eloFormula(int kFactor, double score, int playerBotRating, int aiBotRating) {
    double difference = (aiBotRating - playerBotRating)/400.0;
    double power10 = Math.pow(10.0, difference);
    double rawResult = kFactor * (score - (1/(power10+1)));
    int result = (int) Math.ceil(rawResult);
    return result;
  }
  
}