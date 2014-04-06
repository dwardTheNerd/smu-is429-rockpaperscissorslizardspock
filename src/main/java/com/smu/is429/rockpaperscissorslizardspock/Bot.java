package com.smu.is429.rockpaperscissorslizardspock;

public class Bot {

  private int id;
  private String name, code;
  private Language language;
  private int eloRating = 1400;
  private int win = 0;
  private int lose = 0;
  private int draw = 0;
  private String userId;

  public Bot(int id, String name, String code, Language language, int eloRating, int win, int lose, int draw, String userId) {

    this.id = id;
    this.name = name;
    this.code = code;
    this.language = language;
    this.eloRating = eloRating;
	this.win = win;
	this.lose = lose;
	this.draw = draw;
	this.userId = userId;
  };

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
  
  public int getEloRating() {
	return eloRating;
  }
  
  public int getWin() {
	return win;
  }
  
  public int getLose() {
	return lose;
  }
  
  public int getDraw() {
	return draw;
  }
  
  public String getUserId() {
	return userId;
  }
}