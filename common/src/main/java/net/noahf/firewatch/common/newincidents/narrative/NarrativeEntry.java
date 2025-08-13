package net.noahf.firewatch.common.newincidents.narrative;

public class NarrativeEntry {

    private final long time;
    private final String narration;

    NarrativeEntry(String narration) {
        this.time = System.currentTimeMillis();
        this.narration = narration;
    }

    public long time() {
        return this.time;
    }

    public String narration() {
        return this.narration;
    }

}
