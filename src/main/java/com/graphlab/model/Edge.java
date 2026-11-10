package com.graphlab.model;

import java.util.Comparator;
import java.util.Objects;

/** An undirected, finite-weight edge. Endpoint order is normalized for equality. */
public record Edge(Vertex from, Vertex to, double weight) {
    public static final Comparator<Edge> BY_WEIGHT = Comparator.comparingDouble(Edge::weight)
            .thenComparing(e -> e.from().id())
            .thenComparing(e -> e.to().id());

    public Edge {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Self-loops are not supported");
        }
        if (!Double.isFinite(weight)) {
            throw new IllegalArgumentException("Weight must be finite");
        }
        if (from.compareTo(to) > 0) {
            Vertex swap = from;
            from = to;
            to = swap;
        }
        // Avoid -0.0 behaving differently from 0.0 in record equality.
        if (weight == 0.0) weight = 0.0;
    }

    public boolean touches(Vertex vertex) {
        return from.equals(vertex) || to.equals(vertex);
    }

    public Vertex other(Vertex vertex) {
        if (from.equals(vertex)) return to;
        if (to.equals(vertex)) return from;
        throw new IllegalArgumentException("Vertex is not incident to this edge: " + vertex);
    }
}
