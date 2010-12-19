package com.googlecode.transmuter.converter;

import java.lang.reflect.Method;

import com.googlecode.transmuter.Transmuter;
import com.googlecode.transmuter.converter.exception.ConverterTypeInstantiationException;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

/**
 * A {@link Binding binding} which has a {@link ConverterType converter type}, and so can safely be used with a 
 * {@link Transmuter transmuter}.
 * 
 * @author Humberto S. N. dos Anjos
 */
// TODO improve prose
public class Converter extends Binding {
  /* This converter's type. */
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
   */
  @Override
  protected Notification initialize(Object instance, Method method) {
    // do all previous validation...
    Notification notification = super.initialize(instance, method);
    
    // ...and try to extract the type from the given arguments
    try {
      this.type = ConverterType.from(instance, method);
    } catch (ConverterTypeInstantiationException e) {
      // problems found; snitch immediately 
      notification.report(e.getCauses());
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
