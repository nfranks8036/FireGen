package net.noahf.firewatch.desktopclient.displays.callviewer;

import com.sothawo.mapjfx.*;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.geolocation.exceptions.GeoLocatorException;
import net.noahf.firewatch.common.incidents.*;
import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.State;
import net.noahf.firewatch.common.incidents.narrative.NarrativeEntry;
import net.noahf.firewatch.common.utils.ObjectDuplicator;
import net.noahf.firewatch.common.utils.TimeHelper;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

public class CallViewer extends GUIPage {

    final Incident incident;
    GridPane viewer;

    public CallViewer(Incident incident) {
        super(generateTitle(incident).get());
        this.incident = incident;
    }

    @Override
    public Node[] gui(Stage stage) {
        this.viewer = new GridPane();
        this.viewer.setPrefWidth(800.0D);
        this.viewer.setPrefHeight(710.0D);
        VBox.setMargin(this.viewer, new Insets(20.0, 0.0, 0.0, 0.0));

        this.populateLocationData(this.viewer);
        this.populateCallData(this.viewer);
        this.populateNarrative(this.viewer);

        this.viewer.getColumnConstraints().setAll(
                new ColumnConstraints(10.0D, 498.6D, 498.6D, Priority.SOMETIMES, HPos.CENTER, true),
                new ColumnConstraints(10.0D, 301.0D, 401.4D, Priority.SOMETIMES, HPos.CENTER, true)
        );
        this.viewer.getRowConstraints().setAll(
                new ObjectDuplicator<>(
                        new RowConstraints(10.0D, 367.0D, 367.0D, Priority.SOMETIMES, VPos.CENTER, true)
                ).duplicate(2)
        );

        return new GridPane[] {this.viewer};
    }

    void populateLocationData(GridPane viewer) {
        VBox root = new VBox();
        root.setLayoutX(411.0);
        root.setLayoutY(10.0);
        root.setPrefHeight(200.0);
        root.setPrefWidth(100.0);

        // ---------------- MAPS ----------------

        StackPane mapContainer = new StackPane();
        mapContainer.setPrefWidth(180.0);
        mapContainer.setPrefHeight(180.0);

        IncidentAddress incidentAddress = this.incident.address();
        GeoAddress determineGeoAddress = null;

        try {
            determineGeoAddress = incidentAddress.geoAddress(Main.firegen.geoLocator());

            MapView map = new MapView();
            StackPane.setMargin(map, new Insets(10.0, 10.0, 10.0, 10.0));
            map.setPrefWidth(180.0);
            map.setPrefHeight(180.0);
            map.setMapType(MapType.OSM);
            map.setCenter(new Coordinate(determineGeoAddress.coords().latitude(), determineGeoAddress.coords().longitude()));
            map.initialize(Configuration.builder().interactive(true).showZoomControls(true).build());

            mapContainer.getChildren().add(map);
        } catch (GeoLocatorException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid address!\n\nCorrect the address and press 'SEARCH' again.\n\nError: '" + exception.getMessage() + "'", ButtonType.OK);
            alert.showAndWait();
        }
        if (determineGeoAddress != null) {
            incidentAddress.copyFrom(determineGeoAddress);
        }

        // ---------------- MAPS (END) ----------------


        // ---------------- ADDRESS FIELDS ----------------
        GridPane addressForm = new GridPane();
        addressForm.setPrefHeight(255.0);
        addressForm.setPrefWidth(400.0);
        addressForm.setAlignment(Pos.CENTER);
        addressForm.setPadding(new Insets(0.0, 10.0, 5.0, 10.0));

        Label commonNameLabel = this.formText("Common Name");
        TextField commonName = this.textField("Common Name", incidentAddress::commonName);
        commonName.textProperty().addListener((e, old, now) -> {
            this.incident.address().commonName(now);
        });
        addressForm.add(commonNameLabel, 0, 0);
        addressForm.add(commonName, 1, 0);

        Label streetAddressLabel = formText("Street Address");
        HBox streetAddress = new HBox(5);
        streetAddress.setPrefWidth(230);
        streetAddress.setMaxWidth(230);
        streetAddress.setMinWidth(230);

        TextField houseNumbers = new TextField(this.tryGet(incidentAddress::houseNumbers));
        houseNumbers.setFont(new Font(14.0));
        houseNumbers.setPromptText("#");
        houseNumbers.setAlignment(Pos.CENTER);
        Text textMeasurer = new Text(this.tryGet(incidentAddress::houseNumbers));
        textMeasurer.setFont(houseNumbers.getFont());
        houseNumbers.textProperty().addListener((e, old, now) -> {
            this.incident.address().houseNumbers(now);
            textMeasurer.setText(now);
            textMeasurer.applyCss();
        });
        houseNumbers.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            double textWidth = textMeasurer.getLayoutBounds().getWidth();
            textMeasurer.setText("0".repeat(2));
            double minWidth = textMeasurer.getLayoutBounds().getWidth() + 20;
            textMeasurer.setText("0".repeat(7));
            double maxWidth = textMeasurer.getLayoutBounds().getWidth() + 20;
            textMeasurer.setText(houseNumbers.getText());
            return Math.min(Math.max(textWidth + 20, minWidth), maxWidth);
        }, textMeasurer.layoutBoundsProperty(), houseNumbers.textProperty()));
        streetAddress.getChildren().add(houseNumbers);

        TextField streetName = new TextField(this.tryGet(incidentAddress::streetName));
        streetName.setPromptText("Street");
        streetName.setFont(new Font(14.0));
        streetName.textProperty().addListener((e, old, now) -> {
            this.incident.address().streetName(now);
        });
        streetName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(streetName, Priority.ALWAYS);
        streetAddress.getChildren().add(streetName);

        addressForm.add(streetAddressLabel, 0, 1);
        addressForm.add(streetAddress, 1, 1);

        Label cityLabel = formText("City");
        TextField city = this.textField("City", incidentAddress::city);
        city.textProperty().addListener((e, old, now) -> {
            this.incident.address().city(now);
        });
        addressForm.add(cityLabel, 0, 2);
        addressForm.add(city, 1, 2);

        Label stateLabel = this.formText("State");
        ComboBox<String> state = new ComboBox<>();
        state.setPrefWidth(229.0);
        state.setPrefHeight(26.0);
        state.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
        GridPane.setValignment(state, VPos.CENTER);
        GridPane.setHalignment(state, HPos.CENTER);
        state.getItems().addAll(State.asFormattedStrings());
        state.setValue(this.tryGet(() -> incidentAddress.state().toString()));
        state.getSelectionModel().selectedItemProperty().addListener((e, old, now) -> {
            this.incident.address().state(State.valueOf(now.toUpperCase().replace(" ", "_")));
        });
        addressForm.add(stateLabel, 0, 3);
        addressForm.add(state, 1, 3);

        Label zipLabel = this.formText("ZIP Code");
        TextField zip = this.textField("ZIP Code", () -> String.valueOf(incidentAddress.zip()));
        zipLabel.textProperty().addListener((e, old, now) -> {
            try {
                this.incident.address().zip(Integer.parseInt(now));
            } catch (NumberFormatException error) {
                zip.setText(old);
            }
        });
        addressForm.add(zipLabel, 0, 4);
        addressForm.add(zip, 1, 4);

        Button searchButton = this.button("SEARCH");
        searchButton.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                this.populateLocationData(this.viewer);
            }
            e.consume();
        });
        addressForm.add(searchButton, 1, 5, 1, 1);

        addressForm.getColumnConstraints().setAll(
                new ObjectDuplicator<>(
                        new ColumnConstraints(10.0, 249.3, 249.3, Priority.SOMETIMES, HPos.CENTER, true)
                ).duplicate(2)
        );
        addressForm.getRowConstraints().setAll(
                new ObjectDuplicator<>(
                        new RowConstraints(10.0, 50.0, 30.0, Priority.SOMETIMES, VPos.CENTER, true)
                ).duplicate(4)
        );

        // ---------------- ADDRESS FIELDS (END) ----------------

        root.getChildren().addAll(mapContainer, addressForm);
        viewer.add(root, 0, 0);
    }
    void populateCallData(GridPane viewer) {
        VBox root = new VBox();
        root.setPrefHeight(362.0);
        GridPane callDataForm = new GridPane();
        callDataForm.setAlignment(Pos.CENTER);
        callDataForm.setPrefHeight(321.0);
        callDataForm.setPrefWidth(399.0);

        callDataForm.getColumnConstraints().addAll(
                new ColumnConstraints(10.0, 135.0, 191.0, Priority.SOMETIMES, HPos.CENTER, true),
                new ColumnConstraints(10.0, 264.6, 286.0, Priority.SOMETIMES, HPos.CENTER, true)
        );
        callDataForm.getRowConstraints().addAll(
                new ObjectDuplicator<>(
                        new RowConstraints(10.0, 89.0, 89.0, Priority.SOMETIMES, VPos.CENTER, true)
                ).duplicate(7)
        );

        Label incidentNumberLabel = this.formText("Incident #");
        Label incidentNumber = this.formText(String.valueOf(this.incident.getIncidentNumber()));
        incidentNumber.setCursor(Cursor.HAND);
        Tooltip tooltip = new Tooltip("Click to copy!");
        tooltip.setHideDelay(Duration.millis(2.5));
        tooltip.setShowDelay(Duration.millis(2.5));
        tooltip.setFont(new Font(12));
        incidentNumber.setTooltip(tooltip);
        incidentNumber.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection string = new StringSelection(this.incident.getIncidentNumber());
                clipboard.setContents(string, string);
                tooltip.setText("Copied!");
            }
            event.consume();
        });
        callDataForm.add(incidentNumberLabel, 0, 0);
        callDataForm.add(incidentNumber, 1, 0);

        Label incidentTypeLabel = this.formText("Type");
        ChoiceBox<String> incidentType = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
        incidentType.getItems().addAll(IncidentType.asFormattedStrings());
        incidentType.setValue(this.tryGet(() -> this.incident.incidentType().toString()));
        incidentType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.incident.incidentType(IncidentType.valueOfFormatted(newValue));
            this.setDynamicTitle(generateTitle(this.incident));
            this.populateCallData(this.viewer);
        });
        callDataForm.add(incidentTypeLabel, 0, 1);
        callDataForm.add(incidentType, 1, 1);

        Label priorityLabel = this.formText("Priority");
        ChoiceBox<String> priority = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
        priority.getItems().addAll(Arrays.stream(this.incident.incidentType().supportedPriorityResponses()).toList().stream().map(IncidentPriority::toString).toList());
        priority.setValue(this.tryGet(() -> this.incident.incidentPriority().toString()));
        priority.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.incident.incidentPriority(IncidentPriority.valueOfFormatted(newValue));
            this.setDynamicTitle(generateTitle(this.incident));
            this.populateCallData(this.viewer);
        });
        callDataForm.add(priorityLabel, 0, 2);
        callDataForm.add(priority, 1, 2);

        Label callerTypeLabel = this.formText("Caller");
        ChoiceBox<String> callerType = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
        callerType.getItems().addAll(CallerType.asFormattedStrings());
        callerType.setValue(this.tryGet(() -> this.incident.callerType().toString()));
        callerType.getSelectionModel().selectedItemProperty().addListener(IncidentChanges.INCIDENT_CALLER);
        callDataForm.add(callerTypeLabel, 0, 3);
        callDataForm.add(callerType, 1, 3);

        // ---------------- DATE AND TIME -----------------
        Label dispatchTimeLabel = this.formText("Time");
        callDataForm.add(dispatchTimeLabel, 0, 4);

        HBox dateAndTimeContainer = new HBox();
        dateAndTimeContainer.setAlignment(Pos.CENTER);
        dateAndTimeContainer.setPrefHeight(100.0);
        dateAndTimeContainer.setPrefWidth(200.0);
        GridPane.setHalignment(dateAndTimeContainer, HPos.CENTER);
        GridPane.setValignment(dateAndTimeContainer, VPos.CENTER);

        Date date = new Date(this.incident.dispatchTime());

        DatePicker dispatchDatePicker = new DatePicker(this.tryGet(() -> LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault())));
        dispatchDatePicker.setPrefHeight(26.0);
        dispatchDatePicker.setPrefWidth(89.0);
        dispatchDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate object) {
                return new SimpleDateFormat("MM/dd/yy").format(Date.from(object.atTime(12, 0, 0).toInstant(ZoneOffset.UTC)));
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return LocalDate.ofInstant(new SimpleDateFormat("MM/dd/yy").parse(string).toInstant(), ZoneId.systemDefault());
                } catch (Exception exception) {
                    return LocalDate.of(TimeHelper.getCurrentYear(), 1, 1);
                }
            }
        });
        dispatchDatePicker.setPromptText("Date");

        TextField dispatchTimePicker = new TextField();
        dispatchTimePicker.setPrefHeight(26.0);
        dispatchTimePicker.setPrefWidth(58.0);
        dispatchTimePicker.setPromptText("Time");
        dispatchTimePicker.setText(this.tryGet(() -> new SimpleDateFormat("HH:mm:ss").format(date)));
        HBox.setMargin(dispatchTimePicker, new Insets(0.0, 2.0, 0.0, 2.0));

        Button dispatchDateTimeNow = new Button("N");
        dispatchDateTimeNow.setMnemonicParsing(false);
        dispatchDateTimeNow.setPrefHeight(26.0);
        dispatchDateTimeNow.setPrefWidth(27.0);
        dispatchDateTimeNow.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                this.incident.dispatchTime(System.currentTimeMillis());
                this.populateCallData(viewer);
            }
            e.consume();
        });

        dateAndTimeContainer.getChildren().addAll(dispatchDatePicker, dispatchTimePicker, dispatchDateTimeNow);
        callDataForm.add(dateAndTimeContainer, 1, 4);

        // ---------------- DATE AND TIME (END) ----------------

        Label unitsLabel = this.formText("Units");
        Button units = this.button("Edit units");
        callDataForm.add(unitsLabel, 0, 5);
        callDataForm.add(units, 1, 5);

        Label shareIncidentLabel = this.formText("Share");
        Button shareIncident = this.button("Share this incident");
        callDataForm.add(shareIncidentLabel, 0, 6);
        callDataForm.add(shareIncident, 1, 6);

        // ---------------- CALL DATA FORM (END) ----------------

        root.getChildren().add(callDataForm);
        viewer.add(callDataForm, 1, 0);
    }
    private void populateNarrative(GridPane viewer) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefHeight(22.0);
        root.setPrefWidth(400.0);

        Label title = new Label("Narrative");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setUnderline(true);
        title.setFont(new Font(19.0));

        TableView<NarrativeEntry> narrativeTable = new TableView<>();
        narrativeTable.setPrefHeight(275.0);
        narrativeTable.setPrefWidth(400.0);
        narrativeTable.setPlaceholder(new Label("No narrative written about this incident yet"));
        VBox.setMargin(narrativeTable, new Insets(5.0, 10.0, 5.0, 10.0));

        TableColumn<NarrativeEntry, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().time())));
        timeColumn.setSortType(TableColumn.SortType.DESCENDING);
        timeColumn.setPrefWidth(110);
        narrativeTable.getColumns().add(timeColumn);

        TableColumn<NarrativeEntry, String> narrationColumn = new TableColumn<>("Narrative");
        narrationColumn.setCellValueFactory((cell) -> new ReadOnlyStringWrapper(cell.getValue().narration()));
        narrationColumn.setPrefWidth(668);
        narrativeTable.getColumns().add(narrationColumn);

        narrativeTable.getItems().addAll(this.incident.narrative().entries());

        root.getChildren().addAll(title, narrativeTable);
        viewer.add(root, 0, 1, 2, 1);
    }

    private Label formText(String text) {
        Label label = new Label(text);
        label.setFont(new Font(19.0));
        label.setContentDisplay(ContentDisplay.CENTER);
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setValignment(label, VPos.CENTER);
        return label;
    }
    private Button button(String text) {
        Button button = new Button(text);
        button.setMnemonicParsing(false);
        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        return button;
    }
    private <T> ChoiceBox<T> choices(Insets padding) {
        ChoiceBox<T> choices = new ChoiceBox<>();
        choices.setPrefWidth(176.0);
        choices.setPrefHeight(26.0);
        choices.setPadding(padding);
        GridPane.setValignment(choices, VPos.CENTER);
        GridPane.setHalignment(choices, HPos.CENTER);
        return choices;
    }
    private TextField textField(String form, Supplier<String> prefilled) {
        TextField text = new TextField(this.tryGet(prefilled));
        text.setPromptText(form);
        text.setPrefWidth(226.0);
        text.setFont(new Font(14.0));
        GridPane.setMargin(text, new Insets(0.0, 5.0, 0.0, 5.0));
        return text;
    }

    private <T> T tryGet(Supplier<T> tryValue) {
        try {
            return tryValue.get();
        } catch (Exception exception) {
            return null;
        }
    }

    private static Supplier<String> generateTitle(Incident incident) {
        IncidentType type = incident.incidentType();
        IncidentPriority priority = incident.incidentPriority();
        if (type == IncidentType.EMS) {
            return () -> type + ", " + priority.toString().replace("EMS ", "") + (priority.toString().contains("EMS") ? " RESPONSE" : "");
        } else if (type == IncidentType.MOTOR_VEHICLE_CRASH) {
            return () -> "MVC " + priority.toString().replace("MVC ", "");
        }

        return () -> incident.incidentType().toString();
    }

}
