package com.smu.is429.rockpaperscissorslizardspock;

public enum Language {
  JAVASCRIPT("JAVASCRIPT"), PYTHON("PYTHON"), RUBY("RUBY");
  
  private String value;
  private Language(String value) {
    this.value = value; 
  }
  
  @Override
  public String toString() {
     return value; 
  }
  
}