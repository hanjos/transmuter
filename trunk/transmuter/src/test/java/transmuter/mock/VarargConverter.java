package transmuter.mock;

import transmuter.Converts;

public final class VarargConverter {
  @Converts
  public String stringifyArray(Object... o) {
    return String.valueOf(o);
  }
}