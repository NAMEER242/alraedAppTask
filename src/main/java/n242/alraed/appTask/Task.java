package n242.alraed.appTask;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import n242.alraed.appTask.MyTools.ActiveWindows;
import n242.alraed.appTask.models.LoadingWindowData;
import n242.alraed.appTask.models.ReceiptsViewerData;
import n242.alraed.appTask.secondaryView.LoadingWindow;

import java.io.IOException;

public class Task extends Application {

    public static Scene scene;
    public static Stage stage;
    static private Stage loginStage;

    @Override
    public void start(Stage stage) throws IOException {

        openLoginView(stage);

    }

    public static void openHomeView(Stage stage) {
        try {

            loginStage.close();

            Task.stage = stage;
            FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "home-view.fxml" ) );
            scene = new Scene( fxmlLoader.load() );
            scene.setFill( Color.TRANSPARENT);
            stage.setScene( scene );
            stage.initStyle( StageStyle.TRANSPARENT );
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLoginView(Stage homeStage) throws IOException {
        Stage stage = new Stage();
        loginStage = stage;

        LoadingWindowData.stage = stage;
        LoadingWindowData.homeStage = homeStage;

        FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "secondaryView/loadingWindow.fxml" ) );
        Scene scene = new Scene( fxmlLoader.load() );
        scene.setFill( Color.TRANSPARENT);
        stage.setScene( scene );
        stage.initStyle( StageStyle.TRANSPARENT );
        stage.setAlwaysOnTop( true );
        stage.show();
        ActiveWindows.windows.add( stage );
    }

    public static void main(String[] args) {
        launch();
    }
}