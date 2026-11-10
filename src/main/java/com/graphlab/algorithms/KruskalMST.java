package com.graphlab.algorithms;

import com.graphlab.model.*;
import java.util.*;

/** Kruskal: sort edges and accept iff they join distinct components. O(E log E). */
public final class KruskalMST {
    private KruskalMST() {}

    public static MSTResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        UnionFind<Vertex> sets = new UnionFind<>(graph.vertices());
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort(Edge.BY_WEIGHT);
        List<Edge> selected = new ArrayList<>();
        double total = 0;
        int examined = 0;
        for (Edge edge : sorted) {
            examined++;
            if (sets.union(edge.from(), edge.to())) {
                selected.add(edge);
                total += edge.weight();
            }
        }
        return new MSTResult("Kruskal", selected, total,
                graph.vertexCount(), sets.componentCount(), examined);
    }
}
