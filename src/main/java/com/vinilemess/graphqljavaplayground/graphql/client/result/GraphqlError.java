package com.vinilemess.graphqljavaplayground.graphql.client.result;

import java.util.Collection;

public record GraphqlError(
        String message,
        Collection<Location> locations,
        Collection<String> path,
        Collection<Extension> extensions
) {

    public record Extension(String classification) {
    }

    public record Location(int line, int column) {
    }
}
