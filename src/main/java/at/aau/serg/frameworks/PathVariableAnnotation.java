package at.aau.serg.frameworks;

/**
 * Represents a path variable annotation adapter for different REST frameworks
 * This interface provides a unified way to access path variable information
 * across different framework implementations (e.g. Spring, Jakarta)
 *
 * <p>Path variables are typically used to extract values from URL path segments
 * in RESTful web services. For example, in the URL {@code / users/{id}}, the
 * {@code id} portion would be a path variable.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Spring implementation
 * @PathVariable("userId") String id
 *
 * // Jakarta implementation
 * @PathParam("userId") String id
 * }</pre>
 */
public interface PathVariableAnnotation {

    /**
     * Returns the name of the path variable
     *
     * @return the path variable name
     */
    String value();

    /**
     * Indicates whether the path variable is required
     *
     * @return {@code true} if the path variable is required, {@code false} otherwise
     */
    boolean required();
}
