package transmuter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import transmuter.type.TypeTokenValueTypeTest;
import transmuter.util.BindingTest;
import transmuter.util.ObjectUtilsTest;
import transmuter.util.PairTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
  TypeTokenValueTypeTest.class, 
  BindingTest.class,
  ObjectUtilsTest.class, 
  PairTest.class })
public class FullTest { /* empty block */ }
