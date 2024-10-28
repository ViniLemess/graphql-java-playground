package com.vinilemess.graphqljavaplayground.graphql.client.result;

import java.util.Collection;

public record GraphQlError(
        String message,
        Collection<Location> locations,
        Collection<String> path,
        Extensions extensions
) {

    public record Extensions(String classification) {
    }

    public record Location(int line, int column) {
    }


    public static GraphQlErrorBuilder builder() {
        return new GraphQlErrorBuilder();
    }

    public static class GraphQlErrorBuilder {
        private String message;
        private Collection<Location> locations;
        private Collection<String> path;
        private Extensions extensions;

        public GraphQlErrorBuilder message(String message) {
            this.message = message;
            return this;
        }

        public GraphQlErrorBuilder locations(Collection<Location> locations) {
            this.locations = locations;
            return this;
        }

        public GraphQlErrorBuilder path(Collection<String> path) {
            this.path = path;
            return this;
        }

        public GraphQlErrorBuilder extensions(Extensions extensions) {
            this.extensions = extensions;
            return this;
        }

        public GraphQlError build() {
            return new GraphQlError(message, locations, path, extensions);
        }
    }
}
