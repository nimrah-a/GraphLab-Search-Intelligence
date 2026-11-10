package com.graphlab.algorithms;

import com.graphlab.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MSTAlgorithmsTest {
    private Graph example() {
        Graph graph = new Graph();
        for (String id : new String[]{"A", "B", "C", "D", "E"}) graph.addVertex(id);
        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "C", 2);
        graph.addEdge("B", "C", 1);
        graph.addEdge("B", "D", 5);
        graph.addEdge("C", "D", 8);
        graph.addEdge("C", "E", 10);
        graph.addEdge("D", "E", 2);
        return graph;
    }

    @Test void bothAlgorithmsFindExpectedMST() {
        for (MSTResult result : new MSTResult[]{KruskalMST.compute(example()), PrimMST.compute(example())}) {
            assertTrue(result.isSpanningTree());
            assertEquals(4, result.selectedEdges().size());
            assertEquals(10.0, result.totalWeight(), 1e-9);
            assertEquals(1, result.componentCount());
        }
    }

    @Test void disconnectedGraphYieldsForestIncludingIsolatedVertex() {
        Graph graph = new Graph();
        for (String id : new String[]{"A", "B", "C", "D"}) graph.addVertex(id);
        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 2);
        for (MSTResult result : new MSTResult[]{KruskalMST.compute(graph), PrimMST.compute(graph)}) {
            assertFalse(result.isSpanningTree());
            assertEquals(2, result.componentCount());
            assertEquals(2, result.selectedEdges().size());
            assertEquals(5.0, result.totalWeight(), 1e-9);
        }
    }

    @Test void emptyAndSingletonGraphs() {
        Graph graph = new Graph();
        assertEquals(0, PrimMST.compute(graph).componentCount());
        assertEquals(0, KruskalMST.compute(graph).selectedEdges().size());
        graph.addVertex("Only");
        assertTrue(PrimMST.compute(graph).isSpanningTree());
        assertTrue(KruskalMST.compute(graph).isSpanningTree());
        assertEquals(0, PrimMST.compute(graph).totalWeight());
    }

    @Test void negativeZeroAndTiedWeights() {
        Graph graph = new Graph();
        for (String id : new String[]{"A", "B", "C", "D"}) graph.addVertex(id);
        graph.addEdge("A", "B", -5);
        graph.addEdge("B", "C", 0);
        graph.addEdge("A", "C", 0);
        graph.addEdge("C", "D", 1);
        graph.addEdge("A", "D", 1);
        assertEquals(-4.0, KruskalMST.compute(graph).totalWeight());
        assertEquals(-4.0, PrimMST.compute(graph).totalWeight());
    }

    @Test void bothAlgorithmsAgreeOnDenseGraph() {
        Graph graph = new Graph();
        for (int i = 0; i < 12; i++) graph.addVertex("V" + i);
        for (int i = 0; i < 12; i++) {
            for (int j = i + 1; j < 12; j++) {
                graph.addEdge("V" + i, "V" + j, (i * 17 + j * 13) % 23 - 7);
            }
        }
        assertEquals(KruskalMST.compute(graph).totalWeight(), PrimMST.compute(graph).totalWeight(), 1e-9);
    }
}
