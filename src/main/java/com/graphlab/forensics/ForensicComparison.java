package com.graphlab.forensics;

import java.util.*;
import static com.graphlab.forensics.SyntheticCases.*;

/** Educational, deterministic synthetic feature comparison; NOT validated for forensic use. */
public final class ForensicComparison {
    private ForensicComparison() {}
    public record Result(Finding finding, String explanation, List<String> observations) {
        public Result { observations = List.copyOf(observations); }
    }
    public static Result fingerprint(Fingerprint a, Fingerprint b) {
        if (Math.min(a.quality(),b.quality()) < 30 || b.features().size()<2)
            return new Result(Finding.UNSUITABLE,"The comparison impression lacks usable detail.",List.of("Quality gate not met","No identification decision permitted"));
        int pairs=0;
        var matched = new HashSet<Integer>();
        for (var p:b.features()) {
            for (int i=0;i<a.features().size();i++) {
                var q=a.features().get(i);
                if (!matched.contains(i) && p.type().equals(q.type()) && Math.hypot(p.x()-q.x(),p.y()-q.y())<=4) { pairs++; matched.add(i); break; }
            }
        }
        List<String> notes=List.of("Corresponding synthetic features: "+pairs,"Reference features: "+a.features().size(),"Comparison features: "+b.features().size());
        if (b.quality()<65 || b.features().size()<5)
            return new Result(Finding.INCONCLUSIVE,"Too few reliable synthetic features for a conclusive educational classification.",notes);
        if (pairs>=5) return new Result(Finding.IDENTIFICATION,"The synthetic samples share at least five corresponding typed landmarks within the demo tolerance.",notes);
        if (pairs<=1) return new Result(Finding.EXCLUSION,"The synthetic samples show extensive disagreement between landmark arrangements.",notes);
        return new Result(Finding.INCONCLUSIVE,"Mixed synthetic features do not support a clear simulated result.",notes);
    }
    public static Result dental(Dental a, Dental b) {
        if (b.chart().isEmpty()) return new Result(Finding.UNSUITABLE,"No usable comparison chart is available.",List.of("Recorded comparison teeth: 0"));
        int agreed=0, different=0;
        for(var item:b.chart().entrySet()) {
            if (!a.chart().containsKey(item.getKey())) continue;
            if(a.chart().get(item.getKey()).equals(item.getValue())) agreed++; else different++;
        }
        var notes=List.of("Agreements: "+agreed,"Discrepancies: "+different,"Recorded comparison teeth: "+b.chart().size());
        if (different>=2) return new Result(Finding.EXCLUSION,"Multiple deliberately inconsistent tooth records in this synthetic exercise.",notes);
        if (!b.complete() || agreed<8 || different>0) return new Result(Finding.INCONCLUSIVE,"The chart is incomplete or contains insufficiently resolved discrepancies.",notes);
        return new Result(Finding.IDENTIFICATION,"The complete synthetic dental records agree across all documented teeth.",notes);
    }
}
