package transmuter.exception;

import java.lang.reflect.Type;

import transmuter.type.TypeToken;
import transmuter.util.Pair;

public class SameClassConverterCollisionException extends ConverterCollisionException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(TypeToken<?> declaringType, Pair pair) {
    return "More than one converter for " + pair + " found in " + declaringType;
  }
  
  private TypeToken<?> declaringType;

  public SameClassConverterCollisionException(Type declaringType, Pair pair) {
    this(TypeToken.get(declaringType), pair);
  }

  public SameClassConverterCollisionException(Type declaringType, Pair pair, String message) {
    this(TypeToken.get(declaringType), pair, message);
  }

  public SameClassConverterCollisionException(TypeToken<?> declaringType, Pair pair) {
    this(declaringType, pair, buildMessage(declaringType, pair));
  }

  public SameClassConverterCollisionException(TypeToken<?> declaringType, Pair pair, String message) {
    super(pair, message);
    
    this.declaringType = declaringType;
  }

  public TypeToken<?> getDeclaringType() {
    return declaringType;
  }
}
