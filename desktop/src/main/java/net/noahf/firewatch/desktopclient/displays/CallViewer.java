package net.noahf.firewatch.desktopclient.displays;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.noahf.firewatch.common.IncidentManager;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.utils.ObjectDuplicator;

public class CallViewer extends GUIPage {

    private final Incident incident;

    public CallViewer(Incident incident) {
        this.incident = incident;
    }

    @Override
    public Parent gui(Stage stage) {
        AnchorPane root = new AnchorPane();
        root.setPrefHeight(800.0D);
        root.setPrefWidth(800.0D);

        VBox mainContainer= new VBox();
        mainContainer.setPrefHeight(800.0D);
        mainContainer.setPrefWidth(800.0D);

        Label label = new Label(this.incident.callType().toString());
        label.setFont(new Font(36.0D));
        label.setPadding(new Insets(20.0D, 0.0D, 0.0D, 20.0D));

        GridPane viewerContainer = new GridPane();
        viewerContainer.setPrefWidth(800.0D);
        viewerContainer.setPrefHeight(710.0D);

        viewerContainer.getColumnConstraints().addAll(
                new ColumnConstraints(10.0D, 498.6D, 498.6D),
                new ColumnConstraints(10.0D, 301.0D, 401.4D)
        );

        viewerContainer.getRowConstraints().addAll(
                new ObjectDuplicator<>(
                        new RowConstraints(10.0D, 30.0D, 30.0D, Priority.SOMETIMES, VPos.CENTER, false)
                ).duplicate(2)
        );

        this.insertIntoViewerContainer(viewerContainer);

        return root;
    }

    private void insertIntoViewerContainer(GridPane viewer) {

    }

}
