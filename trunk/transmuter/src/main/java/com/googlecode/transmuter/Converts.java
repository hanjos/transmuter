package com.googlecode.transmuter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.exception.ConverterProviderException;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

/**
 * May be used to mark a method as a prospective converter method. 
 * <p>
 * Not all methods can be converter methods. This annotation should be used only on methods which can be 
 * {@linkplain EagerProvider successfully} {@linkplain LazyProvider provided}. 
 * 
 * @author Humberto S. N. dos Anjos
 * @see EagerProvider
 * @see LazyProvider
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Converts {
  /**
   * Scans a given object for all public methods marked with the {@link Converts} annotation, 
   * {@linkplain Converter binding} them with the given object and making {@linkplain Iterator iterators} available  
   * as per {@link Iterable} protocol.
   * <p>
   * Any errors encountered during the extraction process will be bundled together and thrown as a single 
   * {@link ConverterProviderException} exception. In that case, no converters from the given object will be available, 
   * even if they're valid.
   * <p>
   * This provider scans the entire object upon construction, storing all converters for later iteration.
   * 
   * @author Humberto S. N. dos Anjos
   */
  public static class EagerProvider implements Iterable<Converter> {
    private List<Converter> converters;
    
    /**
     * Scans the given object for public methods marked with {@code @Converts}, which will be assumed to be valid 
     * converter methods and made available as {@linkplain Converter converters} via {@link #iterator()}.
     * <p>
     * If the given object is null, or has no {@code @Converts}-marked public methods, this constructor will end 
     * successfully and an empty iterator will be created.  
     * 
     * @param source an object with presumed converter methods.
     * @throws ConverterProviderException thrown if any errors are found during the scan and extraction process.
     */
    public EagerProvider(Object source) throws ConverterProviderException {
      this.converters = Collections.unmodifiableList(extractConvertersFrom(source));
    }

    private List<Converter> extractConvertersFrom(Object source) throws ConverterProviderException {
      List<Converter> converters = new ArrayList<Converter>();
      
      if(source == null)
        return converters; // nothing to do here
      
      List<Exception> exceptions = new ArrayList<Exception>();
      
      // all public methods
      for(Method method : source.getClass().getMethods()) {
        if(! method.isAnnotationPresent(Converts.class))
          continue;
        
        try {
          converters.add(new Converter(source, method));
        } catch (MultipleCausesException e) {
          exceptions.addAll(e.getCauses());
        } catch(Exception e) {
          exceptions.add(e);
        }
      }
      
      if(! exceptions.isEmpty())
        throw new ConverterProviderException(exceptions);
      
      // if we're here, then it's safe
      return converters;
    }
    
    @Override
    public Iterator<Converter> iterator() {
      return converters.iterator();
    }
  }

  /**
   * Scans a given object for all public methods marked with the {@link Converts} annotation, 
   * {@linkplain Converter binding} them with the given object and making {@linkplain Iterator iterators} available  
   * as per {@link Iterable} protocol.
   * <p>
   * Any errors encountered during the extraction process will be bundled together and thrown as a single 
   * {@link ConverterProviderException} exception. In that case, no converters from the given object will be available, 
   * even if they're valid.
   * <p>
   * The extraction process happens only during iteration, so this provider may be instanced without a hitch, only to 
   * fail later.
   * 
   * @author Humberto S. N. dos Anjos
   */
  public static class LazyProvider implements Iterable<Converter> {
    private Object source;
    private static final List<Converter> EMPTY_LIST = Collections.emptyList();
    
    /**
     * Stores the given object for later scanning.  
     * 
     * @param source an object with presumed converter methods.
     */
    public LazyProvider(Object source) {
      this.source = source;
    }

    @Override
    public Iterator<Converter> iterator() {
      return (source != null)
           ? new LazyIterator()
           : EMPTY_LIST.iterator();
    }
    
    /* (non-Javadoc)
     * The iterator which does all the work. Scans the object in search of convertible methods.
     */
    private class LazyIterator implements Iterator<Converter> {
      private int cursor;
      private Method[] methods;
      
      @SuppressWarnings("synthetic-access")
      public LazyIterator() {
        methods = source.getClass().getMethods();
        cursor = nextConverter(-1);
      }

      @Override
      public boolean hasNext() {
        return 0 <= cursor && cursor < methods.length;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Converter next() {
        if(! hasNext()) // end of iteration
          throw new NoSuchElementException();
        
        try {
          return new Converter(source, methods[cursor]);
        } catch (MultipleCausesException e) {
          throw new ConverterProviderException(e.getCauses());
        } catch (Exception e) {
          throw new ConverterProviderException(e);
        } finally {
          cursor = nextConverter(cursor); // update the cursor!
        }
      }

      /* Returns the index of the next converter, or -1 if there are no more left. */
      private int nextConverter(int cursor) {
        for(int i = cursor + 1; i < methods.length; i++) {
          if(! methods[i].isAnnotationPresent(Converts.class))
            continue;
          
          return i;
        }
        
        return -1;
      }
      
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    }  
  }
}