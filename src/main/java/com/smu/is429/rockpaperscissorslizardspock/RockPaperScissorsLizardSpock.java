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

@Api(description = "API for RockPaperScissorsLizardSpock game", name = "rockpaperscissorslizardspock", version = "v1")
public class RockPaperScissorsLizardSpock {

  private final int NUMBER_OF_ROUNDS = 10;

  private final Judge judge;

  // Constructor
  public RockPaperScissorsLizardSpock() {

    judge = new Judge();    

  }

  // Start a new game
  @ApiMethod(name="newGame", path="newGame")
  public Response newGame(@Named("playerBotName") String playerBotName, @Named("playerBotCode") String playerBot, @Named("aiBotId") int aiBotId, @Named("language") Language language) {

    // Need to decode code first before we can process it
    playerBot = URLDecoder.decode(playerBot);

    RockPaperScissorsLizardSpockDAO gameDAO = null;

    Response response = null;
    
    // Retrieve ai bot code from the database
    try {

      gameDAO = new RockPaperScissorsLizardSpockDAO();
      Bot aiBot = gameDAO.getBot(aiBotId);

      // Invoking provided verify service
      Gson gson = new Gson();
      VerifyServiceResponse playerResponse = gson.fromJson(testCode(playerBot, language), VerifyServiceResponse.class);

      // Return error message if there is one
      if(playerResponse.getErrors() != null) {
        response = new Response();
        response.setSuccess(false);
        response.setMessage(playerResponse.getErrors());
        return response;     
      }

      VerifyServiceResponse aiResponse = gson.fromJson(testCode(aiBot.getCode(), aiBot.getLanguage()), VerifyServiceResponse.class);

      if(aiResponse.getErrors() != null) {
        response = new Response();
        response.setSuccess(false);
        response.setMessage(aiResponse.getErrors());
        return response;      
      }

      // No errors with code at this point so we proceed!
      // Next up we need to validate the moves made by the bots

      Move playerMove, aiMove = null;

      // Because we are using enum to specify the valid moves, if player or ai bot method return a value
      // that is not inside our specified values in enum, it will throw an exception.
      // And just in case the move returned is not in uppercase and/or it possess other extra characters, we strip it
      playerMove = Move.valueOf(playerResponse.getResults()[0].getReceived().replaceAll("[\\[\\]]", "").replaceAll("\"", "").trim().toUpperCase());
      aiMove = Move.valueOf(aiResponse.getResults()[0].getReceived().replaceAll("[\\[\\]]", "").replaceAll("\"", "").trim().toUpperCase());

      // At this stage, player's bot has been thoroughly validated.
      // Now we can insert player's bot code into database
      int playerBotId = playerBotId = gameDAO.insertBot(playerBotName, playerBot, language);

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

      // Invoking provided verify service
      Gson gson = new Gson();
      VerifyServiceResponse playerResponse = gson.fromJson(testCode(playerBot.getCode(), playerBot.getLanguage()), VerifyServiceResponse.class);

      // Return error message if there is one
      if(playerResponse.getErrors() != null) {
        response = new Response();
        response.setSuccess(false);
        response.setMessage(playerResponse.getErrors());
        return response;      
      }

      VerifyServiceResponse aiResponse = gson.fromJson(testCode(aiBot.getCode(), aiBot.getLanguage()), VerifyServiceResponse.class);

      if(aiResponse.getErrors() != null) {
        response = new Response();
        response.setSuccess(false);
        response.setMessage(aiResponse.getErrors());
        return response;     
      }

      Move playerMove, aiMove = null;

      playerMove = Move.valueOf(playerResponse.getResults()[0].getReceived().replaceAll("[\\[\\]]", "").replaceAll("\"", "").trim().toUpperCase());
      aiMove = Move.valueOf(aiResponse.getResults()[0].getReceived().replaceAll("[\\[\\]]", "").replaceAll("\"", "").trim().toUpperCase());

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

          response.setMessage("Congratulations! Your bot has won the game!");

        } else if(totalScore == 0) {

          response.setMessage("Your bot draw the game! Don't be sad, try again!");

        } else {

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

  private String testCode(String code, Language language) {

    String urlString = "http://162.222.183.53/";
    String parameters = "jsonrequest=";
    String tests = "";

    if(language == Language.PYTHON) {

      urlString += "python";
      tests = ">>> play_game()\\n'ANYTHING'";
      parameters += "{\"tests\":\""+ tests +"\",\"solution\":\"" + code + "\"}";

    } else if(language == Language.RUBY) {

      urlString += "ruby";
      tests = "assert_equal('ANYTHING',play_game())";
      parameters += "{\"tests\":\""+ tests +"\",\"solution\":\"" + code + "\"}";

    } else {   // Any other language values we default to javascript

      urlString += "js";
      tests = "assert_equal('ANYTHING',playGame())";
      parameters += "{\"tests\":\""+ tests +"\",\"solution\":\"" + URLEncoder.encode(code) + "\"}";

    }

    try {

      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      // Configuring request header
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Content-Length", Integer.toString(parameters.length()));

      // Send post request
     conn.setDoInput(true);
     conn.setDoOutput(true);
     DataOutputStream output = new DataOutputStream(conn.getOutputStream());
     output.writeBytes(parameters);
     output.flush();
     output.close();

     int responseCode = conn.getResponseCode();

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
        return sb.toString();
      default:
        return "{'errors':'Unable to verify code now. Please try again.'}";

     }

    } catch(Exception ex) {
      return "{'errors':'" + ex.getMessage() + "'}";
    }

  }

  // Generate a game session id for every new game session
  private String generateID() {
    return UUID.randomUUID().toString();
  }

}