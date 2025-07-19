package net.noahf.firewatch.desktopclient.displays.callviewer;

import javafx.beans.value.ChangeListener;
import net.noahf.firewatch.common.incidents.CallerType;
import net.noahf.firewatch.common.incidents.IncidentPriority;
import net.noahf.firewatch.common.incidents.IncidentType;
import net.noahf.firewatch.desktopclient.Main;

public class IncidentChanges {

    static ChangeListener<String> INCIDENT_TYPE = (observable, oldValue, newValue) -> {
        if (Main.fx.getCurrentPage() instanceof CallViewer callViewer) {
            callViewer.incident.incidentType(IncidentType.valueOfFormatted(newValue));
            callViewer.show();
        }
    };

    static ChangeListener<String> INCIDENT_PRIORITY = (observable, oldValue, newValue) -> {
        if (Main.fx.getCurrentPage() instanceof CallViewer callViewer) {
            callViewer.incident.incidentPriority(IncidentPriority.valueOfFormatted(newValue));
            callViewer.show();
        }
    };

    static ChangeListener<String> INCIDENT_CALLER = (observable, oldValue, newValue) -> {
        if (Main.fx.getCurrentPage() instanceof CallViewer callViewer) {
            callViewer.incident.callerType(CallerType.valueOfFormatted(newValue));
            callViewer.show();
        }
    };

}
