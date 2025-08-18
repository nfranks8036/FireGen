package net.noahf.firewatch.desktopclient.displays;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.dynalink.Operation;
import net.noahf.firewatch.common.data.units.UnitAssignmentStatus;
import net.noahf.firewatch.common.data.units.UnitOperationStatus;
import net.noahf.firewatch.common.data.units.UnitStatus;
import net.noahf.firewatch.common.data.units.UnitStatusType;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitAssignment;
import net.noahf.firewatch.common.utils.ObjectDuplicator;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;

import java.util.*;
import java.util.function.Predicate;

public class UnitList extends GUIPage {

    public static final int WIDTH = 1400;
    public static final int HEIGHT = 700;

    public UnitList(String title, CallViewer callViewer) {
        super(
                () -> title,
                () -> {
                    Stage stage = new Stage();
                    stage.initModality(Modality.NONE);
                    stage.setWidth(WIDTH);
                    stage.setHeight(HEIGHT);
                    stage.setResizable(false);
                    return stage;
                },
                false
        );
        this.callViewer = callViewer;
    }

    private final CallViewer callViewer;

    private GridPane viewer;
    private int index;

    private List<TilePane> columns;
    private Map<String, Predicate<Unit>> filters;

    @Override
    public Node[] gui(Stage stage) {
        this.columns = new ArrayList<>();
        this.filters = new HashMap<>();

        this.viewer = new GridPane();
        this.viewer.setAlignment(Pos.CENTER);
        this.viewer.setPrefHeight(HEIGHT - 68.0);
        this.viewer.setPrefWidth(WIDTH);

        this.index = 0;
        for (UnitAssignmentStatus assignment : Main.firegen.incidentStructure().unitAssignmentStatuses()) {
            this.createUnitDisplayForStatus(assignment);
        }

        this.viewer.add(this.createFilterOptions(), 0, 0, this.index, 1);

        this.viewer.getColumnConstraints().setAll(
                new ObjectDuplicator<>(
                        new ColumnConstraints(WIDTH / (double)this.index)
                ).duplicate(this.index)
        );
        // 692
        this.viewer.getRowConstraints().setAll(
                new RowConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, VPos.CENTER, true),
                new RowConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, VPos.CENTER, true),
                new RowConstraints(600.0, 692.0, 692.0, Priority.SOMETIMES, VPos.CENTER, true)
        );

        return new Node[] { this.viewer };
    }

    private HBox createFilterOptions() {
        HBox topBar = new HBox(10);
        topBar.setPrefWidth(WIDTH);
        topBar.setPrefHeight(35.0);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0.0, 23.0, 0.0, 23.0));

        Label searchLabel = new Label("Search Unit: ");
        searchLabel.setFont(new Font(16.0));

        TextField search = new TextField();
        search.setFont(new Font(12.0));
        search.setPromptText("Unit Callsign");
        search.setAlignment(Pos.CENTER);
        search.setPrefWidth(100);
        search.textProperty().addListener((e, old, now) -> {
            if (now == null || now.isBlank()) {
                this.filter("search", null);
                return;
            }
            this.filter("search", (u) -> u.matches(now));
        });

        Label unitTypeLabel = new Label("Unit Type: ");
        unitTypeLabel.setFont(new Font(16.0));

        ComboBox<String> unitType = new ComboBox<>();
        unitType.getItems().add(" ");
        unitType.getItems().addAll(Main.firegen.incidentStructure().unitTypes().asFormatted());
        unitType.setValue(" ");
        unitType.setPrefWidth(150);
        unitType.getSelectionModel().selectedItemProperty().addListener((e, old, now) -> {
            if (now.isBlank()) {
                this.filter("unitType", null);
                return;
            }
            this.filter("unitType", (u) -> u.unitType().name().equalsIgnoreCase(unitType.valueProperty().get().replace(" ", "_").toUpperCase()));
        });

        Button clear = new Button("Clear Filters");
        clear.setFont(new Font(14.0));
        clear.setPrefHeight(20.0);
        clear.visibleProperty().bind(Bindings.or(
                search.textProperty().isNotEmpty(),
                unitType.valueProperty().isNotEqualTo(" ")
        ));
        clear.setOnMouseClicked((e) -> {
            search.setText(null);
            search.deselect();
            unitType.setValue(" ");
            this.filters.clear();
        });

        topBar.getChildren().addAll(searchLabel, search, unitTypeLabel, unitType, clear);

        return topBar;
    }

    private void createUnitDisplayForStatus(UnitStatus status) {
        Label titleLabel = this.createTitleCard(status.formatted());
        this.viewer.add(titleLabel, this.index, 1);

        TilePane tiles = new TilePane();
        tiles.setHgap(10.0);
        tiles.setVgap(10.0);
        tiles.setPrefColumns(4);
        tiles.setAlignment(Pos.CENTER);
        tiles.setTileAlignment(Pos.CENTER);
        tiles.setPrefTileWidth(100);
        tiles.setPrefTileHeight(120);
        tiles.setId(status.name());

        tiles.getChildren().addAll(Main.firegen
                .agencyManager()
                .findUnitsByStatus(status)
                .stream()
                .map(u -> this.createTileUnit(tiles, u))
                .toList()
        );

        this.columns.add(tiles);

        ScrollPane scrollPane = new ScrollPane(tiles);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.setupDropHandling(scrollPane, tiles);

        this.viewer.add(scrollPane, this.index, 2);
        this.index++;
    }

    private void filter(String id, Predicate<Unit> predicate) {
        if (predicate != null) {
            this.filters.put(id, predicate);
        } else {
            this.filters.remove(id);
        }

        for (TilePane column : this.columns) {
            System.out.println("column is: " + column.toString());
            column.getChildren().removeAll(column.getChildren());
            UnitStatus status = Main.firegen.incidentStructure().combineUnitStatuses().getFromName(column.getId());

            List<Predicate<Unit>> filters = new ArrayList<>(this.filters.values());
            List<Unit> units = new ArrayList<>(Main.firegen.agencyManager().findUnitsByStatus(status));
            List<VBox> box = new ArrayList<>();

            System.out.println("Finding units by " + status.formatted());

            if (filters.isEmpty()) {
                filters.add((u) -> true);
            }

            for (Predicate<Unit> filter : filters) {
                box = units.stream()
                        .filter(filter)
                        .map(u -> this.createTileUnit(column, u))
                        .toList();
            }

            column.getChildren().addAll(box);
        }
    }

    private VBox createTileUnit(TilePane tiles, Unit unit) {
        Image image;
        try {
            image = new Image("/icons/" + unit.unitType().name() + ".png");
        } catch (Exception exception) {
            image = new Image("/icons/FALLBACK_ICON.png");
        }
        ImageView icon = new ImageView(image);
        icon.setFitWidth(64);
        icon.setFitHeight(64);

        Label label = new Label(unit.callsign(false, true));
        label.setFont(new Font(15.0));
        Text text = asText(label);
        if (text.getLayoutBounds().getWidth() >= 100) {
            label.setText(unit.callsign(true, true));
        }

        VBox box = new VBox(5, icon, label);
        box.setStyle("-fx-background-color: transparent");
        box.setAlignment(Pos.CENTER);
        box.setId(unit.callsign(true, false));
        box.setOnDragDetected((e) -> {
            Dragboard dragboard = box.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(unit.callsign(true, false));
            dragboard.setContent(content);
            dragboard.setDragView(box.snapshot(null, null));

            box.getProperties().put("source", tiles);
            box.getProperties().put("self", box);
            box.getProperties().put("index", tiles.getChildren().indexOf(box));

            e.consume();
        });

        return box;
    }

    private void setupDropHandling(ScrollPane eventHandler, TilePane target) {
        eventHandler.addEventFilter(DragEvent.DRAG_OVER, e -> {
            if (e.getGestureSource() instanceof VBox sourceNode && e.getDragboard().hasString()) {
                if (sourceNode.getParent() != target) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            }
        });
        eventHandler.addEventFilter(DragEvent.DRAG_DROPPED, e -> {
            Dragboard dragboard = e.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                String unitCallsign = dragboard.getString();
                VBox sourceNode = (VBox) e.getGestureSource();

                TilePane source = (TilePane) sourceNode.getParent();

                if (source != null && source != target) {
                    source.getChildren().remove(sourceNode);
                    target.getChildren().add(sourceNode);

                    Unit unit = Main.firegen.agencyManager().findUnitByCallsign(unitCallsign);
                    UnitStatus newStatus = Main.firegen.incidentStructure().combineUnitStatuses().getFromFormatted(target.getId());

                    var narrative = new Object() {
                        String value = null;
                    };
                    Platform.runLater(() -> {
                        final String prefix = "=" + unitCallsign + " " + newStatus.formatted() + ": ";

                        TextInputDialog noteInput = new TextInputDialog();
                        noteInput.setTitle("Add Narrative");
                        noteInput.setHeaderText("Add additional information about this status update");
                        noteInput.setContentText(prefix);

                        String result = noteInput.showAndWait().orElse(null);

                        narrative.value = prefix + " " + result;
                    });

                    if (newStatus.statusType() == UnitStatusType.ASSIGNMENT) {
                        if (unit.assignment() == null) {
                            unit.assignment(new UnitAssignment(this.callViewer.incident, unit, false));
                        }
                        //noinspection DataFlowIssue (we just set the value one line above this line)
                        unit.assignment().updateStatus((UnitAssignmentStatus) newStatus, narrative.value);
                    } else { // newStatus is of type OPERATION
                        unit.operation((UnitOperationStatus) newStatus);
                    }

                    this.callViewer.populateNarrative(this.callViewer.viewer);
                }

                success = true;
            }

            e.setDropCompleted(success);
        });

    }

    private Label createTitleCard(String text) {
        Label label = new Label(text);
        label.setUnderline(true);
        label.setFont(new Font(24.0));
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setValignment(label, VPos.CENTER);
        return label;
    }

    private Text asText(Label label) {
        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        return text;
    }

}
