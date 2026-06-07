package at.aau.serg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the parameter as unused but not removed for call compatibility.
 *
 * <p>
 * E.g., {@code void someMethod(Object par1, @Unused Object par2)} where {@code someMethod} does not use {@code par2}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Unused {
}
