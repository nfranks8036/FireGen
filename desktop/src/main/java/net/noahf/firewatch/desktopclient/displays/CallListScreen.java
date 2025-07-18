package net.noahf.firewatch.desktopclient.displays;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.noahf.firewatch.common.IncidentManager;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;
import net.noahf.firewatch.desktopclient.displays.callviewer.CallViewer;

public class CallListScreen extends GUIPage {

    private final Callback<TableView<Incident>, TableRow<Incident>> clickRowEvent = callback -> {
        TableRow<Incident> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Incident clicked = row.getItem();
                Main.fx.setNewPage(new CallViewer(clicked));
            }
            event.consume();
        });
        return row;
    };

    private final EventHandler<MouseEvent> clickCreateCallButton = event -> {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            Main.fx.setNewPage(null);
        }
        event.consume();
    };

    public CallListScreen() {
        super("Active Calls (" + (Main.firegen.getCallManager().active.size()) + ")");
    }

    @Override
    public Node[] gui(Stage stage) {
        IncidentManager calls = Main.firegen.getCallManager();

        TableView<Incident> table = new TableView<>();
        table.setRowFactory(this.clickRowEvent);
        table.setPadding(new Insets(20.0D, 20.0D, 0.0D, 20.0D));
        table.setPrefHeight(616.0D);
        table.setPrefWidth(800.0D);
        this.addColumns(table);
        for (Incident activeIncident : calls.active) {
            table.getItems().add(activeIncident);
        }

        HBox createCallContainer = new HBox();
        createCallContainer.setAlignment(Pos.CENTER);
        createCallContainer.setPrefHeight(70.0D);
        createCallContainer.setPrefWidth(800.0D);

        Button createCall = new Button("CREATE NEW CALL");
        createCall.setAlignment(Pos.CENTER);
        createCall.setContentDisplay(ContentDisplay.CENTER);
        createCall.setGraphicTextGap(0.0D);
        createCall.setMnemonicParsing(false);
        createCall.setPrefHeight(31.0);
        createCall.setPrefWidth(211.0);
        createCall.setFont(new Font(15.0D));
        createCall.setCursor(Cursor.HAND);
        createCall.setOnMouseClicked(this.clickCreateCallButton);
        createCallContainer.getChildren().add(createCall);

        return new Node[] { table, createCallContainer };
    }

    @SuppressWarnings("unchecked")
    private void addColumns(TableView<Incident> table) {
        TableColumn<Incident, String> first = new TableColumn<>("Incident Number");
        first.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().getIncidentNumber()));
        first.setPrefWidth(112.0D);

        TableColumn<Incident, String> second = new TableColumn<>("Time");
        second.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().dispatchTime())));
        second.setPrefWidth(75.0D);

        TableColumn<Incident, String> third = new TableColumn<>("Call Type");
        third.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().incidentType().toString()));
        third.setPrefWidth(92.0D);

        TableColumn<Incident, String> fourth = new TableColumn<>("Priority");
        fourth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().incidentPriority().toString()));
        fourth.setPrefWidth(140.0D);

        TableColumn<Incident, String> fifth = new TableColumn<>("Address");
        fifth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().address().toString()));
        fifth.setPrefWidth(341.0D);

        table.getColumns().addAll(first, second, third, fourth, fifth);
    }
}
