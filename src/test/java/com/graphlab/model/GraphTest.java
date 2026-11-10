package com.graphlab.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    @Test void validatesAndNormalizesUndirectedEdges() {
        Graph graph = new Graph();
        graph.addVertex("A"); graph.addVertex("B");
        Edge edge = graph.addEdge("B", "A", -2);
        assertEquals("A", edge.from().id());
        assertEquals("B", edge.to().id());
        assertEquals(1, graph.incidentEdges(graph.requireVertex("A")).size());
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", 8));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "A", 1));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> graph.addVertex("A"));
        assertThrows(IllegalArgumentException.class, () -> graph.addVertex(" "));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "Missing", 1));
    }

    @Test void deletionMaintainsAdjacency() {
        Graph graph = new Graph();
        for (String s : new String[]{"A", "B", "C"}) graph.addVertex(s);
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        assertTrue(graph.removeEdge("A", "B"));
        assertFalse(graph.removeEdge("B", "A"));
        assertEquals(1, graph.edgeCount());
        assertTrue(graph.removeVertex("B"));
        assertEquals(0, graph.edgeCount());
        assertTrue(graph.incidentEdges(graph.requireVertex("C")).isEmpty());
        assertFalse(graph.removeVertex("B"));
    }
}
