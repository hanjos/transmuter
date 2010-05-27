/*
 * Adapted from google-gson's com.google.gson.reflect.TypeToken. Their license follows. 
 */

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package transmuter.type;

import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a generic type {@code T}.
 *
 * You can use this class to get the generic type for a class. For example,
 * to get the generic type for <code>Collection&lt;Foo&gt;</code>, you can use:
 * <p>
 * <code>Type typeOfCollectionOfFoo = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType()
 * </code>
 * 
 * <p>Assumes {@code Type} implements {@code equals()} and {@code hashCode()}
 * as a value (as opposed to identity) comparison.
 *
 * Also implements {@link #isAssignableFrom(Type)} to check type-safe
 * assignability.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Humberto S. N. dos Anjos
 * @param <T> a generic type
 */
public abstract class TypeToken<T> {
  /** A type token instance representing java.lang.Object. */
  public static final TypeToken<Object> OBJECT;
  /** A type token instance representing java.lang.String. */
  public static final TypeToken<String> STRING;
  
  static {
    OBJECT = TypeToken.get(Object.class);
    STRING = TypeToken.get(String.class);
  }
  
  /**
   * An enum-like class which represents Java's primitive data types 
   * (plus {@code void}). Each instance of this class holds two type tokens: 
   * one with a primitive type and the other with the matching wrapper type.
   * 
   * It behaves logically like an enum, except that Java's enums don't take 
   * generic parameters.
   * 
   * @param <T> a wrapper type or {@code Void}.
   */
  public static abstract class ValueType<T> {
    public static final ValueType<Boolean> BOOLEAN;
    public static final ValueType<Byte> BYTE;
    public static final ValueType<Character> CHARACTER;
    public static final ValueType<Double> DOUBLE;
    public static final ValueType<Float> FLOAT;
    public static final ValueType<Integer> INTEGER;
    public static final ValueType<Long> LONG;
    public static final ValueType<Short> SHORT;
    public static final ValueType<Void> VOID;
    
    private static Map<Class<?>, ValueType<?>> primitiveReverseMap;
    private static Map<Class<?>, ValueType<?>> wrapperReverseMap;
    
    static {
      // must be instanced first to enable the constructor to register the new instances
      primitiveReverseMap = new HashMap<Class<?>, ValueType<?>>();
      wrapperReverseMap = new HashMap<Class<?>, ValueType<?>>();
      
      BOOLEAN = new ValueType<Boolean>(boolean.class, Boolean.class) {
        @Override
        protected Boolean castValue(Object value) {
          throw new ClassCastException(value + " is not a boolean!");
        }
      };
      BYTE = new ValueType<Byte>(byte.class, Byte.class) {
        @Override
        protected Byte castValue(Object value) {
          if(value == null 
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not a byte!");
          
          if(CHARACTER.matches(value.getClass()))
            return (byte) ((Character) value).charValue();
          
          return ((Number) value).byteValue();
        }
      };
      CHARACTER = new ValueType<Character>(char.class, Character.class) {
        @Override
        protected Character castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not a char!");
          
          return (char) ((Number) value).intValue();
        }
      };
      DOUBLE = new ValueType<Double>(double.class, Double.class) {
        @Override
        protected Double castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not a double!");
          
          if(CHARACTER.matches(value.getClass()))
            return (double) ((Character) value).charValue();
          
          return ((Number) value).doubleValue();
        }
      };
      FLOAT = new ValueType<Float>(float.class, Float.class) {
        @Override
        protected Float castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not a float!");
          
          if(CHARACTER.matches(value.getClass()))
            return (float) ((Character) value).charValue();
          
          return ((Number) value).floatValue();
        }
      };
      INTEGER = new ValueType<Integer>(int.class, Integer.class) {
        @Override
        protected Integer castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not an int!");
          
          if(CHARACTER.matches(value.getClass()))
            return (int) ((Character) value).charValue();
          
          return ((Number) value).intValue();
        }
      };
      LONG = new ValueType<Long>(long.class, Long.class) {
        @Override
        protected Long castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not an int!");
          
          if(CHARACTER.matches(value.getClass()))
            return (long) ((Character) value).charValue();
          
          return ((Number) value).longValue(); 
        }
      };
      SHORT = new ValueType<Short>(short.class, Short.class) {
        @Override
        protected Short castValue(Object value) {
          if(value == null
          || BOOLEAN.matches(value.getClass()) 
          || VOID.matches(value.getClass()))
            throw new ClassCastException(value + " is not an int!");
          
          if(CHARACTER.matches(value.getClass()))
            return (short) ((Character) value).charValue();
          
          return ((Number) value).shortValue(); 
        }
      };
      VOID = new ValueType<Void>(void.class, Void.class) {
        @Override
        protected Void castValue(Object value) {
          if(value == null)
            return null;
          
          throw new ClassCastException(value + " is not a void!");
        }
      };
    }
    
    /** A type token representing the primitive type expressed in {@code T}. */
    public final TypeToken<T> primitive;
    
    /** A type token representing the wrapper type expressed in {@code T}. */
    public final TypeToken<T> wrapper;
    
    // no instancing going on without us knowing about it
    private ValueType(Class<T> primitive, Class<T> wrapper) {
      this.primitive = TypeToken.get(primitive);
      this.wrapper = TypeToken.get(wrapper);
      
      primitiveReverseMap.put(primitive, this);
      wrapperReverseMap.put(wrapper, this);
    }
    
    // utility methods
    @Override
    public String toString() {
      return "ValueType<" + primitive + ">";
    }
    
    // instance methods
    @SuppressWarnings("unchecked")
    public T cast(Object value) {
      ValueType<?> valueType = valueOf(
          value == null ? void.class : value.getClass());
      
      if(valueType == null)
        throw new ClassCastException("not a primitive nor a wrapper instance");
      
      if(this.equals(valueType))
        return (T) value;
      
      return castValue(value);
    }
    
    protected abstract T castValue(Object value);
    
    /**
     * @param type a generic type.
     * @return {@code true} if {@code type} represents the same value 
     * type as this instance.
     */
    public boolean matches(Type type) {
      return this == valueOf(type);
    }
    
    /**
     * @param token a type token.
     * @return {@code true} if {@code token} represents the same value 
     * type as this instance.
     */
    public boolean matches(TypeToken<?> token) {
      return this == valueOf(token);
    }
    
    // static methods
    /**
     * @param type a generic type.
     * @return {@code true} if {@code type} is a primitive type.
     */
    public static boolean isPrimitive(Type type) {
      return primitiveReverseMap.containsKey(type);
    }
    
    /**
     * @param token a type token.
     * @return {@code true} if {@code token} is a primitive type.
     */
    public static boolean isPrimitive(TypeToken<?> token) {
      return (token != null) && primitiveReverseMap.containsKey(token.type);
    }
    
    /**
     * @param type a generic type.
     * @return {@code true} if {@code type} is a wrapper type.
     */
    public static boolean isWrapper(Type type) {
      return wrapperReverseMap.containsKey(type);
    }
    
    /**
     * @param token a type token.
     * @return {@code true} if {@code token} is a wrapper type.
     */
    public static boolean isWrapper(TypeToken<?> token) {
      return (token != null) && wrapperReverseMap.containsKey(token.type);
    }
    
    /**
     * Returns the matching value type of the given type: the respective 
     * wrapper type if {@code type} is a primitive type; the respective 
     * primitive type if {@code type} is a wrapper type; or {@code null} if 
     * {@code type} is neither.
     * 
     * @param type a generic type.
     * @return one of the following, as a {@code Type}:
     * <ul>
     * <li>the respective wrapper type if {@code type} is primitive;</li>
     * <li>the respective primitive type if {@code type} is a wrapper;</li>
     * <li>{@code null} if {@code type} is neither.</li>
     * </ul>
     */
    public static Type matching(Type type) {
      if(type == null)
        return null;
      
      TypeToken<?> matching = matching(TypeToken.get(type));
      return (matching != null)
           ? matching.type
           : null;
    }
    
    /**
     * Returns the matching value type of the given type: the respective 
     * wrapper type if {@code type} is a primitive type; the respective 
     * primitive type if {@code type} is a wrapper type; or {@code null} if 
     * {@code type} is neither.
     * 
     * @param <E> the specific type {@code cls} represents. 
     * @param cls a class object.
     * @return one of the following, as a {@code Class&lt;E&gt;} object:
     * <ul>
     * <li>the respective wrapper type if {@code cls} is primitive;</li>
     * <li>the respective primitive type if {@code cls} is a wrapper;</li>
     * <li>{@code null} if {@code cls} is neither.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static <E> Class<E> matching(Class<E> cls) {
      return (Class<E>) matching((Type) cls);
    }
    
    /**
     * Returns the matching value type of the given type: the respective 
     * wrapper type if {@code type} is a primitive type; the respective 
     * primitive type if {@code type} is a wrapper type; or {@code null} if 
     * {@code type} is neither.
     * 
     * @param <E> the specific type {@code token} represents.
     * @param token a type token.
     * @return one of the following, as a type token:
     * <ul>
     * <li>the respective wrapper type if {@code token} is primitive;</li>
     * <li>the respective primitive type if {@code token} is a wrapper;</li>
     * <li>{@code null} if {@code token} is neither.</li>
     * </ul>
     */
    public static <E> TypeToken<E> matching(TypeToken<E> token) {
      ValueType<E> primitive = valueOf(token);
      if(primitive == null)
        return null;
      
      if(isPrimitive(token))
        return primitive.wrapper;
      
      return primitive.primitive;
    }
    
    /**
     * Returns the instance of this class which matches {@code type}, or {@code null} if none does.
     * 
     * @param type a generic type.
     * @return the instance of this class which matches {@code type}, or {@code null} if none does.
     */
    public static ValueType<?> valueOf(Type type) {
      if(type == null)
        return null;
      
      if(isPrimitive(type))
        return primitiveReverseMap.get(type);
      
      if(isWrapper(type))
        return wrapperReverseMap.get(type);
      
      // is neither
      return null;
    }
    
    /**
     * Returns the instance of this class which matches {@code cls}, or {@code null} if none does.
     * 
     * @param <E> the specific type {@code cls} represents.
     * @param cls a class object.
     * @return the instance of this class which matches {@code cls}, or {@code null} if none does.
     */
    @SuppressWarnings("unchecked")
    public static <E> ValueType<E> valueOf(Class<E> cls) {
      return (ValueType<E>) valueOf((Type) cls);
    }
    
    /**
     * Returns the instance of this class which matches {@code cls}, or {@code null} if none does.
     * 
     * @param <E> the specific type {@code token} represents.
     * @param token a type token.
     * @return the instance of this class which matches {@code cls}, or {@code null} if none does.
     */
    @SuppressWarnings("unchecked")
    public static <E> ValueType<E> valueOf(TypeToken<E> token) {
      if(token == null)
        return null;
      
      return (ValueType<E>) valueOf(token.type);
    }
  }
  
  final Class<? super T> rawType;
  final Type type;

  /**
   * Constructs a new type token. Derives represented class from type
   * parameter.
   *
   * <p>Clients create an empty anonymous subclass. Doing so embeds the type
   * parameter in the anonymous class's type hierarchy so we can reconstitute
   * it at runtime despite erasure.</p>
   *
   * <p>For example:
   * <code>
   * {@literal TypeToken<List<String>> t = new TypeToken<List<String>>}(){}
   * </code>
   * </p>
   */
  @SuppressWarnings("unchecked")
  protected TypeToken() {
    this.type = getSuperclassTypeParameter(getClass());
    this.rawType = (Class<? super T>) getRawType(type);
  }

  /**
   * Unsafe. Constructs a type token manually.
   */
  @SuppressWarnings({"unchecked"})
  private TypeToken(Type type) {
    this.rawType = (Class<? super T>) getRawType(nonNull(type, "type"));
    this.type = type;
  }

  /**
   * Gets type from super class's type parameter.
   */
  static Type getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    if (superclass instanceof Class<?>) {
      throw new MissingTypeParameterException(subclass);
    }
    return ((ParameterizedType) superclass).getActualTypeArguments()[0];
  }

  /**
   * Gets type token from super class's type parameter.
   */
  static TypeToken<?> fromSuperclassTypeParameter(Class<?> subclass) {
    return new SimpleTypeToken<Object>(subclass);
  }

  private static Class<?> getRawType(Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      // I'm not exactly sure why getRawType() returns Type instead of Class.
      // Neal isn't either but suspects some pathological case related
      // to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        return (Class<?>) rawType;
      }
      throw new UnexpectedTypeException(rawType, Class.class);
    } else if (type instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) type;

      // TODO(jleitch): This is not the most efficient way to handle generic
      // arrays, but is there another way to extract the array class in a
      // non-hacky way (i.e. using String value class names- "[L...")?
      Object rawArrayType = Array.newInstance(
          getRawType(genericArrayType.getGenericComponentType()), 0);
      return rawArrayType.getClass();
    } else {
      throw new UnexpectedTypeException(
          type, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * Gets the raw type.
   * @return the raw type.
   */
  public Class<? super T> getRawType() {
    return rawType;
  }

  /**
   * Gets underlying {@code Type} instance.
   * @return the underlying {@code Type} instance.
   */
  public Type getType() {
    return type;
  }

  /**
   * Check if this type is assignable from the given class object.
   * @param cls a class object
   * @return {@code true} if this type is assignable from {@code cls}.
   */
  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }

  /**
   * Check if this type is assignable from the given Type.
   * @param from a {@code Type} object
   * @return {@code true} if this type is assignable from {@code from}.
   */
  public boolean isAssignableFrom(Type from) {
    if (from == null) {
      return false;
    }

    if (type.equals(from)) {
      return true;
    }

    if (type instanceof Class<?>) {
      return rawType.isAssignableFrom(getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type,
          new HashMap<String, Type>());
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom(getRawType(from))
          && isAssignableFrom(from, (GenericArrayType) type);
    } else {
      throw new UnexpectedTypeException(
          type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * Check if this type is assignable from the given type token.
   * @param token a type token
   * @return {@code true} if this type is assignable from {@code token}.
   */
  public boolean isAssignableFrom(TypeToken<?> token) {
    return isAssignableFrom(token.getType());
  }

  /**
   * Private helper function that performs some assignability checks for
   * the provided GenericArrayType.
   */
  private static boolean isAssignableFrom(Type from, GenericArrayType to) {
    Type toGenericComponentType = to.getGenericComponentType();
    if (toGenericComponentType instanceof ParameterizedType) {
      Type t = from;
      if (from instanceof GenericArrayType) {
        t = ((GenericArrayType) from).getGenericComponentType();
      } else if (from instanceof Class<?>) {
        Class<?> classType = (Class<?>) from;
        while (classType.isArray()) {
          classType = classType.getComponentType();
        }
        t = classType;
      }
      return isAssignableFrom(t, (ParameterizedType) toGenericComponentType,
          new HashMap<String, Type>());
    }
    // No generic defined on "to"; therefore, return true and let other
    // checks determine assignability
    return true;
  }

  /**
   * Private recursive helper function to actually do the type-safe checking
   * of assignability.
   */
  private static boolean isAssignableFrom(Type from, ParameterizedType to,
      Map<String, Type> typeVarMap) {

    if (from == null) {
      return false;
    }

    if (to.equals(from)) {
      return true;
    }

    // First figure out the class and any type information.
    Class<?> clazz = getRawType(from);
    ParameterizedType ptype = null;
    if (from instanceof ParameterizedType) {
      ptype = (ParameterizedType) from;
    }

    // Load up parameterized variable info if it was parameterized.
    if (ptype != null) {
      Type[] tArgs = ptype.getActualTypeArguments();
      TypeVariable<?>[] tParams = clazz.getTypeParameters();
      for (int i = 0; i < tArgs.length; i++) {
        Type arg = tArgs[i];
        TypeVariable<?> var = tParams[i];
        while (arg instanceof TypeVariable<?>) {
          TypeVariable<?> v = (TypeVariable<?>) arg;
          arg = typeVarMap.get(v.getName());
        }
        typeVarMap.put(var.getName(), arg);
      }

      // check if they are equivalent under our current mapping.
      if (typeEquals(ptype, to, typeVarMap)) {
        return true;
      }
    }

    for (Type itype : clazz.getGenericInterfaces()) {
      if (isAssignableFrom(itype, to, new HashMap<String, Type>(typeVarMap))) {
        return true;
      }
    }

    // Interfaces didn't work, try the superclass.
    Type sType = clazz.getGenericSuperclass();
    if (isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap))) {
      return true;
    }

    return false;
  }

  /**
   * Checks if two parameterized types are exactly equal, under the variable
   * replacement described in the typeVarMap.
   */
  private static boolean typeEquals(ParameterizedType from,
      ParameterizedType to, Map<String, Type> typeVarMap) {
    if (from.getRawType().equals(to.getRawType())) {
      Type[] fromArgs = from.getActualTypeArguments();
      Type[] toArgs = to.getActualTypeArguments();
      for (int i = 0; i < fromArgs.length; i++) {
        if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Checks if two types are the same or are equivalent under a variable mapping
   * given in the type map that was provided.
   */
  private static boolean matches(Type from, Type to,
      Map<String, Type> typeMap) {
    if (to.equals(from)) return true;

    if (from instanceof TypeVariable<?>) {
      return to.equals(typeMap.get(((TypeVariable<?>)from).getName()));
    }

    return false;
  }

  /**
   * Hashcode for this object.
   * @return hashcode for this object.
   */
  @Override public int hashCode() {
    return type.hashCode();
  }

  /**
   * Method to test equality. 
   * 
   * @return true if this object is logically equal to the specified object, false otherwise.
   */
  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof TypeToken<?>)) {
      return false;
    }
    TypeToken<?> t = (TypeToken<?>) o;
    return type.equals(t.type);
  }

  /**
   * Returns a string representation of this object.
   * @return a string representation of this object.
   */
  @Override public String toString() {
    return type instanceof Class<?>
        ? ((Class<?>) type).getName()
        : type.toString();
  }

  /**
   * Gets type token for the given {@code Type} instance.
   * @param type a {@code Type} instance
   * @return the corresponding type token.
   */
  public static TypeToken<?> get(Type type) {
    return new SimpleTypeToken<Object>(type);
  }

  /**
   * Gets type token for the given {@code Class} instance.
   * @param type a {@code Class} instance
   * @param <T> {@code type}'s type
   * @return the corresponding type token.
   */
  public static <T> TypeToken<T> get(Class<T> type) {
    return new SimpleTypeToken<T>(type);
  }

  /**
   * Private static class to not create more anonymous classes than
   * necessary.
   */
  private static class SimpleTypeToken<T> extends TypeToken<T> {
    public SimpleTypeToken(Type type) {
      super(type);
    }
  }
}