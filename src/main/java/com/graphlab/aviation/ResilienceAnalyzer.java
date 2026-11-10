package com.graphlab.aviation;

import com.graphlab.algorithms.UnionFind;
import com.graphlab.model.*;
import java.util.*;

/** Analyze loss of a chosen route in a connected spanning network. */
public final class ResilienceAnalyzer {
    private ResilienceAnalyzer() {}

    public record Recovery(Edge failedRoute, List<Set<Vertex>> islands, Edge backupRoute,
                           double originalCost, double disruptedCost, double restoredCost) {
        public Recovery {
            islands = islands.stream().map(Set::copyOf).toList();
        }
        public boolean recoverable() { return backupRoute != null; }
    }

    /** Candidate edges model potential routes, active edges model existing routes. */
    public static Recovery analyze(Graph candidates, Collection<Edge> activeEdges, Edge failedRoute) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(activeEdges, "activeEdges");
        Objects.requireNonNull(failedRoute, "failedRoute");
        Set<Edge> allCandidates = Set.copyOf(candidates.edges());
        Set<Edge> active = new HashSet<>(activeEdges);
        if (active.size() != activeEdges.size()) throw new IllegalArgumentException("Duplicate active edge");
        if (!allCandidates.containsAll(active)) throw new IllegalArgumentException("Active routes must be candidates");
        if (!active.contains(failedRoute)) throw new IllegalArgumentException("The failed route must be active");
        UnionFind<Vertex> network = new UnionFind<>(candidates.vertices());
        double originalCost = 0;
        double remainingCost = 0;
        for (Edge edge : active) {
            originalCost += edge.weight();
            if (!edge.equals(failedRoute)) {
                network.union(edge.from(), edge.to());
                remainingCost += edge.weight();
            }
        }
        Map<Vertex, Set<Vertex>> groups = new LinkedHashMap<>();
        for (Vertex v : candidates.vertices()) groups.computeIfAbsent(network.find(v), k -> new LinkedHashSet<>()).add(v);
        List<Set<Vertex>> islands = List.copyOf(groups.values());
        Edge cheapest = candidates.edges().stream()
                .filter(e -> !active.contains(e))
                .filter(e -> !network.connected(e.from(), e.to()))
                .min(Edge.BY_WEIGHT).orElse(null);
        return new Recovery(failedRoute, islands, cheapest, originalCost, remainingCost,
                cheapest == null ? Double.NaN : remainingCost + cheapest.weight());
    }
}
