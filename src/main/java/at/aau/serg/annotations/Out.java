package at.aau.serg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The existing source code utilizes reference parameters as additional output values.
 * This annotation serves for documenting such (hacky) uses for clarity.
 * <p>
 * E.g., {@code void someMethod(Object in, Object out)} should be changed to {@code @Out Object out}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Out {
}
