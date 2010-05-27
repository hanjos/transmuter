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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + actual;
    result = prime * result + expected;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    WrongParameterCountException other = (WrongParameterCountException) obj;
    if(actual != other.actual)
      return false;
    if(expected != other.expected)
      return false;
    return true;
  }
}
