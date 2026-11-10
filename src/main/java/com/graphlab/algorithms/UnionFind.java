package com.graphlab.algorithms;

import java.util.*;

/** Disjoint-set union with path compression and union by rank. */
public final class UnionFind<T> {
    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> rank = new HashMap<>();
    private int components;

    public UnionFind(Collection<T> elements) {
        Objects.requireNonNull(elements, "elements");
        for (T element : elements) {
            Objects.requireNonNull(element, "element");
            if (parent.putIfAbsent(element, element) != null) {
                throw new IllegalArgumentException("Duplicate element: " + element);
            }
            rank.put(element, 0);
            components++;
        }
    }

    public T find(T element) {
        if (!parent.containsKey(element)) throw new IllegalArgumentException("Unknown element: " + element);
        T root = parent.get(element);
        if (!root.equals(element)) {
            root = find(root);
            parent.put(element, root);
        }
        return root;
    }

    /** @return true if two previously separate components were merged. */
    public boolean union(T first, T second) {
        T rootA = find(first);
        T rootB = find(second);
        if (rootA.equals(rootB)) return false;
        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) parent.put(rootA, rootB);
        else if (rankA > rankB) parent.put(rootB, rootA);
        else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        components--;
        return true;
    }

    public boolean connected(T first, T second) { return find(first).equals(find(second)); }
    public int componentCount() { return components; }
}
