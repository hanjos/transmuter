package transmuter.mock;

import transmuter.Converts;

public class GenericConverter<From, To> {
  @Converts
  public To convert(From from) {
    return (To) from;
  }
}