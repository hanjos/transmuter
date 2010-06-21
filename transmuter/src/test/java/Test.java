import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.googlecode.transmuter.type.TypeToken;


public class Test {
  private static final Method CONVERT_METHOD;
  
  static {
    Method method;
    try {
      method = IConverter.class.getDeclaredMethod("convert", Object.class);
    } catch (SecurityException e) {
      method = null;
    } catch (NoSuchMethodException e) {
      method = null;
    }
    
    CONVERT_METHOD = method;
  }

  private static interface IConverter<From, To> {
    To convert(From from);
  }
  
  private static class SomethingToString implements IConverter<double[], String> {
    @Override
    public String convert(double[] from) {
      return String.valueOf(from);
    }
  }
  
  private static class GenericToString<T> implements IConverter<T, String> {
    @Override
    public String convert(T from) {
      return String.valueOf(from);
    }
  }
  
  private static class SpecificToString extends GenericToString<List<Map<Enum<?>, String>>> {
    // empty block
  }
  
  public static void main(String[] args) {
    testConverter(SomethingToString.class);
    testConverter(GenericToString.class);
    testConverter(new TypeToken<GenericToString<String[]>>() {}.getType());
    testConverter(SpecificToString.class);
  }

  private static void testConverter(Type converterType) {
    System.out.println("converter type: " + converterType);
    
    Type[] exactParameterTypes = getExactParameterTypes(CONVERT_METHOD, converterType);
    
    System.out.println("\tparameter types: " + Arrays.toString(exactParameterTypes));
    System.out.println("\tgeneric parameter types: " + Arrays.toString(CONVERT_METHOD.getGenericParameterTypes()));
    System.out.println("\treturn type: " + getExactReturnType(CONVERT_METHOD, converterType));
    System.out.println("\tgeneric return type: " + CONVERT_METHOD.getGenericReturnType());
    System.out.println("===");
  }

}
