package at.aau.serg.frameworks;

/**
 * Represents a multipart parameter annotation adapter for different REST frameworks.
 * This interface provides a unified way to access individual parts of a multipart payload
 * across different framework implementations (e.g., Spring's @RequestPart, Jersey's @FormDataParam).
 *
 * <p>Multipart parameters represent individual segments of a {@code multipart/form-data}
 * HTTP request. These are commonly used for file uploads, complex JSON/XML payloads,
 * or a mix of both within a single request body.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Spring implementation
 * @RequestPart("profilePicture") MultipartFile file
 * * // Jersey implementation
 * @FormDataParam("profilePicture") InputStream file
 * }</pre>
 */
public interface MultipartParameterAnnotation {

    /**
     * Returns the explicitly defined name of the multipart parameter.
     *
     * @return the name of the multipart parameter
     */
    String value();

    /**
     * Indicates whether this specific multipart parameter is required to be present in the request.
     *
     * @return {@code true} if the parameter is required, {@code false} if optional
     */
    boolean required();
}