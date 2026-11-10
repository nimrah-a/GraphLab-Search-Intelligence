package com.graphlab.algorithms;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {
    @Test void mergesAndDetectsCycles() {
        UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D"));
        assertEquals(4, uf.componentCount());
        assertTrue(uf.union("A", "B"));
        assertTrue(uf.union("B", "C"));
        assertFalse(uf.union("A", "C"));
        assertTrue(uf.connected("A", "C"));
        assertFalse(uf.connected("A", "D"));
        assertEquals(2, uf.componentCount());
    }

    @Test void unknownAndDuplicateElementsFail() {
        assertThrows(IllegalArgumentException.class, () -> new UnionFind<>(List.of("A", "A")));
        UnionFind<String> uf = new UnionFind<>(List.of("A"));
        assertThrows(IllegalArgumentException.class, () -> uf.find("B"));
    }
}
