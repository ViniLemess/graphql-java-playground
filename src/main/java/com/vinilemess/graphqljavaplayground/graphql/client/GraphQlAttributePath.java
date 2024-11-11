package com.vinilemess.graphqljavaplayground.graphql.client;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to specify the attribute path in the GraphQL response that corresponds to the annotated class.
 * <p>
 * The attribute path is used by the GraphQlResult `as` method to convert the data in the GraphQL response into
 * an instance of the annotated class.
 * <p>
 * **Note:** The attribute path specified must be at the root level of the `data` field in the GraphQL response.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GraphQlAttributePath("yourAttributePath")
 * public class YourClass {
 *     // Class implementation
 * }
 * }
 * </pre>
 * In this example, the `yourAttributePath` will be used by the GraphQlResult `as` method to find the data
 * in the GraphQL response and convert it to an instance of `YourClass`.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQlAttributePath {
    /**
     * The attribute path in the GraphQL response.
     *
     * @return the attribute path as a String.
     */
    String value();
}