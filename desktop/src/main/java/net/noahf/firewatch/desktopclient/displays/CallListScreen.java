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
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.incidents.IncidentManager;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;
import net.noahf.firewatch.desktopclient.utils.SupplierUtils;

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
            Incident incident = Main.firegen.incidentManager().generate();
            Main.fx.setNewPage(new CallViewer(incident));
        }
        event.consume();
    };

    public CallListScreen() {
        super(() -> "Active Calls (" + (Main.firegen.incidentManager().countActive()) + ")");
    }

    @Override
    public Node[] gui(Stage stage) {
        IncidentManager calls = Main.firegen.incidentManager();

        TableView<Incident> table = new TableView<>();
        table.setRowFactory(this.clickRowEvent);
        table.setPadding(new Insets(20.0D, 20.0D, 0.0D, 20.0D));
        table.setPlaceholder(new Label("No active calls"));
        table.prefWidthProperty().bind(this.width());
        table.prefHeightProperty().bind(this.height().subtract(70D * 2));
        this.addColumns(table);
        for (Incident activeIncident : calls.findActive()) {
            table.getItems().add(activeIncident);
        }

        HBox createCallContainer = new HBox();
        createCallContainer.setAlignment(Pos.CENTER);
        createCallContainer.setPrefHeight(70D);

        Button createCall = new Button("CREATE NEW CALL");
        createCall.setGraphicTextGap(0.0D);
        createCall.setMnemonicParsing(false);
        createCall.setPrefHeight(31.0);
        createCall.prefWidthProperty().bind(table.prefWidthProperty().multiply(211D / 800D));
        createCall.setFont(new Font(15.0D));
        createCall.setCursor(Cursor.HAND);
        createCall.setOnMouseClicked(this.clickCreateCallButton);
        createCall.setAlignment(Pos.CENTER);
        createCallContainer.getChildren().add(createCall);

        return new Node[] { table, createCallContainer };
    }

    @SuppressWarnings("unchecked")
    private void addColumns(TableView<Incident> table) {
        TableColumn<Incident, String> first = new TableColumn<>("Incident Number");
        first.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(
                SupplierUtils.tryGet(() -> cell.getValue().identifier().display())
        ));
        first.prefWidthProperty().bind(table.widthProperty().multiply(112D / 800D));

        TableColumn<Incident, String> second = new TableColumn<>("Time");
        second.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(
                SupplierUtils.tryGet(() -> cell.getValue().created().toString())
        ));
        second.prefWidthProperty().bind(table.widthProperty().multiply(75D / 800D));

        TableColumn<Incident, String> third = new TableColumn<>("Call Type");
        third.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(
                SupplierUtils.tryGet(() -> cell.getValue().type().formatted(), "* NEW *")
        ));
        third.prefWidthProperty().bind(table.widthProperty().multiply(92D / 800D));

        TableColumn<Incident, String> fourth = new TableColumn<>("Priority");
        fourth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(
                SupplierUtils.tryGet(() -> cell.getValue().priority().formatted())
        ));
        fourth.prefWidthProperty().bind(table.widthProperty().multiply(140D / 800D));

        TableColumn<Incident, String> fifth = new TableColumn<>("Address");
        fifth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(
                SupplierUtils.tryGet(() -> cell.getValue().address().toString())
        ));
        fifth.prefWidthProperty().bind(table.widthProperty().multiply(341D / 800));

        table.getColumns().addAll(first, second, third, fourth, fifth);
    }
}
