package net.noahf.firewatch.desktopclient.displays;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.incidents.IncidentType;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitStatus;
import net.noahf.firewatch.common.utils.ObjectDuplicator;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.Main;

import java.io.InputStream;
import java.util.Optional;

public class UnitList extends GUIPage {

    public static final int WIDTH = 1400;
    public static final int HEIGHT = 700;

    public UnitList(String title, UnitStatus... statuses) {
        super(
                title,
                () -> {
                    Stage stage = new Stage();
                    stage.initModality(Modality.NONE);
                    stage.setWidth(WIDTH);
                    stage.setHeight(HEIGHT);
                    stage.setResizable(false);
                    return stage;
                }, false
        );
        this.statuses = statuses;
    }

    private final UnitStatus[] statuses;

    private GridPane viewer;
    private int index;

    @Override
    public Node[] gui(Stage stage) {
        this.viewer = new GridPane();
        this.viewer.setAlignment(Pos.CENTER);
        this.viewer.setPrefHeight(HEIGHT - 68.0);
        this.viewer.setPrefWidth(WIDTH);

        this.index = 0;
        TilePane[] panes = new TilePane[this.statuses.length];
        for (UnitStatus status : this.statuses) {
            panes[this.index] = this.createUnitDisplayForStatus(status);
        }

        for (TilePane target : panes) {
            for (TilePane other : panes) {
                if (target != other) {
                    this.setupDropHandling(target, other);
                    this.setupDropHandling(other, target);
                }
            }
        }

        this.viewer.getColumnConstraints().setAll(
                new ObjectDuplicator<>(
                        new ColumnConstraints(WIDTH / (double)this.index)
                ).duplicate(this.index)
        );
        this.viewer.getRowConstraints().setAll(
                new RowConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, VPos.CENTER, true),
                new RowConstraints(692.0, 692.0, 692.0, Priority.SOMETIMES, VPos.CENTER, true)
        );

        return new Node[] { this.viewer };
    }

    private TilePane createUnitDisplayForStatus(UnitStatus status) {
        Label titleLabel = this.createTitleCard(status.toString());
        this.viewer.add(titleLabel, this.index, 0);

        TilePane tiles = new TilePane();
        tiles.setHgap(10.0);
        tiles.setVgap(10.0);
        tiles.setPrefColumns(4);
        tiles.setAlignment(Pos.CENTER);
        tiles.setTileAlignment(Pos.CENTER);
        tiles.setPrefTileWidth(100);
        tiles.setPrefTileHeight(120);
        tiles.setId(status.name());

        tiles.getChildren().addAll(
                Main.firegen.unitManager().getAllUnits(status).stream()
                        .map(unit -> this.createTileUnit(tiles, unit))
                        .toList()
        );

        ScrollPane scrollPane = new ScrollPane(tiles);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.viewer.add(scrollPane, this.index, 1);
        this.index++;

        return tiles;
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

        Label label = new Label(unit.callsign().fullCallsign());
        label.setFont(new Font(15.0));

        VBox box = new VBox(5, icon, label);
        box.setStyle("-fx-background-color: transparent");
        box.setAlignment(Pos.CENTER);
        box.setId(unit.callsign().primaryCallsign());
        box.setOnDragDetected((e) -> {
            Dragboard dragboard = box.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(unit.callsign().primaryCallsign());
            dragboard.setContent(content);
            dragboard.setDragView(box.snapshot(null, null));

            box.getProperties().put("source", tiles);
            box.getProperties().put("self", box);
            box.getProperties().put("index", tiles.getChildren().indexOf(box));

            tiles.getChildren().remove(box);

            e.consume();
        });
        box.setOnDragDone(e -> {
            boolean success = e.isDropCompleted();
            if (!success) {
                TilePane sourcePane = (TilePane) box.getProperties().get("source");
                sourcePane.getChildren().add((int) box.getProperties().get("index"), box);
            }
        });

        return box;
    }

    private void setupDropHandling(TilePane target, TilePane other) {
        target.setOnDragOver(e -> {
            if (e.getGestureSource() != target && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        target.setOnDragDropped(e -> {
            Dragboard dragboard = e.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                String itemText = dragboard.getString();

                TextInputDialog noteInput = new TextInputDialog();
                noteInput.setTitle("Add Narrative");
                noteInput.setHeaderText("Add additional information about this status update");
                noteInput.setContentText(itemText + " " + UnitStatus.valueOf(target.getId()));

                Optional<String> result = noteInput.showAndWait();
                result.ifPresent(n -> {
                    VBox source = null;
                    for (var node : other.getChildren()) {
                        if (node instanceof VBox src && src.getId().equalsIgnoreCase(itemText)) {
                            source = src;
                            break;
                        }
                    }

                    if (source != null) {
                        Unit unit = Main.firegen.unitManager().match(source.getId()).getFirst();
                        other.getChildren().remove(source);
                        VBox newBox = this.createTileUnit(target, unit);
                        other.getChildren().add(newBox);
                    }
                });

                success = true;
            }

            e.setDropCompleted(success);
            e.consume();
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

}
