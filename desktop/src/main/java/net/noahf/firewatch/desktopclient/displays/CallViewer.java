package net.noahf.firewatch.desktopclient.displays;

import com.sothawo.mapjfx.Configuration;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;
import net.noahf.firewatch.common.data.ems.EmsField;
import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.geolocation.exceptions.GeoLocatorException;
import net.noahf.firewatch.common.geolocation.exceptions.NoDataProvidedException;
import net.noahf.firewatch.common.data.*;
import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.State;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.incidents.IncidentEms;
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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public class CallViewer extends GUIPage {

    private static <T extends StructureObject> StringConverter<T> createStringConverter(StructureList<T> objs) {
        return new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object.formatted();
            }

            @Override
            public T fromString(String string) {
                return objs.getFromFormatted(string);
            }
        };
    }

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
                                textMeasurer.setText(houseNumbers.getText());
                                double textWidth = textMeasurer.getLayoutBounds().getWidth();
                                textMeasurer.setText("0".repeat(2));
                                double minWidth = textMeasurer.getLayoutBounds().getWidth() + 20;
                                textMeasurer.setText("0".repeat(7));
                                double maxWidth = textMeasurer.getLayoutBounds().getWidth() + 20;
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

        FormInput.combo(String.class, false)
                .title("State")
                .add(3, addressForm)
                .update((field) -> {
                    field.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
                    field.getItems().addAll(State.asFormattedStrings());
                    field.setValue(SupplierUtils.tryGet(() -> incidentAddress.state().toString()));
                    field.getSelectionModel().selectedItemProperty().addListener((e, old, now) -> {
                        this.incident.address().state(State.valueOf(now.toUpperCase(Locale.ROOT).replace(" ", "_")));
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
                        if (now == null || now.isEmpty()) {
                            this.incident.address().zip(0);
                            field.setText(null);
                            return;
                        }
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
                ).duplicate(8)
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

        FormInput.choices(String.class)
                .title("Status")
                .add(1, callDataForm)
                .update((status) -> {
                    status.setPadding(new Insets(0));
                    status.getItems().addAll(Main.firegen.incidentStructure().incidentStatuses().asFormatted());
                    status.setValue(SupplierUtils.tryGet(() -> this.incident.status().formatted()));
                    status.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
                        this.incident.status(Main.firegen.incidentStructure().incidentStatuses().getFromFormatted(now));
                        this.setDynamicTitle(generateTitle(this.incident));
                        this.populateCallData(this.viewer);
                    });
                });

        FormInput.choices(String.class)
                .title("Type")
                .add(2, callDataForm)
                .update((box) -> {
                    box.setPadding(new Insets(0));
                    box.getItems().addAll(Main.firegen.incidentStructure().incidentTypes().asFormatted());
                    box.setValue(SupplierUtils.tryGet(() -> this.incident.type().formatted()));
                    box.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
                        this.incident.type(Main.firegen.incidentStructure().incidentTypes().getFromFormatted(now));
                        this.setDynamicTitle(generateTitle(this.incident));
                        this.populateCallData(this.viewer);
                    });
                });

        // ------------------ INCIDENT PRIORITY ------------------
        FormInput.custom(() -> new HBox())
                .title("Priority")
                .add(3, callDataForm)
                .update((box) -> {
                    if (this.incident.type() == null || !this.incident.type().isEms()) {
                        // ------------------ NON-EMS INCIDENT ------------------
                        FormInput.choices(String.class)
                                .title("Priority")
                                .add((label, choices) -> box.getChildren().add(choices))
                                .update((choices) -> {
                                    List<String> prioritiesCall = SupplierUtils.tryGet(() ->
                                            this.incident.type()
                                                    .getIncidentPriorities()
                                                    .asCollection()
                                                    .stream()
                                                    .map(IncidentPriority::formatted)
                                                    .toList()
                                    );
                                    if (prioritiesCall != null) {
                                        choices.getItems().addAll(prioritiesCall);
                                    }
                                    choices.setValue(SupplierUtils.tryGet(() -> this.incident.priority().formatted()));
                                    choices.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
                                        this.incident.priority(Main.firegen.incidentStructure().incidentPriorities().getFromFormatted(now));
                                        this.setDynamicTitle(generateTitle(this.incident));
                                        this.populateCallData(this.viewer);
                                    });

                                });

                    } else {
                        // ------------------ EMS INCIDENT ------------------
                        IncidentEms medical = this.incident.ems();
                        if (medical == null) {
                            throw new IllegalStateException("EMS not found, but marked true. This state should not be possible.");
                        }

                        for (EmsField field : medical.fields()) {
                            FormInput.combo(String.class, true)
                                    .title(field.name())
                                    .add((label, combo) -> box.getChildren().add(combo))
                                    .update((combo) -> {
                                        combo.getItems().addAll(field.items().asFormatted());
                                    });
                        }
                    }
                });
//        } else {
//            EmsField medicalDetail = this.incident;
//            HBox medicalDispatch = new HBox(10);
//            GridPane.setHalignment(medicalDispatch, HPos.CENTER);
//            GridPane.setValignment(medicalDispatch, VPos.CENTER);
//            medicalDispatch.setAlignment(Pos.CENTER);
//            medicalDispatch.setMaxWidth(176.0);
//
//            callDataForm.add(medicalDispatch, 1, 2);
//        }
//        callDataForm.add(priorityLabel, 0, 2);

//         ------------------- INCIDENT PRIORITY (END) -------------------

        FormInput.choices(String.class)
                .title("Caller")
                .add(4, callDataForm)
                .update((caller) -> {
                    caller.getItems().addAll(Main.firegen.incidentStructure().callerTypes().asFormatted());
                    caller.setValue(SupplierUtils.tryGet(() -> this.incident.callerType().formatted()));
                    caller.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
                        this.incident.callerType(Main.firegen.incidentStructure().callerTypes().getFromFormatted(now));
                        this.populateCallData(this.viewer);
                    });
                });

        FormInput.check(Agency.class)
                .title("Agencies")
                .add(5, callDataForm)
                .update((agency) -> {
                    agency.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(Agency object) {
                            return object.abbreviation();
                        }

                        @Override
                        public Agency fromString(String string) {
                            return Main.firegen.agencyManager().findAgencyByAbbreviation(string);
                        }
                    });
                    agency.getItems().addAll(Main.firegen.agencyManager().findAgencies());
                    this.incident.agencies().forEach(a -> agency.getCheckModel().check(a));
                    agency.getCheckModel().getCheckedItems().addListener((ListChangeListener<Agency>) c -> {
                        // to prevent type casting exceptions
                        ObservableList<? extends Agency> fakeAgencyList = c.getList();
                        ObservableList<Agency> realAgencyList = FXCollections.observableArrayList();
                        realAgencyList.addAll(fakeAgencyList);

                        this.incident.agencies(new HashSet<>(realAgencyList));
                    });
                });

        FormInput.check(RadioChannel.class)
                .title("Radio Tac")
                .add(6, callDataForm)
                .update((radio) -> {
                    radio.setConverter(createStringConverter(Main.firegen.incidentStructure().radioChannels()));
                    radio.getItems().addAll(Main.firegen.incidentStructure().radioChannels().asCollection());
                    this.incident.radioChannels().forEach(rc -> radio.getCheckModel().check(rc));
                    radio.getCheckModel().getCheckedItems().addListener((ListChangeListener<RadioChannel>) c -> {
                        // to prevent type casting exceptions
                        ObservableList<? extends RadioChannel> fakeRadioChannelList = c.getList();
                        ObservableList<RadioChannel> realRadioChannelList = FXCollections.observableArrayList();
                        realRadioChannelList.addAll(fakeRadioChannelList);

                        this.incident.radioChannels(new HashSet<>(realRadioChannelList));
                    });
                });

        FormInput.button()
                .title("Edit Units")
                .add((label, button) -> callDataForm.add(button, 0, 7, 2, 1))
                .update(units -> {
                    units.setOnMouseClicked((event) -> {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            UnitList unitList = new UnitList("Units (" + this.title() + ")", this);
                            unitList.show();
                        }
                        event.consume();
                    });
                });

        FormInput.button()
                .title("Share Incident")
                .add((label, button) -> callDataForm.add(button, 0, 8, 2, 1))
                .update(share -> {
                    // ShareIncident initializer
                });

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
