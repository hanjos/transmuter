package transmuter.util.exception;

public class InaccessibleObjectTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Object object;

  public InaccessibleObjectTypeException(Object object) {
    super("Methods on " + object + " cannot be accessed via reflection");
    this.object = object;
  }

  public Object getObject() {
    return object;
  }
}
