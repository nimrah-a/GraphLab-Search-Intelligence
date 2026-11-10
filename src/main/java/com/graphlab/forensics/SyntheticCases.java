package com.graphlab.forensics;

import java.util.*;

/** Synthetic, explicitly fictional fixtures; never use for actual biometric identification. */
public final class SyntheticCases {
    private SyntheticCases() {}
    public enum Finding { IDENTIFICATION, EXCLUSION, INCONCLUSIVE, UNSUITABLE }
    public record Point(int x, int y, String type) {}
    public record Fingerprint(String name, List<Point> features, int quality) {
        public Fingerprint { features = List.copyOf(features); }
    }
    public record Dental(String name, Map<Integer, String> chart, boolean complete) {
        public Dental { chart = Map.copyOf(chart); }
    }
    public record CaseRecord(String id, String title, String story, Fingerprint reference,
                             Fingerprint recovered, Dental dentalReference, Dental dentalRecovered) {}
    private static final int[] TEETH = {11,12,13,14,15,16,17,18,21,22,23,24,25,26,27,28};
    private static final List<Point> BASE = List.of(new Point(23,24,"ending"),new Point(35,42,"fork"),
            new Point(58,24,"ending"),new Point(71,49,"fork"),new Point(42,68,"ending"),
            new Point(66,77,"fork"),new Point(22,78,"ending"));
    public static List<CaseRecord> all() {
        var good = new Fingerprint("Reference print", BASE, 95);
        var same = new Fingerprint("Recovered impression", List.of(new Point(24,25,"ending"),new Point(36,42,"fork"),
                new Point(59,25,"ending"),new Point(72,48,"fork"),new Point(43,69,"ending"),
                new Point(65,77,"fork"),new Point(23,77,"ending")), 90);
        var different = new Fingerprint("Recovered impression", List.of(new Point(11,18,"fork"),new Point(80,13,"ending"),
                new Point(15,56,"fork"),new Point(83,66,"ending"),new Point(48,91,"fork"),new Point(16,93,"fork")),90);
        var partial = new Fingerprint("Partial impression", BASE.subList(0,3),48);
        var poor = new Fingerprint("Smudged impression", BASE.subList(0,1),15);
        var chart = new LinkedHashMap<Integer,String>();
        for(int t:TEETH) chart.put(t,"Intact");
        chart.put(16,"Filling"); chart.put(24,"Crown"); chart.put(28,"Missing");
        var match = new LinkedHashMap<>(chart);
        var mismatch = new LinkedHashMap<>(chart); mismatch.put(16,"Intact"); mismatch.put(24,"Missing");
        var insufficient = new LinkedHashMap<>(chart); insufficient.keySet().removeIf(t->t!=11&&t!=12&&t!=13);
        return List.of(
          new CaseRecord("MP-001","Riverside Station","Fictional search exercise · reference and recovered records were generated from the same synthetic profile.",good,same,new Dental("Reference dental chart",chart,true),new Dental("Comparison chart",match,true)),
          new CaseRecord("MP-002","North Park","Fictional search exercise · recovered records deliberately differ from the reference.",good,different,new Dental("Reference dental chart",chart,true),new Dental("Comparison chart",mismatch,true)),
          new CaseRecord("MP-003","Harbor Walk","Fictional search exercise · a partial fingerprint and incomplete dental chart limit interpretation.",good,partial,new Dental("Reference dental chart",chart,true),new Dental("Comparison chart",insufficient,false)),
          new CaseRecord("MP-004","East Terminal","Fictional search exercise · unusable impression and incomplete records.",good,poor,new Dental("Reference dental chart",chart,true),new Dental("Comparison chart",Map.of(),false)));
    }
}
