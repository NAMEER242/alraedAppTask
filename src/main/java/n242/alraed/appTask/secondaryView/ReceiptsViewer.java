package n242.alraed.appTask.secondaryView;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import n242.alraed.appTask.MyTools.FindViewById;
import n242.alraed.appTask.MyTools.FixingScrollPaneSpeed;
import n242.alraed.appTask.MyTools.TitleBar_Events;
import n242.alraed.appTask.Task;
import n242.alraed.appTask.Tools.json.JSONArray;
import n242.alraed.appTask.Tools.json.JSONObject;
import n242.alraed.appTask.models.ReceiptsViewerData;

public class ReceiptsViewer {

    @FXML
    private HBox navBar_hb;
    @FXML
    private FlowPane receiptsViewer_fp;
    @FXML
    private ScrollPane receiptsView_sp;

    private Stage stage;
    private String csrf;
    private String receiptsJson;
    private TitleBar_Events tEvent;

    public ReceiptsViewer() {

        stage = ReceiptsViewerData.stage;
        csrf = ReceiptsViewerData.csrf;
        receiptsJson = ReceiptsViewerData.receiptsJson;

        stage.setOnShown( windowEvent -> {

            tEvent = new TitleBar_Events( stage, new SVGPath(), false );

            //window DragDrop Task.
            stage.getScene().setCursor( Cursor.DEFAULT );
            navBar_hb.setOnMousePressed( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseDragged( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseReleased( tEvent.dragDrop_bar_event() );

            addAllReceipts( receiptsViewer_fp );
            new FixingScrollPaneSpeed( receiptsView_sp );

        } );

    }

    private void addAllReceipts(FlowPane receiptsViewer_fp) {

        if (!receiptsJson.equals( "" ) && !receiptsJson.equals( "Error!!" )){

            receiptsViewer_fp.getChildren().removeAll( receiptsViewer_fp.getChildren() );
            JSONArray ja = new JSONArray( receiptsJson );

            for (int i = 0; i < ja.length(); i++) {
                addReceipt( receiptsViewer_fp, ja.getJSONObject( i ) );
            }

        }

    }

    private void addReceipt(FlowPane receiptsViewer_fp, JSONObject data) {

        try {

            String seller = data.getString( "seller" );
            String buyer = data.getString( "buyer" );
            String product = data.getString( "product" );
            float price = data.getFloat( "price" );
            int datetime = data.getInt( "datetime" );

            URL viewUrl = Task.class.getResource( "fxmlFiles/receiptsView.fxml" );
            VBox view = (VBox) new FXMLLoader( viewUrl ).load();

            Label slr = (Label) new FindViewById(
                    viewUrl, view, "Label", "slr_lbl"
            ).ctrl;

            Label byr = (Label) new FindViewById(
                    viewUrl, view, "Label", "byr_lbl"
            ).ctrl;

            Label totalPrice = (Label) new FindViewById(
                    viewUrl, view, "Label", "totPrice_lbl"
            ).ctrl;

            Label date = (Label) new FindViewById(
                    viewUrl, view, "Label", "date_lbl"
            ).ctrl;

            Label prods = (Label) new FindViewById(
                    viewUrl, view, "Label", "prods_lbl"
            ).ctrl;

            slr.setText( seller );
            byr.setText( buyer );
            prods.setText( product );
            totalPrice.setText( String.valueOf( price ) );
            Date dateTime = new Date(datetime);
            DateFormat df = new SimpleDateFormat("yy:MM:dd:HH:mm:ss");
            date.setText( df.format( dateTime ) );


            receiptsViewer_fp.getChildren().add( view );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        tEvent.bu_Exit( mouseEvent );
    }

}
