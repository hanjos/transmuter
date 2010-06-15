package transmuter;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.classOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.MultipleCausesException;
import transmuter.exception.NoCompatibleConvertersFoundException;
import transmuter.exception.PairIncompatibleWithBindingException;
import transmuter.exception.PairInstantiationException;
import transmuter.exception.TooManyConvertersFoundException;
import transmuter.type.TypeToken;

/**
 * A central provider of conversion operations and converter registry.
 * <p>
 * Although converter methods can still be individually called, a transmuter 
 * acts as a single point of access for conversion.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Transmuter {
  /**
   * A map used for converter registration, validating the prospective mapping before the actual insertion.
   * 
   *  @author Humberto S. N. dos Anjos
   */
  protected static class PairBindingMap extends HashMap<Pair, Binding> {
    private static final long serialVersionUID = 1L;

    public PairBindingMap() { /* empty block */ }

    /**
     * Validates the pair and the binding (using {@link #validatePut(Pair, Binding) validatePut}) before insertion, 
     * throwing an exception if a problem is found.
     * <p>
     * In particular, bindings cannot be overwritten; they must be specifically removed from this map before a new put
     * operation with {@code pair} can be done.
     * 
     * @return {@code null} if there was no previous binding for {@code pair}, or {@code binding} if it was already 
     * associated with {@code pair}.
     * @throws - all exceptions thrown by {@link #validatePut(Pair, Binding)}. 
     */
    @Override
    public Binding put(Pair pair, Binding binding) {
      return validatePut(pair, binding) 
           ? binding
           : super.put(pair, binding);
    }

    /**
     * Checks if the pair and the binding can be stored in this map.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code pair} nor {@code binding} can be {@code null}.</li>
     * <li>a pair must be buildable using {@code binding}.</li>
     * <li>{@code pair} must be assignable from {@code binding}'s pair.</li>
     * <li>this map must not have {@code pair} associated to a binding different than {@code binding}.</li>
     * </ul>
     * 
     * @param pair a pair.
     * @param binding a binding.
     * @return {@code true} if {@code pair} is already associated with {@code binding}, or {@code false} 
     * if there's no binding.
     * @throws IllegalArgumentException if either {@code pair} or {@code binding} are {@code null}.
     * @throws PairIncompatibleWithBindingException if {@code pair} and {@code binding} are not compatible.
     * @throws PairInstantiationException if {@code binding} cannot be used to extract a pair.
     * @throws ConverterCollisionException if this map already has a different binding associated to {@code pair}.
     * @see {@link #checkForCompatibility(Pair, Binding)}
     * @see {@link #checkForCollision(Pair, Binding)}
     */
    protected boolean validatePut(Pair pair, Binding binding) {
      // check if the binding matches the pair
      checkForCompatibility(pair, binding);
      
      // check for collisions here
      return checkForCollision(pair, binding);
    }

    /**
     * Checks if the pair and the binding are mutually compatible.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code pair} nor {@code binding} can be {@code null}.</li>
     * <li>a pair must be buildable using {@code binding}.</li>
     * <li>{@code pair} must be assignable from {@code binding}'s pair.</li>
     * </ul>
     * 
     * @param pair a pair.
     * @param binding a binding.
     * @throws IllegalArgumentException if either {@code pair} or {@code binding} are {@code null}.
     * @throws PairIncompatibleWithBindingException if {@code pair} and {@code binding} are not compatible.
     * @throws PairInstantiationException if {@code binding} cannot be used to extract a pair. 
     */
    protected void checkForCompatibility(Pair pair, Binding binding) 
    throws PairIncompatibleWithBindingException {
      nonNull(pair, "pair"); 
      nonNull(binding, "binding");
      
      if(! pair.isAssignableFrom(Pair.fromBinding(binding)))
        throw new PairIncompatibleWithBindingException(pair, binding);
    }

    /**
     * Checks if the pair and the binding can be stored in this map.
     * 
     * @param pair a pair.
     * @param binding a binding.
     * @return {@code true} if {@code pair} is already associated with {@code binding} in this map, or {@code false} 
     * if there's no binding.
     * @throws IllegalArgumentException if either {@code pair}, {@code binding} or {@code map} are {@code null}.
     * @throws ConverterCollisionException if this map already has a different binding associated to {@code pair}.
     * @see {@link #checkMapForCollision(Pair, Binding, Map)}.
     */
    protected boolean checkForCollision(Pair pair, Binding binding) 
    throws ConverterCollisionException {
      return checkMapForCollision(pair, binding, this);
    }
    
    /**
     * Checks if the pair and the binding can be stored in the map.
     * <p>
     * The restrictions are:
     * <ul>
     * <li>neither {@code pair} nor {@code binding} nor {@code map} can be {@code null}.</li>
     * <li>{@code map} must not have {@code pair} associated to a binding different than {@code binding}.</li>
     * </ul>
     * 
     * @param pair a pair.
     * @param binding a binding.
     * @param map a map.
     * @return {@code true} if {@code pair} is already associated with {@code binding} in {@code map}, or {@code false} 
     * if there's no binding in {@code map}.
     * @throws IllegalArgumentException if either {@code pair}, {@code binding} or {@code map} are {@code null}.
     * @throws ConverterCollisionException if this map already has a different binding associated to {@code pair}.
     */
    protected static boolean checkMapForCollision(Pair pair, Binding binding, Map<? extends Pair, ? extends Binding> map) 
    throws ConverterCollisionException {
      nonNull(pair, "pair");
      nonNull(binding, "binding");
      nonNull(map, "map");
      
      if(! map.containsKey(pair))
        return false;
      
      if(areEqual(binding, map.get(pair)))
        return true;
      
      // converter collision, throw up
      throw new ConverterCollisionException(pair, binding, map.get(pair));
    }
    
    @Override
    public void putAll(Map<? extends Pair, ? extends Binding> map) {
      if(map == null || map.isEmpty())
        return;
      
      super.putAll(map);
    }
    
    @Override
    public boolean containsKey(Object key) {
      if(key == null)
        return false;
      
      return super.containsKey(key);
    }
    
    @Override
    public Binding remove(Object key) {
      if(key == null)
        return null;
      
      return super.remove(key);
    }
  }
    
  protected static class TempPairBindingMap extends PairBindingMap {
    private static final long serialVersionUID = 1L;

    private Map<Pair, Binding> masterMap;

    public TempPairBindingMap(Map<Pair, Binding> masterMap) {
      this.masterMap = nonNull(masterMap);
    }
    
    @Override
    protected boolean checkForCollision(Pair pair, Binding binding) 
    throws ConverterCollisionException {
      return super.checkForCollision(pair, binding)
          || super.checkMapForCollision(pair, binding, getMasterMap());
    }
    
    public Map<Pair, Binding> getMasterMap() {
      return masterMap;
    }
  }
  
  private Map<Pair, Binding> converterMap;
  
  public Transmuter() {
    converterMap = new PairBindingMap();
  }
  
  // operations
  public <From, To> To convert(From from, Class<To> toType) {
    return convert(from, TypeToken.get(toType));
  }
  
  @SuppressWarnings("unchecked")
  public <From, To> To convert(From from, TypeToken<To> toType) {
    return convert(from, TypeToken.get((Class<From>) classOf(from)), toType);
  }

  public <From, To, SubFrom extends From> To convert(SubFrom from, Class<From> fromType, Class<To> toType) {
    return convert(from, TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  @SuppressWarnings("unchecked")
  public <From, To, SubFrom extends From> To convert(SubFrom from, TypeToken<From> fromType, TypeToken<To> toType) {
    return (To) convertRaw(from, fromType, toType);
  }

  protected Object convertRaw(Object from, TypeToken<?> toType) {
    return convertRaw(from, TypeToken.get(classOf(from)), toType);
  }
  
  protected Object convertRaw(Object from, TypeToken<?> fromType, TypeToken<?> toType) {
    nonNull(fromType, "fromType"); nonNull(toType, "toType");
    
    return getConverterFor(new Pair(fromType, toType)).invoke(from);
  }
  
  public void register(Object object) {
    if(object == null)
      return;
    
    Map<Pair, Binding> temp = new TempPairBindingMap(getConverterMap());
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converts.class))
        continue;
      
      try {
        temp.put(Pair.fromMethod(method, object.getClass()), new Binding(object, method));
      } catch (MultipleCausesException e) {
        exceptions.addAll(e.getCauses());
      } catch(Exception e) {
        exceptions.add(e);
      }
    }
    
    if(exceptions.size() > 0) // errors during registration
      throw new ConverterRegistrationException(exceptions);
    
    getConverterMap().putAll(temp);
  }
  
  public boolean isRegistered(Type fromType, Type toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  public boolean isRegistered(TypeToken<?> fromType, TypeToken<?> toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  public boolean isRegistered(Pair pair) {
    return getConverterMap().containsKey(pair);
  }
  
  public void unregister(Type fromType, Type toType) {
    unregister(new Pair(fromType, toType));
  }
  
  public void unregister(TypeToken<?> fromType, TypeToken<?> toType) {
    unregister(new Pair(fromType, toType));
  }
  
  public Binding unregister(Pair pair) {
    return getConverterMap().remove(pair);
  }
  
  // helper methods
  protected Binding getConverterFor(Pair pair) {
    if(pair == null)
      throw new NoCompatibleConvertersFoundException(pair);
    
    Binding converter = getMostCompatibleConverterFor(pair);
    if(converter != null)
      return converter;
    
    List<Binding> compatibleBindings = getCompatibleConvertersFor(pair);
    
    if(compatibleBindings.isEmpty())
      throw new NoCompatibleConvertersFoundException(pair);
    
    if(compatibleBindings.size() > 1)
      throw new TooManyConvertersFoundException(pair, compatibleBindings);
    
    return compatibleBindings.get(0);
  }

  protected Binding getMostCompatibleConverterFor(Pair pair) {
    if(pair == null)
      return null;
    
    // TODO determine a search algorithm for a "most compatible" pair
    // XXX parameterize it?
    
    return getConverterMap().get(pair);
  }
  
  protected List<Binding> getCompatibleConvertersFor(Pair pair) {
    List<Binding> compatibleBindings = new ArrayList<Binding>();
    if(pair == null)
      return compatibleBindings;
    
    for(Entry<Pair, Binding> entry : getConverterMap().entrySet()) {
      if(entry.getKey().isAssignableFrom(pair))
        compatibleBindings.add(entry.getValue());
    }
    
    return compatibleBindings;
  } 
  
  // properties
  protected Map<Pair, Binding> getConverterMap() {
    return converterMap;
  }
}
