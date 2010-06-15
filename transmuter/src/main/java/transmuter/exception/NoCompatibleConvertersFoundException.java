package transmuter.exception;

import transmuter.Pair;

public class NoCompatibleConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Pair pair;

  public NoCompatibleConvertersFoundException(Pair pair) {
    super("no compatible converters found for " + pair);
    
    this.pair = pair;
  }

  public Pair getPair() {
    return pair;
  }
}
