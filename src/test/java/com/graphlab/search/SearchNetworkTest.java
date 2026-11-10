package com.graphlab.search;

import com.graphlab.algorithms.*;
import com.graphlab.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SearchNetworkTest {
    @Test void shortestPathAndBlockedRoute() {
        Graph g=SearchNetwork.build();
        assertEquals(9.0,SearchNetwork.dijkstra(g,"CAMP","CLINIC",Set.of()).total());
        Edge cafeClinic=g.edges().stream().filter(e->e.from().id().equals("CAFE")&&e.to().id().equals("CLINIC")).findFirst().orElseThrow();
        assertTrue(SearchNetwork.dijkstra(g,"CAMP","CLINIC",Set.of(cafeClinic)).total()>9);
    }
    @Test void mstAlgorithmsAgree() {
        Graph g=SearchNetwork.build();
        assertEquals(20.0,KruskalMST.compute(g).totalWeight());
        assertEquals(KruskalMST.compute(g).totalWeight(),PrimMST.compute(g).totalWeight());
    }
    @Test void traversalCoversAllSites() {assertEquals(6,SearchNetwork.bfs(SearchNetwork.build(),"CAMP",Set.of()).size());}
}
