package net.noahf.firewatch.desktopclient.displays;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.noahf.firewatch.common.IncidentManager;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;

import java.util.Arrays;

public class CallListScreen extends GUIPage {

    private EventHandler<MouseEvent> clickRowEvent = event -> {
        if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {

        }
        event.consume();
    };

    private EventHandler<MouseEvent> clickCreateCallButton = event -> {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            Main.fx.setNewPage(new CreateCallScreen());
        }
        event.consume();
    };

    @Override
    public Parent gui(Stage stage) {
        AnchorPane root = new AnchorPane();
        root.setPrefHeight(Main.fx.window);
        root.setPrefWidth(Main.fx.window);

        VBox container = new VBox();
        container.setLayoutX(-1D);
        container.setPrefHeight(Main.fx.window);
        container.setPrefWidth(Main.fx.window);

        this.insertIntoContainer(container);

        root.getChildren().addAll(container);

        return root;
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
        third.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().callType().toString()));
        third.setPrefWidth(92.0D);

        TableColumn<Incident, String> fourth = new TableColumn<>("Address");
        fourth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().address().toString()));
        fourth.setPrefWidth(360.0D);

        TableColumn<Incident, String> fifth = new TableColumn<>("Units");
        fifth.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(Arrays.toString(cell.getValue().units())));
        fifth.setPrefWidth(123.0D);

        table.getColumns().addAll(first, second, third, fourth, fifth);
    }

    private void insertIntoContainer(VBox container) {
        IncidentManager calls = Main.firegen.getCallManager();

        Label activeCalls = new Label("Active Calls (" + calls.active.size() + ")");
        activeCalls.setFont(new Font(36.0));
        activeCalls.setPadding(new Insets(20.0D, 0.0D, 0.0D, 20.0D));

        TableView<Incident> table = new TableView<>();
        table.setPadding(new Insets(20.0D, 20.0D, 0.0D, 20.0D));
        table.setPrefHeight(616.0D);
        table.setPrefWidth(800.0D);
        this.addColumns(table);
        table.getItems().addAll(calls.active);

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

        container.getChildren().addAll(activeCalls, table, createCallContainer);
    }
}
