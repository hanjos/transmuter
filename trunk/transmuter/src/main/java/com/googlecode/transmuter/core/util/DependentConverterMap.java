package com.googlecode.transmuter.core.util;

import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.util.Map;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.exception.ConverterCollisionException;

/**
 * A {@link ConverterMap} which
 * {@linkplain #checkMapForCollision(ConverterType, Converter, Map) checks for
 * collisions} against itself and a master map.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class DependentConverterMap extends ConverterMap {
  private static final long serialVersionUID = 1L;

  private Map<? extends ConverterType, ? extends Converter> masterMap;

  /**
   * Creates a new {@link DependentConverterMap} object, which will be backed by
   * {@code masterMap}.
   * 
   * @param masterMap
   *          the master map which backs this map.
   * @throws IllegalArgumentException
   *           if {@code masterMap} is {@code null}.
   */
  public DependentConverterMap(Map<? extends ConverterType, ? extends Converter> masterMap) {
    this.masterMap = nonNull(masterMap);
  }

  /**
   * Checks for collision in this and in the master map.
   */
  @Override
  protected boolean checkForCollision(ConverterType converterType, Converter converter)
      throws ConverterCollisionException {
    return super.checkForCollision(converterType, converter)
        || ConverterMap.checkMapForCollision(converterType, converter, getMasterMap());
  }

  /**
   * Returns the map which backs this instance.
   * 
   * @return the map which backs this instance.
   */
  public Map<? extends ConverterType, ? extends Converter> getMasterMap() {
    return masterMap;
  }
}