package transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.SameClassConverterCollisionException;
import transmuter.exception.WrongParameterCountException;

public class TransmuterTest {
  private Transmuter t;
  
  @Before
  public void setUp() {
    t = new Transmuter();
  }
  
  @Test
  public void registerAndIsRegistered() {
    assertFalse(t.isRegistered(int.class, boolean.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      public boolean toBoolean(int i) {
        return (i == 0) ? false : true;
      }
    });
    
    assertTrue(t.isRegistered(int.class, boolean.class));
  }
  
  @Test
  public void registerFlawedConverter() {
    @SuppressWarnings("unused")
    Object flawed = new Object() {
      @Converter
      public boolean toBoolean1(int i) {
        return (i == 0) ? false : true;
      }
      
      @Converter
      public boolean toBoolean2(int i) {
        return i % 2 == 0;
      }
      
      @Converter
      public void voidAsReturnType(Object whatever) {
        // empty block
      }
      
      @Converter
      public Object tooManyParameters(Object a, Object b) {
        return null;
      }
      
      @Converter
      public Object tooFewParameters() {
        return null;
      }
      
      @Converter
      public int theOneWorkingConverter(boolean b) {
        return b ? 1 : 0;
      }
    };
    
    assertFalse(t.isRegistered(boolean.class, int.class));
    
    try {
      t.register(flawed);
      fail();
    } catch(ConverterRegistrationException e) {
      final List<? extends Exception> causes = e.getCauses();
      
      assertEquals(4, causes.size());
      assertTrue(causes.contains(
          new WrongParameterCountException(1, 0)));
      assertTrue(causes.contains(
          new WrongParameterCountException(1, 2)));
      assertTrue(causes.contains(
          new InvalidReturnTypeException(void.class)));
      assertTrue(causes.contains(
          new SameClassConverterCollisionException(flawed.getClass(), int.class, boolean.class)));
    }
    
    assertFalse(t.isRegistered(boolean.class, int.class));
  }
  
}
