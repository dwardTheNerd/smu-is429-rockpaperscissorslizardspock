package com.smu.is429.rockpaperscissorslizardspock;

public class GameSession {
  
  private String id;
  private int roundNo;
  private Move playerMove, aiMove;
  private int hasPlayerWon;
  private int playerBotId, aiBotId;
  
  public GameSession(String id, int roundNo, Move playerMove, Move aiMove, int hasPlayerWon) {
    this.id = id;
    this.roundNo = roundNo;
    this.playerMove = playerMove;
    this.aiMove = aiMove;
    this.hasPlayerWon = hasPlayerWon;
  }

  public GameSession(String id, int roundNo, int playerBotId, int aiBotId, Move playerMove, Move aiMove, int hasPlayerWon) {
    this.id = id;
    this.roundNo = roundNo;
    this.playerBotId = playerBotId;
    this.aiBotId = aiBotId;
    this.playerMove = playerMove;
    this.aiMove = aiMove;
    this.hasPlayerWon = hasPlayerWon;
  }  

  public String getId() {
    return id;
  }

  public int getRoundNo() {
    return roundNo;
  }

  public Move getPlayerMove() {
    return playerMove;
  }

  public Move getAiMove() {
    return aiMove;
  }

  public int getHasPlayerWon() {
    return hasPlayerWon;
  }

  public int getPlayerBotId() {
    return playerBotId;
  }

  public int getAiBotId() {
    return aiBotId;
  }

}