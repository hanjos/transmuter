package com.googlecode.transmuter.core;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.exception.NoCompatibleConvertersFoundException;
import com.googlecode.transmuter.exception.TooManyConvertersFoundException;

public interface ConverterSelector {
  Converter getConverterFor(ConverterType type, Iterable<? extends Converter> converters) 
      throws NoCompatibleConvertersFoundException, TooManyConvertersFoundException;
}
