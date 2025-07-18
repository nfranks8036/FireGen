package net.noahf.firewatch.desktopclient;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.noahf.firewatch.desktopclient.objects.Arrow;

import javax.tools.Tool;

public abstract class GUIPage {

    private final String title;
    private final GUIPage back;

    public GUIPage(String title) {
        this.title = title;
        this.back = Main.fx.getCurrentPage();
    }

    public void show() {
        Stage stage = Main.fx.getStage();

        this.loading(stage);

        Platform.runLater(() -> {
            AnchorPane masterRoot = new AnchorPane();
            masterRoot.setPrefWidth(Main.fx.window);
            masterRoot.setPrefHeight(Main.fx.window);

            VBox titleContainer = this.constructTitleBar();

            Node[] root = this.gui(stage);
            if (root == null) {
                throw new IllegalStateException("No root provided for GUIPage [" + this.getClass().getCanonicalName() + "]");
            }

            titleContainer.getChildren().addAll(root);
            masterRoot.getChildren().add(titleContainer);

            stage.setScene(new Scene(masterRoot, Main.fx.window, Main.fx.window));
            stage.show();
        });
    }

    public abstract Node[] gui(Stage stage);

    private void loading(Stage stage) {
        AnchorPane anchor = new AnchorPane();
        anchor.setPrefWidth(800.0);
        anchor.setPrefHeight(800.0);
        Label label = new Label("Loading your content...");
        label.setFont(new Font(30.0));
        label.setPadding(new Insets(20.0, 0.0, 0.0, 20.0));
        anchor.getChildren().addAll(label);
        Scene loading = new Scene(anchor);
        loading.setCursor(Cursor.WAIT);
        stage.setScene(loading);
    }

    private VBox constructTitleBar() {
        VBox titleContainer = new VBox();
        titleContainer.setPrefHeight(Main.fx.window);
        titleContainer.setPrefWidth(Main.fx.window);

        HBox titleBar = new HBox();
        titleBar.setPrefWidth(Main.fx.window);
        titleBar.setPrefHeight(20 + 36);

        if (this.back != null) {
            StackPane goBackContainer = new StackPane();
            goBackContainer.setPadding(new Insets(20.0, 0.0, 0.0, 20.0));

            Arrow goBack = new Arrow(36.0);
            goBackContainer.getChildren().add(goBack);

            goBackContainer.setCursor(Cursor.CLOSED_HAND);
            goBackContainer.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 1) {
                    Main.fx.setNewPage(this.back);
                }
                event.consume();
            });

            Tooltip tooltip = new Tooltip("Go Back\nTo \"" + this.back.title +"\"");
            tooltip.setFont(new Font(20.0));
            tooltip.setShowDelay(Duration.millis(100.0));
            tooltip.setHideDelay(Duration.millis(2.5));
            Tooltip.install(goBackContainer, tooltip);

            titleBar.getChildren().add(goBackContainer);
        }

        Label titleText = new Label(this.title);
        titleText.setFont(new Font(36.0));
        titleText.setPadding(new Insets(20.0, 0.0, 0.0, 20.0));
        titleBar.getChildren().add(titleText);

        titleContainer.getChildren().add(titleBar);

        return titleContainer;
    }

}
