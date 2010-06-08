import static com.googlecode.gentyref.GenericTypeReflector.addWildcardParameters;
import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.googlecode.gentyref.TypeToken;

public class GenericTest {
  private static interface Converter<From, To> {
    To convert(From from);
  }
  
  private static class Stringifier<T> implements Converter<T, String> {
    @Override public String convert(T from) {
      return String.valueOf(from);
    }
  }
  
  public static void main(String[] args) throws SecurityException, NoSuchMethodException {
    Method convert = Converter.class.getMethod("convert", Object.class);
    testConverter(convert, Stringifier.class);
    testConverter(convert, addWildcardParameters(Stringifier.class));
    testConverter(convert, new TypeToken<Stringifier<List<Method>>>() {}.getType());
  }

  private static void testConverter(Method convert, Type converterType) {
    System.out.println("converter type: " + converterType);
    
    Type[] exactParameterTypes = getExactParameterTypes(convert, converterType);
    
    System.out.println("\tgentyref parameter types: " + Arrays.toString(exactParameterTypes));
    System.out.println("\tJava's parameter types: " + Arrays.toString(convert.getGenericParameterTypes()));
    System.out.println("\tgentyref return type: " + getExactReturnType(convert, converterType));
    System.out.println("\tJava's return type: " + convert.getGenericReturnType());
  }
}
