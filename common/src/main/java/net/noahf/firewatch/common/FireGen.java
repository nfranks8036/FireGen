package net.noahf.firewatch.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FireGen {

    private final IncidentManager incidentManager;

    public FireGen() {
        this.incidentManager = new IncidentManager();
    }

    public IncidentManager getCallManager() {
        return this.incidentManager;
    }

    public int getCurrentYear() {
        try {
            return Integer.parseInt(new SimpleDateFormat("YYYY").format(new Date()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to get current year: " + exception, exception);
        }
    }

}
