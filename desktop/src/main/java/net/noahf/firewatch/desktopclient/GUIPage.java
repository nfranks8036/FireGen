package net.noahf.firewatch.desktopclient;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GUIPage {

    protected final JavaFXManager fx;

    public GUIPage(JavaFXManager fx) {
        this.fx = fx;
    }

    public void show() {
        Stage stage = this.fx.getStage();
        stage.setScene(new Scene(this.gui(stage), JavaFXManager.WINDOW_SIZE, JavaFXManager.WINDOW_SIZE));
        stage.show();
    }

    public abstract Parent gui(Stage stage);

}
