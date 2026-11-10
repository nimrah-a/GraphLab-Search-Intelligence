package com.graphlab.forensics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.graphlab.forensics.SyntheticCases.*;

class ForensicComparisonTest {
    @Test void fourSyntheticFingerprintOutcomes() {
        var examples=SyntheticCases.all();
        Finding[] expected={Finding.IDENTIFICATION,Finding.EXCLUSION,Finding.INCONCLUSIVE,Finding.UNSUITABLE};
        for(int i=0;i<expected.length;i++) assertEquals(expected[i],ForensicComparison.fingerprint(examples.get(i).reference(),examples.get(i).recovered()).finding());
    }
    @Test void fourSyntheticDentalOutcomes() {
        var examples=SyntheticCases.all();
        Finding[] expected={Finding.IDENTIFICATION,Finding.EXCLUSION,Finding.INCONCLUSIVE,Finding.UNSUITABLE};
        for(int i=0;i<expected.length;i++) assertEquals(expected[i],ForensicComparison.dental(examples.get(i).dentalReference(),examples.get(i).dentalRecovered()).finding());
    }
}
