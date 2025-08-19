package net.noahf.firewatch.desktopclient.objects;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.List;

public class NarrativeInputDialog extends Dialog<String> {

    private final GridPane grid;
    private final Label label;
    private final TextField textField;

    public NarrativeInputDialog() {
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        this.textField = new TextField();
        this.textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setFillWidth(textField, true);

        TextFields.bindAutoCompletion(this.textField,
                (suggestion) -> this.matchingItems(new ArrayList<>(List.of("abc", "test", "hiii zik !!!!")), suggestion.getUserText())
        );

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(this.getTitle());
        dialogPane.setHeaderText(this.getHeaderText());
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textField.getText() : null;
        });
    }

    static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(textField, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(textField::requestFocus);
    }

    private List<String> matchingItems(List<String> allItems, String prefix) {
        List<String> matches = new ArrayList<>();
        for (String s : allItems) {
            if (s.startsWith(prefix)) {
                matches.add(s);
            }
        }
        return matches ;
    }

}
