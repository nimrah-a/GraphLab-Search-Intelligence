package com.graphlab.model;

import java.util.*;

/** Mutable graph editor model: simple, undirected, weighted graph. */
public final class Graph {
    private final Map<String, Vertex> vertices = new LinkedHashMap<>();
    private final Map<Vertex, Map<Vertex, Edge>> adjacency = new LinkedHashMap<>();
    private final Set<Edge> edges = new LinkedHashSet<>();

    public Vertex addVertex(String id) {
        Vertex vertex = new Vertex(id);
        if (vertices.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate vertex: " + id);
        }
        vertices.put(id, vertex);
        adjacency.put(vertex, new LinkedHashMap<>());
        return vertex;
    }

    public Edge addEdge(String firstId, String secondId, double weight) {
        Vertex first = requireVertex(firstId);
        Vertex second = requireVertex(secondId);
        Edge edge = new Edge(first, second, weight);
        if (adjacency.get(first).containsKey(second)) {
            throw new IllegalArgumentException("An edge already connects " + firstId + " and " + secondId);
        }
        adjacency.get(first).put(second, edge);
        adjacency.get(second).put(first, edge);
        edges.add(edge);
        return edge;
    }

    public boolean removeEdge(String firstId, String secondId) {
        Vertex first = requireVertex(firstId);
        Vertex second = requireVertex(secondId);
        Edge removed = adjacency.get(first).remove(second);
        if (removed == null) return false;
        adjacency.get(second).remove(first);
        edges.remove(removed);
        return true;
    }

    public boolean removeVertex(String id) {
        Vertex vertex = vertices.remove(id);
        if (vertex == null) return false;
        for (Edge edge : List.copyOf(adjacency.get(vertex).values())) {
            Vertex neighbor = edge.other(vertex);
            adjacency.get(neighbor).remove(vertex);
            edges.remove(edge);
        }
        adjacency.remove(vertex);
        return true;
    }

    public Vertex requireVertex(String id) {
        Vertex vertex = vertices.get(id);
        if (vertex == null) throw new IllegalArgumentException("Unknown vertex: " + id);
        return vertex;
    }

    public List<Vertex> vertices() {
        return List.copyOf(vertices.values());
    }

    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    public List<Edge> incidentEdges(Vertex vertex) {
        if (!adjacency.containsKey(vertex)) {
            throw new IllegalArgumentException("Vertex is not in this graph: " + vertex);
        }
        return List.copyOf(adjacency.get(vertex).values());
    }

    public int vertexCount() { return vertices.size(); }
    public int edgeCount() { return edges.size(); }
}
