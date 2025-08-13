package net.noahf.firewatch.desktopclient;

import javafx.application.Application;
import javafx.stage.Stage;
import net.noahf.firewatch.desktopclient.displays.CallListScreen;

public class JavaFXManager extends Application {

    public static final String APPLICATION_NAME = "FireGen";

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

        this.stage.setTitle(APPLICATION_NAME);
        this.stage.setMaximized(true);
        this.setNewPage(new CallListScreen());
        this.stage.setResizable(true);
        this.stage.show();
    }

    public Stage getStage() {
        return this.stage;
    }

    public void setNewPage(GUIPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page != null");
        }
        this.currentPage = page;
        this.currentPage.show();
    }

    public GUIPage getCurrentPage() {
        return this.currentPage;
    }

}
