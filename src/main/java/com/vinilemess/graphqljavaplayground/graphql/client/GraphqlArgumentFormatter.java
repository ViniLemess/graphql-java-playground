package com.vinilemess.graphqljavaplayground.graphql.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

class GraphqlArgumentFormatter {

    private GraphqlArgumentFormatter() {
    }

    public static String formatQueryWithArguments(final String query,
                                            final Map<String, Object> arguments) {
        return arguments.entrySet()
                .stream()
                .reduce(query, (q, entry) -> {
                    final String argumentPlaceholder = "$%s".formatted(entry.getKey());
                    return replaceWithArgument(q, argumentPlaceholder, entry.getValue());
                }, (q1, q2) -> q1);
    }

    private static String replaceWithArgument(final String query,
                                              final String argumentPlaceholder,
                                              final Object argumentValue) {
        final String replacementValue = formatArgumentValue(argumentValue);
        return query.replace(argumentPlaceholder, replacementValue);
    }

    private static String formatArgumentValue(final Object argumentValue) {
        return switch (argumentValue) {
            case null -> "null";
            case String stringArgument -> "\"%s\"".formatted(stringArgument);
            case Number numberArgument -> numberArgument.toString();
            case Iterable<?> iterableArgument -> formatToArrayArgument(iterableArgument);
            case Enum<?> enumArgument -> enumArgument.name();
            default -> argumentValue.toString();
        };
    }

    private static String formatToArrayArgument(Iterable<?> iterableArgument) {
        final Collection<String> argumentCollection = new ArrayList<>();

        for (final Object argument : iterableArgument) {
            argumentCollection.add(formatArgumentValue(argument));
        }

        return "[%s]".formatted(String.join(", ", argumentCollection));
    }
}
