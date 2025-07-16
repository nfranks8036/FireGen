package net.noahf.firewatch.desktopclient;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GUIPage {

    public void show() {
        Stage stage = Main.fx.getStage();

        Parent root = this.gui(stage);
        if (root == null) {
            throw new IllegalStateException("No root provided for GUIPage [" + this.getClass().getCanonicalName() + "]");
        }

        stage.setScene(new Scene(root, Main.fx.window, Main.fx.window));
        stage.show();
    }

    public abstract Parent gui(Stage stage);

}
