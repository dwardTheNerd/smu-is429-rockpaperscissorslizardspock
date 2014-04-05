package com.smu.is429.rockpaperscissorslizardspock;

public class VerifyServiceResponseResults {

  private String expected, received, call, printed;
  private boolean correct;

  public void setExpected(String expected) {
    this.expected = expected;
  }

  public void setReceived(String received) {
    this.received = received;
  }

  public void setCall(String call) {
    this.call = call;
  }

  public void setPrinted(String printed) {
    this.printed = printed;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
  }

  public String getExpected() {
    return expected;
  }

  public String getReceived() {
    return received;
  }

  public String getCall() {
    return call;
  }

  public String getPrinted() {
    return printed;
  }

  public boolean getCorrect() {
    return correct;
  }

}