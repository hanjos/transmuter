package com.googlecode.transmuter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.googlecode.transmuter.converter.BindingTest;
import com.googlecode.transmuter.converter.ConverterTest;
import com.googlecode.transmuter.converter.ConverterTypeTest;
import com.googlecode.transmuter.type.TypeTokenValueTypeTest;
import com.googlecode.transmuter.util.CollectionUtilsTest;
import com.googlecode.transmuter.util.NotificationTest;
import com.googlecode.transmuter.util.ObjectUtilsTest;
import com.googlecode.transmuter.util.ReflectionUtilsTest;
import com.googlecode.transmuter.util.StringUtilsTest;

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
  ConvertsProviderTest.class,
  ConverterTypeTest.class,
  ConverterMapTest.class,
  TransmuterTest.class })
public class FullTest { /* empty block */ }
