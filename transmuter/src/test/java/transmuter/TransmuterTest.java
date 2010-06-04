package transmuter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static transmuter.type.TypeToken.ValueType.DOUBLE;
import static transmuter.util.ObjectUtils.areEqual;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.InvalidParameterTypeException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.NoCompatibleConvertersFoundException;
import transmuter.exception.PairIncompatibleWithBindingException;
import transmuter.exception.SameClassConverterCollisionException;
import transmuter.exception.TooManyConvertersFoundException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.util.Binding;
import transmuter.util.Pair;

public class TransmuterTest {
  private static final TypeToken<ArrayList<String>> ARRAYLIST_OF_STRING = new TypeToken<ArrayList<String>>() {};
  private static final TypeToken<List<String>> LIST_OF_STRING = new TypeToken<List<String>>() {};
  private Transmuter t;
  private Map<Pair, Binding> map;

  public static final class GenericConverter {
    @Converter
    public <T> T nonNull(T o) {
      if(o == null)
        throw new IllegalArgumentException();
      
      return o;
    }
  }

  public static final class VarargConverter {
    @Converter
    public String stringifyArray(Object... o) {
      return String.valueOf(o);
    }
  }

  public static final class FlawedConverter {
    @Converter
    public boolean intraClassCollision1(int i) {
      return (i == 0) ? false : true;
    }
    
    @Converter
    public boolean intraClassCollision2(int i) {
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
    public String extraClassCollision(double d) {
      return String.valueOf(d);
    }
    
    @Converter
    public void voidAndTooManyParameters(int a, int b, int c, int d) {
      // empty block
    }
  }

  public static final class MultipleConverter {
    @Converter
    public String converter(double d) {
      return "double: " + d;
    }
    
    @SuppressWarnings("serial")
    @Converter
    public List<String> convert(final String s) {
      return new ArrayList<String>() {{ add(s); }};
    }
  }

  public static class StringConverter {
    @Converter
    public String stringify(Object object) {
      return String.valueOf(object);
    }
    
    @Override
    public boolean equals(Object o) {
      return o instanceof StringConverter;
    }
    
    @Override
    public String toString() {
     return "StringConverter!"; 
    }
  }
  
  public static class MultipleValidConverter {
    @Converter
    public String toString(List<String> l) {
      return String.valueOf(l);
    }
    
    @Converter
    public String toString(Serializable s) {
      return String.valueOf(s);
    }
  }
  
  @Before
  public void setUp() {
    t = new Transmuter();
    map = t.getConverterMap();
  }
  
  @Test
  public void registerAndIsRegisteredAndUnregister() {
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.register(new MultipleConverter());
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    t.unregister(DOUBLE.primitive, TypeToken.STRING);
    
    assertEquals(1, t.getConverterMap().size());
    assertFalse(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
  }
  
  @Test
  public void registerFlawedConverter() throws SecurityException, NoSuchMethodException {
    Object flawed = new FlawedConverter();
    
    assertTrue(t.getConverterMap().isEmpty());
    assertFalse(t.isRegistered(double.class, String.class));
    assertFalse(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    final Object working = new MultipleConverter();
    t.register(working);
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    
    try {
      t.register(flawed);
      fail();
    } catch(ConverterRegistrationException e) {
      final List<? extends Exception> causes = e.getCauses();
      final Class<?> flawedClass = flawed.getClass();
      
      assertEquals(7, causes.size());
      
      assertWrongParameterCount(causes,
          extractMethod(flawedClass, "tooManyParameters", Object.class, Object.class),
          1, 1);
      assertWrongParameterCount(causes, 
          extractMethod(flawedClass, "tooFewParameters"),
          1, 1);
      assertWrongParameterCount(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class),
          1, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAsReturnType", Object.class), 
          void.class, 1);
      assertInvalidReturnType(causes, 
          extractMethod(flawedClass, "voidAndTooManyParameters", int.class, int.class, int.class, int.class), 
          void.class, 1);
      assertSameClassConverterCollision(causes, 
          TypeToken.get(flawedClass), 
          new Pair(int.class, boolean.class), 
          Arrays.asList(
              extractMethod(flawedClass, "intraClassCollision1", int.class),
              extractMethod(flawedClass, "intraClassCollision2", int.class)),
          1);
      assertConverterCollision(causes, 
          new Pair(double.class, String.class), 
          Arrays.asList(
              extractMethod(flawedClass, "extraClassCollision", double.class),
              extractMethod(working.getClass(), "converter", double.class)),
          1);
      
      //e.printStackTrace();
    }
    
    assertEquals(2, t.getConverterMap().size());
    assertTrue(t.isRegistered(double.class, String.class));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
  }
  
  private Method extractMethod(Class<?> cls, String name, Class<?>... parameterTypes) 
  throws SecurityException, NoSuchMethodException {
    return cls.getMethod(name, parameterTypes);
  }
  
  private void assertWrongParameterCount(final List<? extends Exception> causes, 
      Method method, int expected, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != WrongParameterCountException.class)
        continue;
      
      WrongParameterCountException cause2 = (WrongParameterCountException) cause;
      if(areEqual(cause2.getMethod(), method)
      && cause2.getActual() == method.getParameterTypes().length
      && cause2.getExpected() == expected)
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertInvalidReturnType(final List<? extends Exception> causes, 
      Method method, Type returnType, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != InvalidReturnTypeException.class)
        continue;
      
      InvalidReturnTypeException cause2 = (InvalidReturnTypeException) cause;
      if(areEqual(cause2.getMethod(), method)
      && areEqual(cause2.getType(), returnType))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertSameClassConverterCollision(final List<? extends Exception> causes, 
      TypeToken<?> declaringType, Pair pair, List<Method> methods, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != SameClassConverterCollisionException.class)
        continue;
      
      SameClassConverterCollisionException cause2 = (SameClassConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), pair)
      && (cause2.getMethods().containsAll(methods) && methods.containsAll(cause2.getMethods()))
      && areEqual(cause2.getDeclaringType(), declaringType))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }
  
  private void assertConverterCollision(final List<? extends Exception> causes, 
      Pair pair, List<Method> methods, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != ConverterCollisionException.class)
        continue;
      
      ConverterCollisionException cause2 = (ConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), pair)
      && (cause2.getMethods().containsAll(methods) && methods.containsAll(cause2.getMethods())))
        count++;
    }
    
    assertEquals(expectedCount, count);
  }

  @Test
  public void registerNullAndEmptyObject() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(null); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new Object()); // nothing happens
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  // The class A must implement equals() for this to work properly
  @Test
  public void registerWithRedundantPut() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(Object.class, String.class));
    assertFalse(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertTrue(map.isEmpty());
    
    t.register(new StringConverter());
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
    
    t.register(new StringConverter());
    
    assertTrue(t.isRegistered(Object.class, String.class));
    assertTrue(t.isRegistered(TypeToken.OBJECT, TypeToken.STRING));
    assertEquals(1, map.size());
  }
  
  // TODO is this what should happen, or should an exception be thrown?
  @Test
  public void registerPrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      private String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerPackagePrivate() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerProtected() {
    assertFalse(t.isRegistered(Object.class, String.class));
    
    t.register(new Object() {
      @SuppressWarnings("unused") // just to shut up Eclipse's warnings
      @Converter
      protected String stringify(Object o) {
        return String.valueOf(o);
      }
    });
    
    assertFalse(t.isRegistered(Object.class, String.class));
  }
  
  @Test
  public void registerVararg() {
    assertFalse(t.isRegistered(Object[].class, String.class));
    
    t.register(new VarargConverter());
    
    assertTrue(t.isRegistered(Object[].class, String.class));
  }
  
  @SuppressWarnings("unchecked") // just to shut up Eclipse's warnings
  @Test
  public void registerGenericType() {
    assertTrue(t.getConverterMap().isEmpty());
    
    try {
      t.register(new GenericConverter());
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertTrue(((InvalidParameterTypeException) e.getCauses().get(0)).getType() instanceof TypeVariable);
      
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      assertTrue(((InvalidReturnTypeException) e.getCauses().get(1)).getType() instanceof TypeVariable);
    }
    
    assertTrue(t.getConverterMap().isEmpty());
  }
  
  @Test
  public void unregisterNullAndNonexistent() {
    t.register(new VarargConverter());
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(null); // nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
    
    t.unregister(boolean.class, int.class); // doesn't exist, nothing happens
    
    assertTrue(t.isRegistered(Object[].class, String.class));
    assertEquals(1, t.getConverterMap().size());
  }
  
  @Test
  public void isRegistered() {
    assertTrue(t.getConverterMap().isEmpty());
    
    t.register(new MultipleConverter());
    
    assertTrue(t.isRegistered(new Pair(double.class, String.class)));
    assertTrue(t.isRegistered(TypeToken.STRING, LIST_OF_STRING));
    assertFalse(t.isRegistered(String.class, List.class));
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
  public void converterMapWithNulls() throws SecurityException, NoSuchMethodException {
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
  public void converterMapWithRedundantPut() throws SecurityException, NoSuchMethodException {
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
  public void converterMapWithIncompatiblePairAndBinding() throws SecurityException, NoSuchMethodException {
    map.put(
        new Pair(String.class, double.class), 
        new Binding(new StringConverter(), StringConverter.class.getMethod("stringify", Object.class)));
  }
  
  @Test
  public void converterMapContainsNullKeyP() {
    assertFalse(map.containsKey(null));
  }
  
  @Test
  public void converterMapContainsKeyWithPrimitives() {
    t.register(new MultipleConverter());
    
    assertTrue(map.containsKey(new Pair(double.class, String.class)));
    assertTrue(map.containsKey(new Pair(Double.class, String.class)));
  }
  
  @Test
  public void convert() {
    t.register(new StringConverter());
    
    assertEquals("sbrubbles", t.convert("sbrubbles", Object.class, String.class));
    assertEquals("sbrubbles", t.convert("sbrubbles", String.class, String.class));
    assertEquals("1", t.convert(1, Object.class, String.class));
    assertEquals("null", t.convert(null, Object.class, String.class));
    
    t.register(new MultipleConverter());
    
    assertEquals("double: 2.0", t.convert(2.0, Double.class, String.class));
    assertArrayEquals(new Object[] { "sbrubbles" }, t.convert("sbrubbles", TypeToken.STRING, LIST_OF_STRING).toArray());
    assertEquals("2.0", t.convert(2.0, Object.class, String.class));
  }
  
  @Test
  public void convertUnknown() {
    try {
      t.convert("sbrubbles", Object.class, String.class);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new Pair(Object.class, String.class), e.getPair());
    } catch(Exception ex) {
      fail();
    }
  }
  
  @Test
  public void multipleValidConverters() {
    t.register(new MultipleValidConverter());
    try {
      t.convert(new ArrayList<String>(), ARRAYLIST_OF_STRING, TypeToken.STRING);
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING), e.getPair());
      assertEquals(2, e.getBindings().size());
    }
  }
  
  @Test
  public void getCompatibleConvertersFor() {
    assertTrue(t.getCompatibleConvertersFor(null).isEmpty());
    
    // TODO flesh this out
  }
  
  @Test
  public void getMostCompatibleConverterFor() {
    assertNull(t.getMostCompatibleConverterFor(null));
    
    // TODO flesh this out
  }
  
  @Test
  public void getConverterFor() {
    assertNull(t.getConverterFor(null));
    
    // TODO flesh this out
  }
}
