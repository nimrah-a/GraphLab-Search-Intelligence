package com.graphlab.model;

/** A vertex identified by a nonblank, trimmed, case-sensitive ID. */
public record Vertex(String id) implements Comparable<Vertex> {
    public Vertex {
        if (id == null || id.isBlank() || !id.equals(id.trim())) {
            throw new IllegalArgumentException("Vertex ID must be nonblank and cannot have surrounding whitespace");
        }
    }

    @Override
    public int compareTo(Vertex other) {
        return id.compareTo(other.id());
    }

    @Override
    public String toString() {
        return id;
    }
}
