package net.noahf.firewatch.desktopclient.displays;

import com.sothawo.mapjfx.Configuration;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import net.noahf.firewatch.common.data.ems.EmsMedical;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.geolocation.exceptions.GeoLocatorException;
import net.noahf.firewatch.common.geolocation.exceptions.NoDataProvidedException;
import net.noahf.firewatch.common.data.*;
import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.State;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.narrative.NarrativeEntry;
import net.noahf.firewatch.common.units.Agency;
import net.noahf.firewatch.common.utils.ObjectDuplicator;
import net.noahf.firewatch.common.utils.TimeHelper;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;
import net.noahf.firewatch.desktopclient.objects.form.FormInput;
import net.noahf.firewatch.desktopclient.utils.SupplierUtils;
import net.noahf.firewatch.desktopclient.objects.PlusSign;
import org.controlsfx.control.CheckComboBox;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
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
        this.viewer.prefWidthProperty().bind(this.width());
        this.viewer.prefHeightProperty().bind(this.height());
        this.viewer.setGridLinesVisible(true);
        VBox.setMargin(this.viewer, new Insets(20.0, 0.0, 0.0, 0.0));

        this.populateLocationData(this.viewer);
        this.populateCallData(this.viewer);
        this.populateNarrative(this.viewer);

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.prefWidthProperty().bind(this.width().multiply(0.5));
        this.viewer.getColumnConstraints().setAll(new ObjectDuplicator<>(columnConstraints).duplicate(2));

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.prefHeightProperty().bind(this.height().multiply(0.5));
        this.viewer.getRowConstraints().setAll(new ObjectDuplicator<>(rowConstraints).duplicate(2));

        return new GridPane[] {this.viewer};
    }

    void populateLocationData(GridPane viewer) {
        HBox root = this.createRoot("locationData", () -> new HBox());

        root.prefHeightProperty().bind(this.height().multiply(1 / 2));
        root.prefWidthProperty().bind(this.width().multiply(1 / 2));
        // ---------------- MAPS ----------------

        StackPane mapContainer = new StackPane();
        mapContainer.prefHeightProperty().bind(this.height().multiply(0.25));
        mapContainer.minWidthProperty().bind(this.width().multiply(0.25));
        mapContainer.maxWidthProperty().bind(this.width().multiply(0.25));
        mapContainer.setAlignment(Pos.CENTER);
        mapContainer.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        IncidentAddress incidentAddress = this.incident.address();
        GeoAddress determineGeoAddress = incidentAddress.geoAddress(Main.firegen.geoLocator(), false);

        try {
            determineGeoAddress = incidentAddress.geoAddress(Main.firegen.geoLocator(), true);
            GeoAddress finalDetermineGeoAddress = determineGeoAddress;
            Platform.runLater(() -> {
                if (finalDetermineGeoAddress == null) {
                    return;
                }

                MapView map = new MapView();
                StackPane.setMargin(map, new Insets(10.0, 10.0, 10.0, 10.0));
                map.setPrefWidth(180.0);
                map.setPrefHeight(180.0);
                map.setMapType(MapType.OSM);
                map.setCenter(new Coordinate(finalDetermineGeoAddress.coords().latitude(), finalDetermineGeoAddress.coords().longitude()));
                map.initialize(Configuration.builder().interactive(true).showZoomControls(true).build());

                mapContainer.getChildren().add(map);
            });
        } catch (NoDataProvidedException ignored) {
            // ignored: possibly a new incident if no data was given to the GeoLocator
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
        addressForm.setGridLinesVisible(true);
        addressForm.setPrefHeight(255.0);
        addressForm.setPrefWidth(400.0);
        addressForm.setAlignment(Pos.CENTER);
        addressForm.setPadding(new Insets(0.0, 10.0, 5.0, 10.0));

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

        FormInput.text(() -> incidentAddress.commonName())
                .title("Common Name")
                .add(0, addressForm)
                .update((field) -> {
                    field.textProperty().addListener((e, old, now) -> {
                        this.incident.address().commonName(now);
                    });
                });

        FormInput.custom(() -> {
                    HBox box = new HBox(5);
                    box.setPrefWidth(150);
                    box.setMaxWidth(150);
                    box.setMinWidth(150);
                    return box;
                })
                .title("Street Address")
                .add(1, addressForm)
                .update((field) -> {
                    field.prefWidthProperty().bind(addressForm.getColumnConstraints().get(1).prefWidthProperty());

                    TextField houseNumbers = FormInput
                            .text(() -> SupplierUtils.tryGet(() -> incidentAddress.houseNumbers()))
                            .title("#")
                            .update((text) -> {
                                text.setFont(new Font(14.0));
                            })
                            .node();

                    // measuring text so we can update the house numbers field to be only as wide as we need it to be
                    Text textMeasurer = new Text(SupplierUtils.tryGet(() -> incidentAddress.houseNumbers()));
                    textMeasurer.setFont(houseNumbers.getFont());
                    houseNumbers.textProperty().addListener((e, old, now) -> {
                        this.incident.address().houseNumbers(now);
                        textMeasurer.setText(now);
                        textMeasurer.applyCss();
                    });
                    ObservableValue<? extends Number> observable =
                            Bindings.createDoubleBinding(() -> {
                                double textWidth = textMeasurer.getLayoutBounds().getWidth();
                                textMeasurer.setText("0".repeat(2));
                                double minWidth = textMeasurer.getLayoutBounds().getWidth() * 4;
                                textMeasurer.setText("0".repeat(7));
                                double maxWidth = textMeasurer.getLayoutBounds().getWidth() + (minWidth * 4);
                                textMeasurer.setText(houseNumbers.getText());
                                System.out.println(textWidth + ", " + minWidth + " -> " + maxWidth);
                                double finalV = Math.min(Math.max(textWidth, minWidth), maxWidth);
                                System.out.println("final: " + finalV);
                                return finalV;
                            }, textMeasurer.layoutBoundsProperty(), houseNumbers.textProperty());
                    houseNumbers.prefWidthProperty().bind(observable);
                    houseNumbers.maxWidthProperty().bind(observable);
                    houseNumbers.minWidthProperty().bind(observable);
                    field.getChildren().add(houseNumbers);
                    // end measuring text code

                    TextField streetName = FormInput
                            .text(() -> SupplierUtils.tryGet(() -> incidentAddress.streetName()))
                            .title("Street")
                            .node();
                    streetName.textProperty().addListener((e, old, now) -> {
                        this.incident.address().streetName(now);
                    });
                    streetName.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(streetName, Priority.ALWAYS);
                    field.getChildren().add(streetName);
                    return field;
                });

        FormInput.text(() -> incidentAddress.city())
                .title("City")
                .add(2, addressForm)
                .update((field) -> {
                    field.textProperty().addListener((e, old, now) -> this.incident.address().city(now));
                });

        FormInput.combo(false)
                .title("State")
                .add(3, addressForm)
                .update((field) -> {
                    field.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
                    field.getItems().addAll(State.asFormattedStrings());
                    field.setValue(SupplierUtils.tryGet(() -> incidentAddress.state().toString()));
                    field.getSelectionModel().selectedItemProperty().addListener((e, old, now) -> {
                        this.incident.address().state(State.valueOf(now.toString().toUpperCase(Locale.ROOT).replace(" ", "_")));
                    });
                });

        FormInput.text(() -> String.valueOf(incidentAddress.zip()))
                .title("Zip")
                .add(4, addressForm)
                .update((field) -> {
                    if (incidentAddress.zip() == 0) {
                        field.setText(null);
                    }
                    field.textProperty().addListener((e, old, now) -> {
                        try {
                            this.incident.address().zip(Integer.parseInt(now));
                        } catch (Exception exception) {
                            field.setText(old);
                        }
                    });
                });

        FormInput.button()
                .title("SEARCH")
                .add((label, button) -> {
                    addressForm.add(button, 1, 5, 1, 1);
                })
                .update((button) -> {
                    button.setOnMouseClicked((e) -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            this.populateLocationData(this.viewer);
                        }
                        e.consume();
                    });
                });

        // ---------------- ADDRESS FIELDS (END) ----------------

        root.getChildren().addAll(mapContainer, addressForm);
        viewer.add(root, 0, 0);
    }
    void populateCallData(GridPane viewer) {
        VBox root = this.createRoot("callData", () -> new VBox());

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

//        Label incidentNumberLabel = this.formText("Incident #");
//        Label incidentNumber = this.formText(String.valueOf(this.incident.identifier().display()));
//        incidentNumber.setCursor(Cursor.HAND);
//        Tooltip tooltip = new Tooltip("Click to copy!");
//        tooltip.setHideDelay(Duration.millis(2.5));
//        tooltip.setShowDelay(Duration.millis(2.5));
//        tooltip.setFont(new Font(12));
//        incidentNumber.setTooltip(tooltip);
//        incidentNumber.setOnMouseClicked(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                StringSelection string = new StringSelection(this.incident.identifier().display());
//                clipboard.setContents(string, string);
//                tooltip.setText("Copied!");
//            }
//            event.consume();
//        });
//        callDataForm.add(incidentNumberLabel, 0, 0);
//        callDataForm.add(incidentNumber, 1, 0);

//        Label incidentTypeLabel = this.formText("Type");
//        ChoiceBox<String> incidentType = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
//        incidentType.getItems().addAll(Main.firegen.incidentStructure().incidentTypes().asFormatted());
//        incidentType.setValue(SupplierUtils.tryGet(() -> this.incident.type().formatted()));
//        incidentType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            this.incident.type(Main.firegen.incidentStructure().incidentTypes().getFromFormatted(newValue));
//            this.setDynamicTitle(generateTitle(this.incident));
//            this.populateCallData(this.viewer);
//        });
//        callDataForm.add(incidentTypeLabel, 0, 1);
//        callDataForm.add(incidentType, 1, 1);

        // ------------------- INCIDENT PRIORITY -------------------

//        Label priorityLabel = this.formText("Priority");
//        if (this.incident.type() == null || !this.incident.type().isEms()) {
//            ChoiceBox<String> priority = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
//            List<String> prioritiesForCall = SupplierUtils.tryGet(() -> this.incident.type().getIncidentPriorities().asCollection().stream().map(IncidentPriority::formatted).toList());
//            if (prioritiesForCall != null) {
//                priority.getItems().addAll(prioritiesForCall);
//            }
//            priority.setValue(SupplierUtils.tryGet(() -> this.incident.priority().formatted()));
//            priority.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                this.incident.priority(Main.firegen.incidentStructure().incidentPriorities().getFromFormatted(newValue));
//                this.setDynamicTitle(generateTitle(this.incident));
//                this.populateCallData(this.viewer);
//            });
//            callDataForm.add(priority, 1, 2);
//        } else {
//            EmsMedical medicalDetail = this.incident.type().ems();
//            HBox medicalDispatch = new HBox(10);
//            GridPane.setHalignment(medicalDispatch, HPos.CENTER);
//            GridPane.setValignment(medicalDispatch, VPos.CENTER);
//            medicalDispatch.setAlignment(Pos.CENTER);
//            medicalDispatch.setMaxWidth(176.0);
//
//            callDataForm.add(medicalDispatch, 1, 2);
//        }
//        callDataForm.add(priorityLabel, 0, 2);

        // ------------------- INCIDENT PRIORITY (END) -------------------

//        Label callerTypeLabel = this.formText("Caller");
//        ChoiceBox<String> callerType = this.choices(new Insets(0.0, 0.0, 0.0, 0.0));
//        callerType.getItems().addAll(Main.firegen.incidentStructure().callerTypes().asFormatted());
//        callerType.setValue(SupplierUtils.tryGet(() -> this.incident.callerType().formatted()));
//        callerType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            this.incident.callerType(Main.firegen.incidentStructure().callerTypes().getFromFormatted(newValue));
//            this.populateCallData(this.viewer);
//        });
//        callDataForm.add(callerTypeLabel, 0, 3);
//        callDataForm.add(callerType, 1, 3);
//
//        Label agencyLabel = this.formText("Agencies");
//        CheckComboBox<Agency> agency = new CheckComboBox<>();
//        agency.setPrefWidth(176.0);
//        agency.setPrefHeight(26.0);
//        agency.setPadding(new Insets(0.0));
//        GridPane.setValignment(agency, VPos.CENTER);
//        GridPane.setHalignment(agency, HPos.CENTER);
//        agency.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(Agency object) {
//                return object.abbreviation();
//            }
//
//            @Override
//            public Agency fromString(String string) {
//                return Main.firegen.agencyManager().findAgencyByAbbreviation(string);
//            }
//        });
//        agency.getItems().addAll(Main.firegen.agencyManager().findAgencies());
////        agency.setValue(this.tryGet(() -> this.incident.callerType().toString()));
////        agency.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
////            this.incident.callerType(CallerType.valueOfFormatted(newValue));
////            this.populateCallData(this.viewer);
////        });
//        callDataForm.add(agencyLabel, 0, 4);
//        callDataForm.add(agency, 1, 4);
//
//        Label radioChannelLabel = this.formText("Radio Ch");
//        CheckComboBox<RadioChannel> radioChannel = new CheckComboBox<>();
//        radioChannel.setPrefHeight(26.0);
//        radioChannel.setPrefWidth(176.0);
//        radioChannel.setPadding(new Insets(0.0));
//        GridPane.setValignment(radioChannel, VPos.CENTER);
//        GridPane.setHalignment(radioChannel, HPos.CENTER);
//        radioChannel.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(RadioChannel object) {
//                return object.formatted();
//            }
//
//            @Override
//            public RadioChannel fromString(String string) {
//                return Main.firegen.incidentStructure().radioChannels().getFromFormatted(string);
//            }
//        });
//        radioChannel.getItems().addAll(Main.firegen.incidentStructure().radioChannels().asCollection());
////        agency.setValue(this.tryGet(() -> this.incident.callerType().toString()));
////        agency.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
////            this.incident.callerType(CallerType.valueOfFormatted(newValue));
////            this.populateCallData(this.viewer);
////        });
//        callDataForm.add(radioChannelLabel, 0, 5);
//        callDataForm.add(radioChannel, 1, 5);
//
//        // ---------------- DATE AND TIME -----------------
//        Label dispatchTimeLabel = this.formText("Time");
//        callDataForm.add(dispatchTimeLabel, 0, 6);
//
//        HBox dateAndTimeContainer = new HBox();
//        dateAndTimeContainer.setAlignment(Pos.CENTER);
//        dateAndTimeContainer.setPrefHeight(100.0);
//        dateAndTimeContainer.setPrefWidth(200.0);
//        GridPane.setHalignment(dateAndTimeContainer, HPos.CENTER);
//        GridPane.setValignment(dateAndTimeContainer, VPos.CENTER);
//
//        Date date = Date.from(this.incident.created());
//
//        DatePicker dispatchDatePicker = new DatePicker(SupplierUtils.tryGet(() -> LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault())));
//        dispatchDatePicker.setPrefHeight(26.0);
//        dispatchDatePicker.setPrefWidth(89.0);
//        dispatchDatePicker.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(LocalDate object) {
//                return new SimpleDateFormat("MM/dd/yy").format(Date.from(object.atTime(12, 0, 0).toInstant(ZoneOffset.UTC)));
//            }
//
//            @Override
//            public LocalDate fromString(String string) {
//                try {
//                    return LocalDate.ofInstant(new SimpleDateFormat("MM/dd/yy").parse(string).toInstant(), ZoneId.systemDefault());
//                } catch (Exception exception) {
//                    return LocalDate.of(TimeHelper.getCurrentYear(), 1, 1);
//                }
//            }
//        });
//        dispatchDatePicker.setPromptText("Date");
//
//        TextField dispatchTimePicker = new TextField();
//        dispatchTimePicker.setPrefHeight(26.0);
//        dispatchTimePicker.setPrefWidth(58.0);
//        dispatchTimePicker.setPromptText("Time");
//        dispatchTimePicker.setText(SupplierUtils.tryGet(() -> new SimpleDateFormat("HH:mm:ss").format(date)));
//        HBox.setMargin(dispatchTimePicker, new Insets(0.0, 2.0, 0.0, 2.0));
//
//        dateAndTimeContainer.getChildren().addAll(dispatchDatePicker, dispatchTimePicker);
//        callDataForm.add(dateAndTimeContainer, 1, 6);
//
//        // ---------------- DATE AND TIME (END) ----------------
//
//        Label unitsLabel = this.formText("Units");
//        Button units = this.button("Edit units");
//        units.setOnMouseClicked((e) -> {
////            UnitStatus[] statuses = { UnitStatus.IN_SERVICE, UnitStatus.RESPONDING, UnitStatus.ON_SCENE };
////            IncidentType type = this.incident.incidentType();
////            if (type == IncidentType.EMS || type == IncidentType.MOTOR_VEHICLE_CRASH) {
////                statuses = new UnitStatus[]{
////                        UnitStatus.IN_SERVICE, UnitStatus.RESPONDING, UnitStatus.ON_SCENE,
////                        UnitStatus.TRANSPORTING_SECONDARY, UnitStatus.ARRIVED_SECONDARY
////                };
////            }
////
////            UnitList unitList = new UnitList("Units (" + generateTitle(this.incident).get() + ")", this, statuses);
////            unitList.show();
//        });
//        callDataForm.add(unitsLabel, 0, 7);
//        callDataForm.add(units, 1, 7);
//
//        Label shareIncidentLabel = this.formText("Share");
//        Button shareIncident = this.button("Share this incident");
//        callDataForm.add(shareIncidentLabel, 0, 8);
//        callDataForm.add(shareIncident, 1, 8);

        // ---------------- CALL DATA FORM (END) ----------------

        root.getChildren().add(callDataForm);
        viewer.add(root, 1, 0);
    }
    void populateNarrative(GridPane viewer) {
        VBox root = (VBox) this.createRoot("narrative", () -> new VBox());

        root.setAlignment(Pos.CENTER);
        root.setPrefHeight(22.0);
        root.setPrefWidth(400.0);

        HBox narrativeHeader = new HBox(10);
        narrativeHeader.setAlignment(Pos.CENTER);

        Label title = new Label("Narrative");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setUnderline(true);
        title.setFont(new Font(19.0));

        StackPane clickAreaPlus = new StackPane();
        clickAreaPlus.setPrefWidth(26.0);
        clickAreaPlus.setPrefHeight(26.0);

        PlusSign plus = new PlusSign(26.0);

        clickAreaPlus.setCursor(Cursor.HAND);
        clickAreaPlus.setOnMouseClicked((e) -> {
            TextInputDialog noteInput = new TextInputDialog();
            noteInput.setTitle("Add Narrative");
            noteInput.setHeaderText("Add narrative information about incident status.");
            noteInput.setContentText(null);

            Optional<String> result = noteInput.showAndWait();
            result.ifPresent(n -> {
                if (n.isBlank()) {
                    return;
                }
                this.incident.narrative().insert(n);
                this.populateNarrative(this.viewer);
            });
        });

        clickAreaPlus.getChildren().add(plus);
        narrativeHeader.getChildren().addAll(title, clickAreaPlus);

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

        root.getChildren().addAll(narrativeHeader, narrativeTable);
        viewer.add(root, 0, 1);
    }

    private <T extends Pane> T createRoot(String methodId, Supplier<T> obj) {
        List<Node> nodes = this.viewer.getChildren().stream()
                .filter(n -> n.getId() != null)
                .filter(n -> n.getId().equalsIgnoreCase(methodId))
                .toList();
        if (!nodes.isEmpty()) {
            for (Node node : nodes) {
                this.viewer.getChildren().remove(node);
            }
        }
        T root = obj.get();
        root.setId(methodId);
        return root;
    }

    public static Supplier<String> generateTitle(Incident incident) {
        IncidentType type = incident.type();
        IncidentPriority priority = incident.priority();
        if (type == null) {
            return () -> "* NEW *";
        }

        return () -> type.formatted();
    }

}
