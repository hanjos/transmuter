package transmuter.mock;

import transmuter.Converts;

public class StringConverter {
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