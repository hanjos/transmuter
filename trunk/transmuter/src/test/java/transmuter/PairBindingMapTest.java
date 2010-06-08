package transmuter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import transmuter.Transmuter.PairBindingMap;
import transmuter.exception.ConverterCollisionException;
import transmuter.exception.PairIncompatibleWithBindingException;
import transmuter.mock.MultipleConverter;
import transmuter.mock.StringConverter;
import transmuter.type.TypeToken;
import transmuter.util.Binding;
import transmuter.util.Pair;

public class PairBindingMapTest {
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() {};
  
  private Transmuter t;
  private Map<Pair, Binding> map;

  @Before
  public void setUp() {
    t = new Transmuter();
    map = t.getConverterMap();
  }
  
  @Test
  public void converterMap() throws SecurityException, NoSuchMethodException {
    assertTrue(map.isEmpty());
    
    t.register(new MultipleConverter());
    
    assertEquals(2, map.size());
    assertTrue(map.containsKey(new Pair(double.class, String.class)));
    assertTrue(map.containsKey(new Pair(TypeToken.STRING, LIST_OF_STRING)));
    assertFalse(map.containsKey(new Pair(String.class, List.class)));
    
    Object multiple = new MultipleConverter();
    
    try {
      map.put(
          new Pair(double.class, String.class), 
          new Binding(
              multiple, 
              multiple.getClass().getMethod("converter", double.class)));
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(new Pair(double.class, String.class), e.getPair());
    }
    
    Map<Pair, Binding> temp = new HashMap<Pair, Binding>();
    temp.put(
        new Pair(double.class, String.class), 
        new Binding(
            multiple, 
            multiple.getClass().getMethod("converter", double.class)));
    temp.put(
        new Pair(TypeToken.STRING, LIST_OF_STRING), 
        new Binding(
            multiple, 
            multiple.getClass().getMethod("convert", String.class)));
    
    try {
      map.putAll(temp);
      fail();
    } catch(ConverterCollisionException e) { //  only the first exception
      // TODO no way of knowing which error comes first, what to do?
    }
  }
  
  @Test
  public void nulls() throws SecurityException, NoSuchMethodException {
    Object o = new MultipleConverter();
    
    try {
      map.put(null, new Binding(o, o.getClass().getMethod("converter", double.class)));
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    try {
      map.put(new Pair(double.class, String.class), null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    assertTrue(map.isEmpty());
    
    map.putAll(null);
    
    assertTrue(map.isEmpty());
    
    map.putAll(new HashMap<Pair, Binding>());
    
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void redundantPut() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertTrue(map.isEmpty());
    
    final StringConverter a = new StringConverter();
    final Method stringify = StringConverter.class.getMethod("stringify", Object.class);
    map.put(new Pair(Object.class, String.class), new Binding(a, stringify));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
    
    map.put(new Pair(Object.class, String.class), new Binding(a, stringify));
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertEquals(1, map.size());
  }
  
  @Test(expected = PairIncompatibleWithBindingException.class)
  public void incompatiblePairAndBinding() throws SecurityException, NoSuchMethodException {
    map.put(
        new Pair(String.class, double.class), 
        new Binding(new StringConverter(), StringConverter.class.getMethod("stringify", Object.class)));
  }
  
  @Test
  public void containsNullKey() {
    assertFalse(map.containsKey(null));
  }
  
  @Test
  public void containsKeyWithPrimitives() {
    t.register(new MultipleConverter());
    
    assertTrue(map.containsKey(new Pair(double.class, String.class)));
    assertTrue(map.containsKey(new Pair(Double.class, String.class)));
  }
  
  @Test
  public void checkForCollision() throws SecurityException, NoSuchMethodException {
    PairBindingMap pbm = (PairBindingMap) map;
    
    StringConverter converter = new StringConverter();
    Pair pair = new Pair(Object.class, String.class);
    Binding stringify = new Binding(converter, StringConverter.class.getMethod("stringify", Object.class));
    Binding toString = new Binding(converter, StringConverter.class.getMethod("toString"));
    
    assertFalse(pbm.checkForCollision(pair, stringify));
    assertFalse(pbm.checkForCollision(pair, toString));
    
    pbm.put(pair, stringify);
    
    assertTrue(pbm.checkForCollision(pair, stringify));
    
    try {
      pbm.checkForCollision(pair, toString);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(pair, e.getPair());
      assertTrue(e.getBindings().containsAll(Arrays.asList(stringify, toString)));
      assertTrue(Arrays.asList(stringify, toString).containsAll(e.getBindings()));
    }
    
    try {
      pbm.checkForCollision(null, stringify);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      pbm.checkForCollision(pair, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    try {
      pbm.checkForCollision(pair, stringify, null);
      fail();
    } catch(IllegalArgumentException e) {
      // empty block
    }
    
    pbm.clear();
    
    assertFalse(pbm.checkForCollision(pair, stringify));
    assertFalse(pbm.checkForCollision(pair, toString));
    
    Map<Pair, Binding> noChecking = new HashMap<Pair, Binding>();
    noChecking.put(pair, toString);
    
    assertFalse(pbm.checkForCollision(pair, toString, pbm));
    assertTrue(pbm.checkForCollision(pair, toString, noChecking));
    
    assertFalse(pbm.checkForCollision(pair, stringify));
    
    try {
      pbm.checkForCollision(pair, stringify, noChecking);
      fail();
    } catch(ConverterCollisionException e) {
      assertEquals(pair, e.getPair());
      assertTrue(Arrays.asList(stringify, toString).containsAll(e.getBindings()));
      assertTrue(e.getBindings().containsAll(Arrays.asList(stringify, toString)));
    }
  }
}
