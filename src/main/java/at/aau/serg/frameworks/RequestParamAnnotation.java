package at.aau.serg.frameworks;

/**
 * Represents a request parameter annotation adapter for different REST frameworks
 * This interface provides a unified way to access query parameters
 * across different framework implementations (e.g., Spring, Jakarta)
 *
 * <p>Query parameters are the key-value pairs that appear after the {@code ?}
 * in a URL, such as {@code /search?query=spring&limit=10}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Spring implementation
 * @RequestParam("pageSize") int size
 * @RequestParam(value = "sort", required = false) String sortBy
 *
 * // Jakarta/Quarkus implementation
 * @QueryParam("pageSize") int size
 * @QueryParam("sort") String sortBy
 * }</pre>
 */
public interface RequestParamAnnotation {
    /**
     * Returns the name of the query parameter to bind to the method parameter.
     *
     * @return the query parameter name
     */
    String value();

    /**
     * Indicates whether the query parameter is required to be present
     *
     * @return {@code true} if the parameter is required, {@code false} if optional
     */
    boolean required();
}
