package net.noahf.firewatch.desktopclient;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.noahf.firewatch.desktopclient.objects.Arrow;

import java.util.function.Supplier;

import static net.noahf.firewatch.desktopclient.JavaFXManager.APPLICATION_NAME;

public abstract class GUIPage {

    private static final int TITLE_HEIGHT = 20 + 36;
    private static final double TITLE_ELEMENT_SIZES = 36.0;
    private static final Insets TITLE_PADDING = new Insets(20.0, 0.0, 0.0, 20.0);

    private Supplier<String> title;
    private final GUIPage back;
    private final Stage stage;
    private final boolean backFunctionality;

    private Label titleText;

    public GUIPage(String title) {
        this(() -> title);
    }

    public GUIPage(Supplier<String> title) {
        this(title, () -> Main.fx.getStage(), true);
    }

    public GUIPage(Supplier<String> title, Supplier<Stage> customStage, boolean backFunctionality) {
        this.title = title;
        this.back = (backFunctionality ? Main.fx.getCurrentPage() : Main.fx.getCurrentPage().back);
        this.stage = customStage.get();
        this.backFunctionality = backFunctionality;
    }

    public void show() {
        this.loading(stage);

        Platform.runLater(() -> {
            AnchorPane masterRoot = new AnchorPane();

            VBox titleContainer = this.constructTitleBar();

            Node[] root = this.gui(stage);
            if (root == null) {
                throw new IllegalStateException("No root(s) provided for [" + this.getClass().getCanonicalName() + "]");
            }

            titleContainer.getChildren().addAll(root);
            masterRoot.getChildren().add(titleContainer);

            stage.setScene(new Scene(masterRoot));
            stage.setTitle(APPLICATION_NAME + " - " + this.title.get());
            stage.show();
        });
    }

    public abstract Node[] gui(Stage stage);

    protected void setDynamicTitle(Supplier<String> dynamicTitle) {
        this.title = dynamicTitle;
        this.titleText.setText(dynamicTitle.get());
        this.stage.setTitle(APPLICATION_NAME + " - " + this.title.get());
    }

    protected String title() { return this.title.get(); }

    public DoubleBinding width() { return this.stage.widthProperty().add(0); }
    public DoubleBinding height() {
        return this.stage.heightProperty().subtract(TITLE_HEIGHT);
    }

    private void loading(Stage stage) {
        AnchorPane anchor = new AnchorPane();
        if (this.back != null) {
            this.stage.setWidth(this.back.stage.getWidth());
            this.stage.setHeight(this.back.stage.getHeight());
        }

        Label label = new Label("Loading your content...");
        label.setFont(new Font(TITLE_ELEMENT_SIZES));
        label.setPadding(TITLE_PADDING);
        anchor.getChildren().addAll(label);
        Scene loading = new Scene(anchor);
        loading.setCursor(Cursor.WAIT);
        stage.setTitle(APPLICATION_NAME + " - " + this.title() + " - Loading...");
        stage.setScene(loading);
    }

    private VBox constructTitleBar() {
        VBox titleContainer = new VBox();

        HBox titleBar = new HBox();
//        titleBar.setPrefWidth(Main.fx.window);
        titleBar.setPrefHeight(TITLE_HEIGHT);

        if (this.backFunctionality && this.back != null) {
            StackPane goBackContainer = new StackPane();
            goBackContainer.setPadding(TITLE_PADDING);

            Arrow goBack = new Arrow(TITLE_ELEMENT_SIZES);
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
        this.titleText.setFont(new Font(TITLE_ELEMENT_SIZES));
        this.titleText.setPadding(TITLE_PADDING);
        titleBar.getChildren().add(this.titleText);

        titleContainer.getChildren().add(titleBar);

        return titleContainer;
    }

}
