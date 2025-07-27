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
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GUIPage {

    private Supplier<String> title;
    private final GUIPage back;
    private final Stage stage;
    private final boolean backFunctionality;

    private Label titleText;

    public GUIPage(String title) {
        this(title, () -> Main.fx.getStage(), true);
    }

    public GUIPage(String title, Supplier<Stage> customStage, boolean backFunctionality) {
        this.title = () -> title;
        this.back = (backFunctionality ? Main.fx.getCurrentPage() : Main.fx.getCurrentPage().back);
        this.stage = customStage.get();
        this.backFunctionality = backFunctionality;
    }

    public void show() {
        this.loading(stage);

        Platform.runLater(() -> {
            AnchorPane masterRoot = new AnchorPane();
            masterRoot.setPrefWidth(Main.fx.window);
            masterRoot.setPrefHeight(Main.fx.window);

            VBox titleContainer = this.constructTitleBar();

            Node[] root = this.gui(stage);
            if (root == null) {
                throw new IllegalStateException("No root(s) provided for [" + this.getClass().getCanonicalName() + "]");
            }

            titleContainer.getChildren().addAll(root);
            masterRoot.getChildren().add(titleContainer);

            stage.setScene(new Scene(masterRoot, Main.fx.window, Main.fx.window));
            stage.setTitle("FireGen - " + this.title.get());
            stage.show();
        });
    }

    public abstract Node[] gui(Stage stage);

    protected void setDynamicTitle(Supplier<String> dynamicTitle) {
        this.title = dynamicTitle;
        this.titleText.setText(dynamicTitle.get());
    }

    protected String getTitle() { return this.title.get(); }

    private void loading(Stage stage) {
        AnchorPane anchor = new AnchorPane();
        anchor.setPrefWidth(800.0);
        anchor.setPrefHeight(800.0);
        Label label = new Label("Loading your content...");
        label.setFont(new Font(36.0));
        label.setPadding(new Insets(20.0, 0.0, 0.0, 20.0));
        anchor.getChildren().addAll(label);
        Scene loading = new Scene(anchor);
        loading.setCursor(Cursor.WAIT);
        stage.setTitle("FireGen - " + this.getTitle() + " - Loading...");
        stage.setScene(loading);
    }

    private VBox constructTitleBar() {
        VBox titleContainer = new VBox();
        titleContainer.setPrefHeight(Main.fx.window);
        titleContainer.setPrefWidth(Main.fx.window);

        HBox titleBar = new HBox();
        titleBar.setPrefWidth(Main.fx.window);
        titleBar.setPrefHeight(20 + 36);

        if (this.backFunctionality && this.back != null) {
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

            Tooltip tooltip = new Tooltip("Go Back\nTo \"" + this.back.title.get() +"\"");
            tooltip.setFont(new Font(20.0));
            tooltip.setShowDelay(Duration.millis(100.0));
            tooltip.setHideDelay(Duration.millis(2.5));
            Tooltip.install(goBackContainer, tooltip);

            titleBar.getChildren().add(goBackContainer);
        }

        this.titleText = new Label(this.title.get());
        this.titleText.setFont(new Font(36.0));
        this.titleText.setPadding(new Insets(20.0, 0.0, 0.0, 20.0));
        titleBar.getChildren().add(this.titleText);

        titleContainer.getChildren().add(titleBar);

        return titleContainer;
    }

}
