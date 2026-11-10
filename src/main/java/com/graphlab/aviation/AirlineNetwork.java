package com.graphlab.aviation;

import com.graphlab.model.*;
import java.util.*;

/** Fixed candidate graph with geographic display positions and fictional route installation costs. */
public final class AirlineNetwork {
    public record Airport(String code, String city, double x, double y) {}
    private final List<Airport> airports;
    private final Graph candidates;

    private AirlineNetwork(List<Airport> airports, Graph candidates) {
        this.airports = List.copyOf(airports);
        this.candidates = candidates;
    }

    public List<Airport> airports() { return airports; }
    public Graph candidates() { return candidates; }

    public static AirlineNetwork sample() {
        List<Airport> airports = List.of(
                new Airport("ORD", "Chicago", .49, .29),
                new Airport("JFK", "New York", .84, .35),
                new Airport("DFW", "Dallas", .45, .74),
                new Airport("DEN", "Denver", .25, .49),
                new Airport("LAX", "Los Angeles", .12, .79),
                new Airport("ATL", "Atlanta", .69, .74),
                new Airport("SEA", "Seattle", .16, .18));
        Graph graph = new Graph();
        for (Airport airport : airports) graph.addVertex(airport.code());
        graph.addEdge("ORD", "JFK", 7);
        graph.addEdge("ORD", "DFW", 3);
        graph.addEdge("ORD", "DEN", 4);
        graph.addEdge("ORD", "SEA", 6);
        graph.addEdge("JFK", "ATL", 5);
        graph.addEdge("JFK", "DFW", 8);
        graph.addEdge("DFW", "DEN", 2);
        graph.addEdge("DFW", "ATL", 6);
        graph.addEdge("DFW", "LAX", 8);
        graph.addEdge("DEN", "LAX", 5);
        graph.addEdge("DEN", "SEA", 7);
        graph.addEdge("LAX", "SEA", 9);
        graph.addEdge("ATL", "LAX", 10);
        return new AirlineNetwork(airports, graph);
    }
}
