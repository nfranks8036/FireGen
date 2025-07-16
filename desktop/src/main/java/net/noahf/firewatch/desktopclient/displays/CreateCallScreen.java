package net.noahf.firewatch.desktopclient.displays;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.incidents.IncidentType;
import net.noahf.firewatch.desktopclient.GUIPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateCallScreen extends GUIPage {

    @Override
    public Parent gui(Stage stage) {
        AnchorPane root = new AnchorPane();
        root.setPrefWidth(800);
        root.setPrefHeight(800);

        VBox container = new VBox();
        container.setPrefHeight(800);
        container.setPrefWidth(800);
        VBox.setMargin(container, new Insets(40.0D, 0.0D, 0.0D, 0.0D));
        this.insertIntoContainer(container);

        root.getChildren().add(container);
        return root;
    }

    private void insertIntoContainer(VBox container) {
        Label label = new Label("Create New Call");
        label.setFont(new Font(36.0D));
        label.setPadding(new Insets(20.0D, 0.0D, 0.0D, 20.0D));

        HBox centeredContainer = new HBox();
        centeredContainer.setAlignment(Pos.CENTER);
        centeredContainer.setPrefHeight(302.0D);
        centeredContainer.setPrefWidth(800.0D);

        GridPane grid = new GridPane();
        grid.setPrefHeight(344.0);
        grid.setPrefWidth(350.0);
        grid.setAlignment(Pos.CENTER);

        grid.getColumnConstraints().addAll(this.getColumnConstraints());
        grid.getRowConstraints().addAll(this.getRowConstraints());

        this.insertIntoGrid(grid);

        HBox createFinalizeContainer = new HBox();
        createFinalizeContainer.setAlignment(Pos.CENTER);
        createFinalizeContainer.setPrefHeight(100.0D);
        createFinalizeContainer.setPrefWidth(200.0D);

        Button createFinalize = new Button("CREATE");
        createFinalize.setMnemonicParsing(false);
        createFinalize.setFont(new Font(18.0D));
        createFinalizeContainer.getChildren().addAll(createFinalize);

        container.getChildren().addAll(label, centeredContainer, grid, createFinalizeContainer);
    }

    private void insertIntoGrid(GridPane grid) {
        Label callTypeLabel = createFieldLabel("Call Type");
        grid.add(callTypeLabel, 0, 0);

        ChoiceBox<String> callType = new ChoiceBox<>();
        callType.setPrefHeight(25.0D);
        callType.setPrefWidth(175.0D);
        callType.getItems().addAll(Arrays.stream(IncidentType.values()).map(IncidentType::toString).toArray(String[]::new));
        grid.add(callType, 1, 0);

        Label streetAddressLabel = createFieldLabel("Street Address");
        grid.add(streetAddressLabel, 0, 1);

        TextField streetAddress = new TextField();
        grid.add(streetAddress, 1, 1);

        Label zipCodeLabel = createFieldLabel("ZIP Code");
        grid.add(zipCodeLabel, 0, 2);

        TextField zipCode = new TextField();
        grid.add(zipCode, 1, 2);
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(ContentDisplay.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(new Font(17.0D));
        return label;
    }

    private List<ColumnConstraints> getColumnConstraints() {
        return new ArrayList<>(List.of(
                new ColumnConstraints(10.0D, 308.0D, 435.0, Priority.SOMETIMES, HPos.CENTER, false),
                new ColumnConstraints(10.0D, 292.0D, 413.0D)
        ));
    }

    private List<RowConstraints> getRowConstraints() {
        return new ArrayList<>(List.of(
                new RowConstraints(10.0D, 100.0D, 100.0D),
                new RowConstraints(1.0D, 100.0D, 100.0D),
                new RowConstraints(0.0D, 100.0D, 100.0D)
        ));
    }

}
