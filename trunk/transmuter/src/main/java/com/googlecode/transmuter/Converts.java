package com.googlecode.transmuter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.exception.ConverterProviderException;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

/**
 * Marks a method as a prospective converter method. 
 * <p>
 * Not all methods can be converter methods. This annotation should be used only on methods which can be successfully
 * {@linkplain Provider provided}. 
 * 
 * @author Humberto S. N. dos Anjos
 * @see Provider
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Converts {
  /**
   * This provider scans a given object for all public methods marked with the {@link Converts} annotation, 
   * {@linkplain Converter binding} them with the given object and making {@linkplain Iterator iterators} available  
   * as per {@link Iterable} protocol.
   * <p>
   * Any errors encountered during the extraction process will be bundled together and thrown as a single 
   * {@link ConverterProviderException} exception. In that case, no converters from the given object will be available, 
   * even if they're valid.
   * 
   * @author Humberto S. N. dos Anjos
   */
  public static class Provider implements Iterable<Converter> {
    private List<Converter> converters;
    
    /**
     * Scans the given object for public methods marked with @Converts, which will be assumed to be valid 
     * converter methods and made available as {@link Converter converters} via {@link #iterator()}.
     * <p>
     * If the given object is null, or has no @Converts-marked public methods, this constructor will end successfully 
     * and an empty iterator will be created.  
     * 
     * @param object an object with presumed converter methods.
     * @throws ConverterProviderException thrown if any errors are found during the scan and extraction process.
     */
    public Provider(Object object) throws ConverterProviderException {
      this.converters = new ArrayList<Converter>();
      
      extractConvertersFrom(object);
    }

    private void extractConvertersFrom(Object object) throws ConverterProviderException {
      if(object == null)
        return; // nothing to do here
      
      List<Exception> exceptions = new ArrayList<Exception>();
      
      // all public methods
      for(Method method : object.getClass().getMethods()) {
        if(! method.isAnnotationPresent(Converts.class))
          continue;
        
        try {
          converters.add(new Converter(object, method));
        } catch (MultipleCausesException e) {
          exceptions.addAll(e.getCauses());
        } catch(Exception e) {
          exceptions.add(e);
        }
      }
      
      if(! exceptions.isEmpty())
        throw new ConverterProviderException(exceptions);
    }
    
    @Override
    public Iterator<Converter> iterator() {
      return converters.iterator();
    }
  }
}