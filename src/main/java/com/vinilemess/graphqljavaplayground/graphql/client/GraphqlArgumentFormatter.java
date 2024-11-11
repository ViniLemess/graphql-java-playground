package com.vinilemess.graphqljavaplayground.graphql.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Utility class for formatting GraphQL queries with provided arguments.
 * <p>
 * This class provides methods to replace placeholders in a GraphQL query
 * with the actual argument values supplied in a map. It supports various
 * data types, including:
 * <ul>
 *   <li>Strings</li>
 *   <li>Numbers</li>
 *   <li>Enums</li>
 *   <li>Iterables</li>
 * </ul>
 */
class GraphqlArgumentFormatter {
    private GraphqlArgumentFormatter() {
    }

    /**
     * Formats a given GraphQL query by replacing placeholders with the provided argument values.
     *
     * @param query The GraphQL query containing placeholders
     * @param arguments A map of argument names to their respective values
     * @return The formatted GraphQL query with all placeholders replaced by argument values
     */
    public static String formatQueryWithArguments(final String query,
                                                  final Map<String, Object> arguments) {
        return arguments.entrySet()
                .stream()
                .reduce(query, (q, entry) -> {
                    final String argumentPlaceholder = "$%s".formatted(entry.getKey());
                    return replaceWithArgument(q, argumentPlaceholder, entry.getValue());
                }, (q1, q2) -> q1);
    }

    /**
     * Replaces a placeholder in the provided query with the specified argument value.
     *
     * @param query The GraphQL query containing the placeholder
     * @param argumentPlaceholder The placeholder in the query to be replaced
     * @param argumentValue The value to replace the placeholder with
     * @return The query with the placeholder replaced by the argument value
     */
    private static String replaceWithArgument(final String query,
                                              final String argumentPlaceholder,
                                              final Object argumentValue) {
        final String replacementValue = formatArgumentValue(argumentValue);
        return query.replace(argumentPlaceholder, replacementValue);
    }

    /**
     * Formats the given argument value into a string representation suitable for insertion into a GraphQL query.
     *
     * @param argumentValue The argument value to be formatted. Can be of type String, Number, Enum, Iterable, or null.
     * @return The formatted string representation of the argument value.
     */
    private static String formatArgumentValue(final Object argumentValue) {
        return switch (argumentValue) {
            case null -> "null";
            case final String stringArgument -> "\"%s\"".formatted(stringArgument);
            case final Number numberArgument -> numberArgument.toString();
            case final Iterable<?> iterableArgument -> formatToArrayArgument(iterableArgument);
            case final Enum<?> enumArgument -> enumArgument.name();
            default -> argumentValue.toString();
        };
    }

    /**
     * Formats an Iterable of argument values into a string representation suitable for insertion into
     * a GraphQL query array format.
     *
     * @param iterableArgument The iterable collection of argument values to be formatted.
     * @return The formatted string representation of the iterable argument values in array format.
     */
    private static String formatToArrayArgument(final Iterable<?> iterableArgument) {
        final Collection<String> argumentCollection = new ArrayList<>();
        for (final Object argument : iterableArgument) {
            argumentCollection.add(formatArgumentValue(argument));
        }
        return "[%s]".formatted(String.join(", ", argumentCollection));
    }
}
