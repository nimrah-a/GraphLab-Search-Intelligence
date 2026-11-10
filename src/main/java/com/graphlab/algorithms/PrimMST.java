package com.graphlab.algorithms;

import com.graphlab.model.*;
import java.util.*;

/** Prim: grow each component via a min-priority queue. O(E log E) with a binary heap. */
public final class PrimMST {
    private PrimMST() {}

    public static MSTResult compute(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        Set<Vertex> visited = new HashSet<>();
        List<Edge> selected = new ArrayList<>();
        double total = 0;
        int examined = 0;
        int components = 0;
        PriorityQueue<Edge> frontier = new PriorityQueue<>(Edge.BY_WEIGHT);
        List<Vertex> roots = new ArrayList<>(graph.vertices());
        Collections.sort(roots);
        for (Vertex root : roots) {
            if (!visited.add(root)) continue;
            components++;
            frontier.addAll(graph.incidentEdges(root));
            while (!frontier.isEmpty()) {
                Edge edge = frontier.poll();
                examined++;
                boolean fromSeen = visited.contains(edge.from());
                boolean toSeen = visited.contains(edge.to());
                if (fromSeen == toSeen) continue;
                Vertex next = fromSeen ? edge.to() : edge.from();
                selected.add(edge);
                total += edge.weight();
                visited.add(next);
                frontier.addAll(graph.incidentEdges(next));
            }
        }
        return new MSTResult("Prim", selected, total,
                graph.vertexCount(), components, examined);
    }
}
