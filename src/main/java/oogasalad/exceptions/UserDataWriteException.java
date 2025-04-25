package oogasalad.exceptions;

public class UserDataWriteException extends Exception {

  public UserDataWriteException(String message) {
    super(message);
  }

  public UserDataWriteException(String message, Throwable cause) {
    super(message, cause);
  }

}
