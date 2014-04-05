package com.smu.is429.rockpaperscissorslizardspock;

public class VerifyServiceResponse {

  private String errors;
  private boolean solved;
  private VerifyServiceResponseResults results[];

  public void setErrors(String errors) {
    this.errors = errors;
  }

  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  public void setResults(VerifyServiceResponseResults results[]) {
    this.results = results;
  }

  public String getErrors() {
    return errors;
  }

  public boolean getSolved() {
    return solved;
  }

  public VerifyServiceResponseResults[] getResults() {
    return results;
  }

}