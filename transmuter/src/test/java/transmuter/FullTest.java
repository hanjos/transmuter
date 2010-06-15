package transmuter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import transmuter.type.TypeTokenValueTypeTest;
import transmuter.util.ObjectUtilsTest;
import transmuter.util.ReflectionUtilsTest;
import transmuter.util.StringUtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
  TypeTokenValueTypeTest.class, 
  BindingTest.class,
  StringUtilsTest.class,
  ObjectUtilsTest.class,
  ReflectionUtilsTest.class,
  PairTest.class,
  PairBindingMapTest.class,
  TransmuterTest.class })
public class FullTest { /* empty block */ }
