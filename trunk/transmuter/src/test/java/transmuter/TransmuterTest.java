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

  public static final class GenericMethodConverter {
    @Converts
    public <T> T nonNull(T o) {
      if(o == null)
        throw new IllegalArgumentException();
      
      return o;
    }
  }
  
  public static class GenericConverter<From, To> {
    @Converts
    public To convert(From from) {
      return (To) from;
    }
  }
  
  public static class PartialGenericConverter<From> extends GenericConverter<From, String> {
    @Override
    @Converts
    public String convert(From from) {
      return String.valueOf(from);
    }
  }
  
  public static class StringArrayToListStringConverter extends GenericConverter<String[], List<String>> {
    // empty block
  }

  public static final class VarargConverter {
    @Converts
    public String stringifyArray(Object... o) {
      return String.valueOf(o);
    }
  }

  public static final class FlawedConverter {
    @Converts
    public boolean intraClassCollision1(int i) {
      return (i == 0) ? false : true;
    }
    
    @Converts
    public boolean intraClassCollision2(int i) {
      return i % 2 == 0;
    }
    
    @Converts
    public void voidAsReturnType(Object whatever) {
      // empty block
    }
    
    @Converts
    public Object tooManyParameters(Object a, Object b) {
      return null;
    }
    
    @Converts
    public Object tooFewParameters() {
      return null;
    }
    
    @Converts
    public String extraClassCollision(double d) {
      return String.valueOf(d);
    }
    
    @Converts
    public void voidAndTooManyParameters(int a, int b, int c, int d) {
      // empty block
    }
  }

  public static final class MultipleConverter {
    @Converts
    public String converter(double d) {
      return "double: " + d;
    }
    
    @SuppressWarnings("serial")
    @Converts
    public List<String> convert(final String s) {
      return new ArrayList<String>() {{ add(s); }};
    }
  }

  public static class StringConverter {
    @Converts
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
    @Converts
    public String toString(List<String> l) {
      return String.valueOf(l);
    }
    
    @Converts
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
      assertConverterCollision(causes, 
          new Pair(int.class, boolean.class), 
          Arrays.asList(
              new Binding(flawed, extractMethod(flawedClass, "intraClassCollision1", int.class)),
              new Binding(flawed, extractMethod(flawedClass, "intraClassCollision2", int.class))),
          1);
      assertConverterCollision(causes, 
          new Pair(double.class, String.class), 
          Arrays.asList(
              new Binding(flawed, extractMethod(flawedClass, "extraClassCollision", double.class)),
              new Binding(working, extractMethod(working.getClass(), "converter", double.class))),
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
  
  private void assertConverterCollision(final List<? extends Exception> causes, 
      Pair pair, List<Binding> bindings, int expectedCount) {
    int count = 0;
    for(Exception cause : causes) {
      if(cause.getClass() != ConverterCollisionException.class)
        continue;
      
      ConverterCollisionException cause2 = (ConverterCollisionException) cause;
      if(areEqual(cause2.getPair(), pair)
      && (cause2.getBindings().containsAll(bindings) && bindings.containsAll(cause2.getBindings())))
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
      @Converts
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
      @Converts
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
      @Converts
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
  
  @Test
  public void registerGenericMethod() {
    assertTrue(t.getConverterMap().isEmpty());
    
    try {
      t.register(new GenericMethodConverter());
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
  public void registerMethodFromPartialGenericClass() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new PartialGenericConverter<List<String>>()); // no way of getting this information with Java
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(PartialGenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable);
      assertEquals("From", ((TypeVariable) ex.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new PartialGenericConverter()); // raw
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(1, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      final Method convertMethod = extractMethod(PartialGenericConverter.class, "convert", Object.class);
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(convertMethod, ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable);
      assertEquals("From", ((TypeVariable) ex.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
  }
  
  @Test
  public void registerMethodFromGenericClass() throws SecurityException, NoSuchMethodException {
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new GenericConverter<List<String>, String>()); // no way of getting this information with Java
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable);
      assertEquals("From", ((TypeVariable) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) e.getCauses().get(1);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable);
      assertEquals("To", ((TypeVariable) ex1.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
    
    try {
      t.register(new GenericConverter()); // raw
      fail();
    } catch(ConverterRegistrationException e) {
      assertEquals(2, e.getCauses().size());
      assertEquals(InvalidParameterTypeException.class, e.getCauses().get(0).getClass());
      assertEquals(InvalidReturnTypeException.class, e.getCauses().get(1).getClass());
      
      InvalidParameterTypeException ex = (InvalidParameterTypeException) e.getCauses().get(0);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex.getMethod());
      assertTrue(ex.getType() instanceof TypeVariable);
      assertEquals("From", ((TypeVariable) ex.getType()).getName());
      
      InvalidReturnTypeException ex1 = (InvalidReturnTypeException) e.getCauses().get(1);
      assertEquals(extractMethod(GenericConverter.class, "convert", Object.class), ex1.getMethod());
      assertTrue(ex1.getType() instanceof TypeVariable);
      assertEquals("To", ((TypeVariable) ex1.getType()).getName());
    }
    
    assertFalse(t.isRegistered(LIST_OF_STRING, TypeToken.STRING));
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
  public void multipleValidConverters() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    try {
      t.convert(new ArrayList<String>(), ARRAYLIST_OF_STRING, TypeToken.STRING);
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING), e.getPair());
      assertEquals(2, e.getBindings().size());
    
      assertTrue(e.getBindings().contains(
          new Binding(
              converter, 
              extractMethod(converter.getClass(), "toString", List.class))));
      assertTrue(e.getBindings().contains(
          new Binding(
              converter, 
              extractMethod(converter.getClass(), "toString", Serializable.class))));
    }
  }
  
  @Test
  public void getCompatibleConvertersFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    final List<Binding> compatible = t.getCompatibleConvertersFor(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING));
    assertEquals(2, compatible.size());
    assertTrue(compatible.contains(
        new Binding(
            converter, 
            extractMethod(converter.getClass(), "toString", List.class))));
    assertTrue(compatible.contains(
        new Binding(
            converter, 
            extractMethod(converter.getClass(), "toString", Serializable.class))));
    
    final List<Binding> serial = t.getCompatibleConvertersFor(new Pair(Serializable.class, String.class));
    assertEquals(1, serial.size());
    assertEquals(
        t.getMostCompatibleConverterFor(new Pair(Serializable.class, String.class)),
        serial.get(0));
    
    assertTrue(t.getCompatibleConvertersFor(null).isEmpty());
    assertTrue(t.getCompatibleConvertersFor(new Pair(Object.class, Integer.class)).isEmpty());
    
    // TODO flesh this out
  }
  
  @Test
  public void getMostCompatibleConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    assertEquals(
        new Binding(
          converter, 
          extractMethod(converter.getClass(), "toString", Serializable.class)),
        t.getMostCompatibleConverterFor(new Pair(Serializable.class, String.class)));
    assertEquals(
        new Binding(
          converter, 
          extractMethod(converter.getClass(), "toString", List.class)),
        t.getMostCompatibleConverterFor(new Pair(LIST_OF_STRING, TypeToken.STRING)));
    assertNull(t.getMostCompatibleConverterFor(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING)));
    assertNull(t.getMostCompatibleConverterFor(null));
    assertNull(t.getMostCompatibleConverterFor(new Pair(Object.class, Integer.class)));
     
    // TODO flesh this out
  }
  
  @Test
  public void getConverterFor() throws SecurityException, NoSuchMethodException {
    final MultipleValidConverter converter = new MultipleValidConverter();
    t.register(converter);
    
    assertEquals(
        new Binding(
          converter, 
          extractMethod(converter.getClass(), "toString", Serializable.class)),
        t.getConverterFor(new Pair(Serializable.class, String.class)));
    assertEquals(
        new Binding(
          converter, 
          extractMethod(converter.getClass(), "toString", List.class)),
        t.getConverterFor(new Pair(LIST_OF_STRING, TypeToken.STRING)));
    
    try {
      t.getConverterFor(null);
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertNull(e.getPair());
    }
    
    try {
      assertNull(t.getConverterFor(new Pair(Object.class, Integer.class)));
      fail();
    } catch(NoCompatibleConvertersFoundException e) {
      assertEquals(new Pair(Object.class, Integer.class), e.getPair());
    }
    
    try {
      t.getConverterFor(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING));
      fail();
    } catch(TooManyConvertersFoundException e) {
      assertEquals(new Pair(ARRAYLIST_OF_STRING, TypeToken.STRING), e.getPair());
      assertEquals(2, e.getBindings().size());
    
      assertTrue(e.getBindings().contains(
          new Binding(
              converter, 
              extractMethod(converter.getClass(), "toString", List.class))));
      assertTrue(e.getBindings().contains(
          new Binding(
              converter, 
              extractMethod(converter.getClass(), "toString", Serializable.class))));
    }
  }
  
  @Test
  public void inheritedGenericMethod() {
    TypeToken<String[]> ARRAY_OF_STRING = TypeToken.get(String[].class);
    
    assertFalse(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
    
    t.register(new StringArrayToListStringConverter());
    
    assertTrue(t.isRegistered(ARRAY_OF_STRING, LIST_OF_STRING));
  }
}
