import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConverterTest {
  public static interface Converter<From, To> {
    To convert(From from);
  } 
  
  public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Method convert = Converter.class.getMethod("convert", Object.class);
    Converter<Object, String> converter = new Converter<Object, String>() {
      @Override
      public String convert(Object from) {
        return String.valueOf(from);
      }
    };
    
    System.out.println(convert.invoke(converter, 42));
    System.out.println(converter.getClass().getMethod("convert", Object.class).invoke(converter, new Object()));
    System.out.println(converter.getClass().getDeclaredMethod("convert", Object.class).invoke(converter, new Object()));
    System.out.println(converter.getClass().getMethod("convert", Object.class).equals(converter.getClass().getDeclaredMethod("convert", Object.class)));
  }

}
