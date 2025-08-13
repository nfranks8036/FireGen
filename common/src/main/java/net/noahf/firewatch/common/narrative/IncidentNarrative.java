package net.noahf.firewatch.common.narrative;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class IncidentNarrative {

    private final List<NarrativeEntry> entries;

    public IncidentNarrative() {
        this.entries = new ArrayList<>();
    }

    public void insert(String narrative) {
        this.entries.add(new NarrativeEntry(Instant.now(), narrative));
    }

    public List<NarrativeEntry> entries() { return new ArrayList<>(this.entries); }

}
