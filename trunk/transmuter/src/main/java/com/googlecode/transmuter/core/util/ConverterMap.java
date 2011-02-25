package com.googlecode.transmuter.core.util;

import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.converter.exception.ConverterTypeIncompatibleWithConverterException;
import com.googlecode.transmuter.exception.ConverterCollisionException;

/**
 * A map used for converter registration, validating the prospective mapping
 * before the actual insertion.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterMap extends HashMap<ConverterType, Converter> {
  private static final long serialVersionUID = 1L;

  public ConverterMap() { /* empty block */ }

  /**
   * Validates the converter type and the converter (using
   * {@link #validatePut(ConverterType, Converter) validatePut}) before
   * insertion, throwing an exception if a problem is found.
   * <p>
   * In particular, a converter cannot be overwritten; they must be specifically
   * removed from this map before a new {@code put} operation with the given
   * converter type can be done.
   * 
   * @return {@code null} if there was no previous converter for
   *         {@code converterType}, or {@code converter} if it was already
   *         paired with {@code converterType}.
   * @throws RuntimeException
   *           all exceptions thrown by
   *           {@link #validatePut(ConverterType, Converter)}.
   * @see #validatePut(ConverterType, Converter)
   */
  @Override
  public Converter put(ConverterType converterType, Converter converter) {
    return validatePut(converterType, converter) ? converter : super.put(converterType, converter);
  }

  /**
   * Checks if the converter type and the converter can be stored in this map.
   * <p>
   * The restrictions implemented here are:
   * <ul>
   * <li>neither {@code converterType} nor {@code converter} can be {@code null}
   * .</li>
   * <li>a converter type must be obtainable from {@code converter}.</li>
   * <li>{@code converterType} must be assignable from {@code converter}'s
   * converter type.</li>
   * <li>this map must not have {@code converterType} associated to a converter
   * other than {@code converter}.</li>
   * </ul>
   * 
   * @param converterType
   *          a converter type.
   * @param converter
   *          a converter.
   * @return {@code true} if {@code converterType} is already associated with
   *         {@code converter}, or {@code false} if {@code converterType} is not
   *         associated to a converter here.
   * @throws IllegalArgumentException
   *           if either {@code converterType} or {@code converter} are
   *           {@code null}.
   * @throws ConverterTypeIncompatibleWithConverterException
   *           if {@code converterType} and {@code converter} are not
   *           compatible.
   * @throws ConverterCollisionException
   *           if this map already has a different converter associated to
   *           {@code converterType}.
   * @see #checkForCompatibility(ConverterType, Converter)
   * @see #checkForCollision(ConverterType, Converter)
   */
  protected boolean validatePut(ConverterType converterType, Converter converter) {
    // check if the carpet matches the curtains
    checkForCompatibility(converterType, converter);

    // check for collisions here
    return checkForCollision(converterType, converter);
  }

  /**
   * Checks if the converter type and the converter are mutually compatible.
   * <p>
   * The restrictions are:
   * <ul>
   * <li>neither {@code converterType} nor {@code converter} can be {@code null}
   * .</li>
   * <li>{@code converterType} must be assignable from {@code converter}'s
   * converter type.</li>
   * </ul>
   * 
   * @param converterType
   *          a converter type.
   * @param converter
   *          a converter.
   * @throws IllegalArgumentException
   *           if either {@code converterType} or {@code converter} are
   *           {@code null}.
   * @throws ConverterTypeIncompatibleWithConverterException
   *           if {@code converterType} and {@code converter} are not
   *           compatible.
   * @see ConverterType#isAssignableFrom(ConverterType)
   */
  protected void checkForCompatibility(ConverterType converterType, Converter converter)
      throws ConverterTypeIncompatibleWithConverterException {
    nonNull(converterType, "converterType");
    nonNull(converter, "converter");

    if (!converterType.isAssignableFrom(converter.getType()))
      throw new ConverterTypeIncompatibleWithConverterException(converterType, converter);
  }

  /**
   * Checks if the converter type and the converter can be stored in this map.
   * 
   * @param converterType
   *          a converter type.
   * @param converter
   *          a converter.
   * @return {@code true} if {@code converterType} is already associated with
   *         {@code converter} in this map, or {@code false} if there's no
   *         converter.
   * @throws IllegalArgumentException
   *           if either {@code converterType}, {@code converter} or {@code map}
   *           are {@code null}.
   * @throws ConverterCollisionException
   *           if this map already has a different converter associated to
   *           {@code converterType}.
   * @see #checkMapForCollision(ConverterType, Converter, Map)
   */
  protected boolean checkForCollision(ConverterType converterType, Converter converter)
      throws ConverterCollisionException {
    return checkMapForCollision(converterType, converter, this);
  }

  /**
   * Checks if the converter type and the converter can be stored in the map.
   * <p>
   * The restrictions are:
   * <ul>
   * <li>neither {@code converterType} nor {@code converter} nor {@code map} can
   * be {@code null}.</li>
   * <li>{@code map} must not have {@code converterType} associated to a
   * converter other than {@code converter}.</li>
   * </ul>
   * 
   * @param converterType
   *          a converter type.
   * @param converter
   *          a converter.
   * @param map
   *          a map.
   * @return {@code true} if {@code converterType} is already associated with
   *         {@code converter} in {@code map}, or {@code false} if there's no
   *         converter in {@code map}.
   * @throws IllegalArgumentException
   *           if either {@code converterType}, {@code converter} or {@code map}
   *           are {@code null}.
   * @throws ConverterCollisionException
   *           if this map already has a different converter associated to the
   *           given converter type.
   */
  protected static boolean checkMapForCollision(ConverterType converterType, Converter converter,
      Map<? extends ConverterType, ? extends Converter> map) throws ConverterCollisionException {
    nonNull(converterType, "converterType");
    nonNull(converter, "converter");
    nonNull(map, "map");

    if (!map.containsKey(converterType))
      return false;

    if (areEqual(converter, map.get(converterType)))
      return true;

    // converter collision, throw up
    throw new ConverterCollisionException(converterType, converter, map.get(converterType));
  }

  /**
   * Attempts to add the converters in the given map to this map, doing nothing
   * if the given map is null or empty. All entries are validated before actual
   * insertion.
   * 
   * @see #validatePut(ConverterType, Converter)
   */
  @Override
  public void putAll(Map<? extends ConverterType, ? extends Converter> map) {
    if (map == null || map.isEmpty())
      return;

    for (Map.Entry<? extends ConverterType, ? extends Converter> entry : map.entrySet())
      validatePut(entry.getKey(), entry.getValue());

    for (Map.Entry<? extends ConverterType, ? extends Converter> entry : map.entrySet())
      super.put(entry.getKey(), entry.getValue());
  }

  /**
   * Ensures that a {@code null} key is never contained in this map.
   */
  @Override
  public boolean containsKey(Object key) {
    return key != null && super.containsKey(key);
  }

  /**
   * Ensures that a {@code null} key is never contained in this map, so invoking
   * this operation on {@code null} does nothing and returns {@code null}.
   */
  @Override
  public Converter remove(Object key) {
    if (key == null)
      return null;

    return super.remove(key);
  }
}