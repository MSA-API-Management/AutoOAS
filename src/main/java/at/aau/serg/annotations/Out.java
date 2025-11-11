package at.aau.serg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the parameter as a call-by-reference parameter that is expected to change after the method call,
 * effectively making it an additional output value of the method.
 *
 * <p>
 * E.g., {@code void someMethod(Object in, @Out Object out)} where {@code out} depends on the input {@code in}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Out {
}
