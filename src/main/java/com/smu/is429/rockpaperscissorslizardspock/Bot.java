package com.smu.is429.rockpaperscissorslizardspock;

public class Bot {

  private int id;
  private String name, code;
  private Language language;
  private int level = -1;
  private int winCount, lossCount, drawCount, elo;

  public Bot(int id, String name, int level) {

    this.id = id;
    this.name = name;
    this.level = level;

  }

  public Bot(int id, String name, String code, Language language, int level, int winCount, int lossCount, int drawCount, int elo) {

    this.id = id;
    this.name = name;
    this.code = code;
    this.language = language;
    this.level = level;
    this.winCount = winCount;
    this.drawCount = drawCount;
    this.lossCount = lossCount;
    this.elo = elo;
    
  }

  public int getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public Language getLanguage() {
    return language;
  }

  public String getName() {
    return name;
  }

  public int getLevel() {
    return level;
  }

  public int getWinCount() {
    return winCount; 
  }
  
  public int getLossCount() {
    return lossCount; 
  }
  
  public int getDrawCount() {
    return drawCount; 
  }
  public int getElo() {
    return elo; 
  }
  
}