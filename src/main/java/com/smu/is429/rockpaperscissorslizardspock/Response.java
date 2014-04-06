package com.smu.is429.rockpaperscissorslizardspock;

public class Response {

  private boolean success;
  private String message;
  private GameSession gameSession;
  private int totalScore;
  
  public Response() {
    this.success = false;
  }
  
  public void setSuccess(boolean success) {
    this.success = success;
  }
  
  public void setGameSession(GameSession gameSession) {
    this.gameSession = gameSession;
  }
  
  public void setTotalScore(int totalScore) {
   this.totalScore = totalScore; 
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public boolean getSuccess() {
    return success;
  }

  public String getMessage() {
    return message; 
  }
  
  public GameSession getGameSession() {
    return gameSession;
  }

  public int getTotalScore() {
    return totalScore;
  }
}