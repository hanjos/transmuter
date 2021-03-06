package com.googlecode.transmuter.converter;

import com.googlecode.transmuter.core.Transmuter;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.exception.MultipleCausesException;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

import java.lang.reflect.Method;

/**
 * A {@linkplain Binding binding} which has a {@linkplain ConverterType converter type}, and so can safely be used 
 * by a {@linkplain Transmuter transmuter}.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Converter extends Binding {
  private ConverterType type;
  
  /**
   * Makes a new {@code Converter} object holding a static method.
   * 
   * @param method a static method object. 
   * @throws ObjectInstantiationException if the given method is not deemed valid.
   * @see #initialize(Object, Method) 
   */
  public Converter(Method method) throws ObjectInstantiationException {
    super(method);
  }

  /**
   * Constructs a new {@code Converter} object.
   * 
   * @param instance an object.
   * @param method a method object.
   * @throws ObjectInstantiationException if the given instance, method, or their combination is not deemed valid.
   * @see #initialize(Object, Method)
   */
  public Converter(Object instance, Method method) throws ObjectInstantiationException {
    super(instance, method);
  }

  /**
   * On top of {@link Binding}'s validation, this class needs to see if the given method is a proper converter method,
   * i.e. it has a converter type.
   * 
   * @see Binding#tryInitialize(Object, Method)
   */
  @Override
  protected Notification tryInitialize(Object instance, Method method) {
    // do all previous validation...
    Notification notification = super.tryInitialize(instance, method);
    
    // ...and try to extract the type from the given arguments
    try {
      this.type = ConverterType.from(instance, method);
    } catch (MultipleCausesException e) {
      // errors were found; snitch immediately 
      notification.add(e.getCauses());
    } catch (Exception e) {
      // unpredicted exception; report it!
      notification.add(e);
    }
    
    return notification;
  }

  /**
   * Returns this instance's converter type.
   * 
   * @return this instance's converter type.
   */
  public ConverterType getType() {
    return type;
  }
}
