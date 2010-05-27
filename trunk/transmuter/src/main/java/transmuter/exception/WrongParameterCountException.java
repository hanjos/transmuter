package transmuter.exception;

public class WrongParameterCountException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(int expected, int actual) {
    return "Expected " + expected + ", got " + actual;
  }

  private int actual;
  private int expected;
  
  public WrongParameterCountException(int expected, int actual) {
    this(expected, actual, buildMessage(expected, actual));
  }

  public WrongParameterCountException(int expected, int actual, String message) {
    super(message);
    
    this.expected = expected;
    this.actual = actual;
  }

  public int getExpected() {
    return expected;
  }
  
  public int getActual() {
    return actual;
  }
}
