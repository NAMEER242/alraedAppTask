package n242.alraed.appTask.secondaryView;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import n242.alraed.appTask.MyTools.TitleBar_Events;
import n242.alraed.appTask.Task;
import n242.alraed.appTask.models.LoadingWindowData;

public class LoadingWindow {

    @FXML
    private TextField password_tf;
    @FXML
    private Button login_bu;
    @FXML
    private TextField username_tf;
    @FXML
    private HBox navBar_hb;

    private TitleBar_Events tEvent;
    private Stage stage;
    private Stage homeStage;

    public LoadingWindow() {

        stage = LoadingWindowData.stage;
        homeStage = LoadingWindowData.homeStage;

        stage.setOnShown( windowEvent -> {

            tEvent = new TitleBar_Events( stage, new SVGPath(), false );

            //window DragDrop Task.
            stage.getScene().setCursor( Cursor.DEFAULT );
            navBar_hb.setOnMousePressed( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseDragged( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseReleased( tEvent.dragDrop_bar_event() );

            login_bu.setOnMouseClicked( loginEvent() );

        } );

    }

    private EventHandler<MouseEvent> loginEvent() {

        return mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                if (!username_tf.getText().equals( "" ) || !password_tf.getText().equals( "" )) {
                    LoadingWindowData.user = username_tf.getText();
                    LoadingWindowData.pass = password_tf.getText();
                    Task.openHomeView( homeStage );
                }
            }
        };

    }

    /**
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        tEvent.bu_Exit( mouseEvent );
    }

}
