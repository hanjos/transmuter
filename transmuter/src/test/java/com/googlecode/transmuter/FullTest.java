package com.googlecode.transmuter;

import com.googlecode.transmuter.converter.*;
import com.googlecode.transmuter.core.ConverterMapTest;
import com.googlecode.transmuter.core.TransmuterTest;
import com.googlecode.transmuter.type.TypeTokenValueTypeTest;
import com.googlecode.transmuter.util.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
  TypeTokenValueTypeTest.class,
  NotificationTest.class,
  BindingTest.class,
  ConverterTest.class,
  StringUtilsTest.class,
  ObjectUtilsTest.class,
  ReflectionUtilsTest.class,
  CollectionUtilsTest.class,
  EagerProviderTest.class,
  LazyProviderTest.class,
  ConverterTypeTest.class,
  ConverterMapTest.class,
  TransmuterTest.class })
public class FullTest { /* empty block */ }
