package com.graphlab.cli;

import com.graphlab.algorithms.*;
import com.graphlab.model.*;

/** Runnable console demonstration before the JavaFX UI is added in Phase 2. */
public final class GraphLabDemo {
    private GraphLabDemo() {}

    public static void main(String[] args) {
        Graph graph = new Graph();
        for (String id : new String[]{"A", "B", "C", "D", "E"}) graph.addVertex(id);
        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "C", 2);
        graph.addEdge("B", "C", 1);
        graph.addEdge("B", "D", 5);
        graph.addEdge("C", "D", 8);
        graph.addEdge("C", "E", 10);
        graph.addEdge("D", "E", 2);
        display(KruskalMST.compute(graph));
        display(PrimMST.compute(graph));
    }

    private static void display(MSTResult result) {
        System.out.printf("%n%s | %s | total weight: %s | components: %d%n",
                result.algorithm(), result.isSpanningTree() ? "MST" : "minimum spanning forest",
                result.totalWeight(), result.componentCount());
        for (Edge edge : result.selectedEdges()) {
            System.out.printf("  %s -- %s (weight %s)%n", edge.from(), edge.to(), edge.weight());
        }
    }
}
