package net.noahf.firewatch.desktopclient.displays.callviewer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import net.noahf.firewatch.common.incidents.IncidentType;
import net.noahf.firewatch.desktopclient.Main;

import javax.swing.event.ChangeEvent;
import java.beans.EventHandler;

public class IncidentChanges {

    static ChangeListener<String> CALL_TYPE = (observable, oldValue, newValue) -> {
        if (Main.fx.getCurrentPage() instanceof CallViewer callViewer) {
            callViewer.incident.incidentType(IncidentType.valueOfFormatted(newValue));
            callViewer.show();
        }
    };

}
