package com.graphlab.aviation;

import com.graphlab.algorithms.KruskalMST;
import com.graphlab.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ResilienceAnalyzerTest {
    @Test void failureDisconnectsTreeAndChoosesCheapestAlternative() {
        Graph g = AirlineNetwork.sample().candidates();
        List<Edge> tree = KruskalMST.compute(g).selectedEdges();
        Edge failed = tree.stream().filter(e -> e.touches(g.requireVertex("DEN"))).findFirst().orElseThrow();
        var recovery = ResilienceAnalyzer.analyze(g, tree, failed);
        assertEquals(2, recovery.islands().size());
        assertTrue(recovery.recoverable());
        assertFalse(tree.contains(recovery.backupRoute()));
        assertEquals(recovery.disruptedCost() + recovery.backupRoute().weight(), recovery.restoredCost());
    }

    @Test void unrecoverableWithoutUnusedCandidate() {
        Graph g = new Graph();g.addVertex("A");g.addVertex("B");
        Edge edge = g.addEdge("A","B",4);
        var result = ResilienceAnalyzer.analyze(g, List.of(edge), edge);
        assertFalse(result.recoverable());
        assertEquals(2, result.islands().size());
        assertTrue(Double.isNaN(result.restoredCost()));
    }

    @Test void rejectsInactiveFailureAndInvalidRoutes() {
        Graph g = new Graph();g.addVertex("A");g.addVertex("B");g.addVertex("C");
        Edge ab = g.addEdge("A","B",1),bc = g.addEdge("B","C",2);
        assertThrows(IllegalArgumentException.class,()->ResilienceAnalyzer.analyze(g,List.of(ab),bc));
        assertThrows(IllegalArgumentException.class,()->ResilienceAnalyzer.analyze(g,List.of(ab,ab),ab));
    }
}
