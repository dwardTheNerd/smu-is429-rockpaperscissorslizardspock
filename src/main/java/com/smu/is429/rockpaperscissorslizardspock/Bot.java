package com.smu.is429.rockpaperscissorslizardspock;

public class Bot {

  private int id;
  private String name, code;
  private Language language;

  public Bot(int id, String name, String code, Language language) {

    this.id = id;
    this.name = name;
    this.code = code;
    this.language = language;

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

}