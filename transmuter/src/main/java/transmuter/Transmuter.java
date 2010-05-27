package transmuter;

import static transmuter.util.ObjectUtils.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.SameClassConverterCollisionException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.util.Binding;
import transmuter.util.Pair;

public class Transmuter {
  private Map<Pair, Binding> converterMap;
  
  public Transmuter() {
    converterMap = new HashMap<Pair, Binding>();
  }
  
  // operations
  public void register(Object object) {
    if(object == null)
      return;
    
    Map<Pair, Binding> temp = new HashMap<Pair, Binding>();
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converter.class))
        continue;
      
      try {
        temp.put(extractTypes(method, temp), new Binding(object, method));
      } catch(Exception e) {
        exceptions.add(e);
      }
    }
    
    if(exceptions.size() > 0) // errors during registration
      throw new ConverterRegistrationException(exceptions);
    
    converterMap.putAll(temp);
  }
  
  public boolean isRegistered(Pair pair) {
    if(pair == null)
      return false;
    
    return converterMap.containsKey(pair);
  }
  
  public boolean isRegistered(Type fromType, Type toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  public boolean isRegistered(TypeToken<?> fromType, TypeToken<?> toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  // helper operations
  // TODO varargs shouldn't be a problem, right?
  protected Pair extractTypes(Method method) {
    return extractTypes(method, new HashMap<Pair, Binding>());
  }
  
  protected Pair extractTypes(Method method, Map<Pair, Binding> temporaryBindings) {
    nonNull(method, "method"); 
    nonNull(temporaryBindings, "temporaryBindings");
    
    final Type[] genericParameterTypes = method.getGenericParameterTypes();
    final int parameterCount = genericParameterTypes.length;
    if(parameterCount == 0 || parameterCount > 1)
      throw new WrongParameterCountException(1, parameterCount);
    
    final Type genericReturnType = method.getGenericReturnType();
    if(TypeToken.ValueType.VOID.matches(genericReturnType))
      throw new InvalidReturnTypeException(genericReturnType);
    
    Pair pair = new Pair(
        genericParameterTypes[0], 
        genericReturnType);
    
    if(temporaryBindings.containsKey(pair))
      throw new SameClassConverterCollisionException(
          TypeToken.get(method.getDeclaringClass()), pair.getFromType(), pair.getToType());
    
    if(converterMap.containsKey(pair))
      throw new ConverterCollisionException(pair.getFromType(), pair.getToType());
    
    return pair;
  }
}
