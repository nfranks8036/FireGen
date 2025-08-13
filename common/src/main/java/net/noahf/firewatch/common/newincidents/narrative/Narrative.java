package net.noahf.firewatch.common.newincidents.narrative;

import java.util.ArrayList;
import java.util.List;

public class Narrative {

    private final List<NarrativeEntry> entries;

    public Narrative() {
        this.entries = new ArrayList<>();
    }

    public List<NarrativeEntry> entries() {
        return this.entries;
    }

    public NarrativeEntry add(String narration) {
        NarrativeEntry entry = new NarrativeEntry(narration);
        this.entries.add(entry);
        return entry;
    }

}
