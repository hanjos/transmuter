package transmuter.type;

public class MissingTypeParameterException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private Class<?> baseClass;
  
  public MissingTypeParameterException(Class<?> baseClass) {
    this(baseClass, baseClass + " has no accessible type parameter!");
  }
  
  public MissingTypeParameterException(Class<?> baseClass, String message) {
    super(message);
    
    this.baseClass = baseClass;
  }

  public Class<?> getBaseClass() {
    return baseClass;
  }
}
