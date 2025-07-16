package net.noahf.firewatch.desktopclient;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.noahf.firewatch.desktopclient.displays.CallListScreen;

public class JavaFXManager extends Application {

    public static final double WINDOW_SIZE = 800D;

    private Stage stage;
    private GUIPage currentPage;

    public JavaFXManager(String[] args) {
        launch(args);
    }
    public JavaFXManager() { } // required for JavaFX

    @Override
    public void start(Stage stage) throws Exception {
        Main.fx = this;
        this.stage = stage;

        stage.setTitle("FireGen");
        this.currentPage = new CallListScreen(this);
        stage.show();
    }

    public Stage getStage() {
        return this.stage;
    }

}
