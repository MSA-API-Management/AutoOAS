package at.aau.serg.frameworks;

/**
 * Represents a request header annotation adapter for different REST frameworks.
 * This interface provides a unified way to access HTTP header information
 * across different framework implementations (e.g., Spring, Jakarta)
 *
 * <p>Request headers contain metadata about the HTTP request, such as
 * content type, authorization tokens, custom application headers, etc.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Spring implementation
 * @RequestHeader("Authorization") String authToken
 *
 * // Jakarta implementation
 * @HeaderParam("Authorization") String authToken
 * }</pre>
 */
public interface RequestHeaderAnnotation {
    /**
     * Returns the name of the HTTP header to bind the method parameter.
     *
     * @return the header name
     */
    String value();

    /**
     * Indicates whether the header is required to be present in the request.
     *
     * @return {@code true} if the header is required, {@code false} if optional
     */
    boolean required();
}
