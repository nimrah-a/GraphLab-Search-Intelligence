package com.graphlab.algorithms;

import com.graphlab.model.Edge;
import java.util.List;
import java.util.Objects;

/** Immutable result. Disconnected input yields a minimum spanning forest. */
public record MSTResult(String algorithm, List<Edge> selectedEdges, double totalWeight,
                        int vertexCount, int componentCount, int examinedEdges) {
    public MSTResult {
        Objects.requireNonNull(algorithm, "algorithm");
        selectedEdges = List.copyOf(selectedEdges);
        if (componentCount < 0 || vertexCount < 0 || componentCount > vertexCount || examinedEdges < 0) {
            throw new IllegalArgumentException("Invalid result counts");
        }
        if (selectedEdges.size() != vertexCount - componentCount) {
            throw new IllegalArgumentException("A spanning forest needs V - components edges");
        }
    }

    /** Empty graph counts as a forest; a nonempty connected graph has an MST. */
    public boolean isSpanningTree() { return vertexCount > 0 && componentCount == 1; }
}
