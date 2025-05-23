package at.aau.serg.frameworks;

import at.aau.serg.parsers.HttpMethod;

/**
 * Represents a request mapping annotation adapter for different REST frameworks.
 * This interface provides a unified way to access HTTP endpoint mapping information
 * across different framework implementations (e.g., Spring, Jakarta)
 *
 * <p>Request annotations define how HTTP requests are mapped to controller methods,
 * including the URL paths, HTTP methods, content types, and other routing information.</p>
 *
 * <p>Different frameworks handle request mapping differently:</p>
 * <ul>
 *   <li><strong>Spring:</strong> Uses annotations like {@code @RequestMapping}, {@code @GetMapping},
 *       {@code @PostMapping}, {@code @PatchMapping}, etc.</li>
 *   <li><strong>Jakarta:</strong> Uses {@code @Path} for URL mapping combined with HTTP method
 *       annotations like {@code @GET}, {@code @POST}, etc., plus {@code @Produces} and
 *       {@code @Consumes} for content type specification</li>
 * </ul>
 */
public interface RequestAnnotation {
    /**
     * Returns the logical name of the request mapping.
     *
     * <p>Not all frameworks support named mappings. If the framework doesn't
     * support naming or no name is specified, this should return an empty string.</p>
     *
     * @return the logical name of hte mapping, or empty string if not applicable
     */
    String name();

    /**
     * Returns the media types that this endpoint can produce (response content types)
     * These correspond to the {@code Content-Type} header of the HTTP response.
     *
     * <p>Common examples include:</p>
     * <ul>
     *   <li>{@code "application/json"} - for JSON responses</li>
     *   <li>{@code "application/xml"} - for XML responses</li>
     *   <li>{@code "text/html"} - for HTML responses</li>
     *   <li>{@code "text/plain"} - for plain text responses</li>
     * </ul>
     *
     * @return array of producible media types, empty array if not specified
     */
    String[] produces();

    /**
     * Returns the media types that this endpoint can consume (request content types).
     * These correspond to the {@code Content-Type} header of the HTTP request.
     *
     * <p>This is particularly important for POST, PUT, PATCH requests where
     * the client sends data in the request body. Common examples include:</p>
     * <ul>
     *   <li>{@code "application/json"} - for JSON request bodies</li>
     *   <li>{@code "application/xml"} - for XML request bodies</li>
     *   <li>{@code "application/x-www-form-urlencoded"} - for form submissions</li>
     *   <li>{@code "multipart/form-data"} - for file uploads</li>
     * </ul>
     *
     * @return array of consumable media types, empty array if not specified
     */
    String[] consumes();

    /**
     * Returns the primary URL path patterns for this request mapping.
     * <p>Path patterns can include:</p>
     * <ul>
     *   <li>Static paths: {@code "/users"}</li>
     *   <li>Path variables: {@code "/users/{id}"}</li>
     *   <li>Wildcards: {@code "/files/**"}</li>
     * </ul>
     *
     * @return array of URL path patterns, empty array if not specified
     * @see #path()
     */
    String[] value();

    /**
     * Returns the URL path patterns for this request mapping.
     * This serves the same purpose as {@link #value()} but uses explicit naming in some frameworks.
     *
     * @return array of URL path patterns, empty array if not specified
     * @see #value()
     */
    String[] path();

    /**
     * Returns the HTTP method that this adapter handles
     *
     * @return array of supported HTTP methods, may be empty if determined by annotation type
     */
    HttpMethod[] method();
}
