package com.googlecode.transmuter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a prospective converter method. 
 * <p>
 * Not all methods can be converter methods. This annotation should be used only on methods which can be successfully
 * {@linkplain Transmuter#register(Object) registered} in a {@linkplain Transmuter transmuter}. 
 * 
 * @author Humberto S. N. dos Anjos
 * @see Transmuter#register(Object)
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Converts { /* empty block */ }