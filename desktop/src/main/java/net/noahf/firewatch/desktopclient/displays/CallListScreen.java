package net.noahf.firewatch.desktopclient.displays;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.noahf.firewatch.desktopclient.GUIPage;
import net.noahf.firewatch.desktopclient.JavaFXManager;

public class CallListScreen extends GUIPage {

    public CallListScreen(JavaFXManager fx) {
        super(fx);
    }

    @Override
    public Parent gui(Stage stage) {

        Text text = new Text("Current Calls");

        StackPane root = new StackPane(text);
        root.setAlignment(Pos.CENTER);

        return root;
    }
}
