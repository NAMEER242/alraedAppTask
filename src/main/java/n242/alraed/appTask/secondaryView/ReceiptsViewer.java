package n242.alraed.appTask.secondaryView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import javax.swing.text.DateFormatter;
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
    private final String serverDomain = "https://alraedTestingServer.pythonanywhere.com/";

    public ReceiptsViewer() {

        stage = ReceiptsViewerData.stage;
        csrf = ReceiptsViewerData.csrf;
        receiptsJson = getReceiptsJson(0);

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

    private String getReceiptsJson(int level) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put( "X-CSRFToken", csrf );
        byte[][] output = sendRequest( "GET", serverDomain, "getReceipts/", headers, "level=" + level );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        return response;
    }

    private byte[][] sendRequest(String requestType, String host, String apiEndPoint, HashMap<String, String> headers, String... prams) {

        StringBuilder parameters = new StringBuilder();
        if (prams != null && prams.length > 0) {
            parameters.append( "?" );
            for (String p : prams) {
                parameters.append( p ).append( "&" );
            }
        }

        String charset = "UTF-8";
        HttpURLConnection connection;
        byte[] output = null;
        String csrf = "", session;
        String cookies = "";

        try {

            connection = (HttpURLConnection) (new URL(
                    host + apiEndPoint + parameters
            ).openConnection());

            connection.setRequestMethod( requestType );
            connection.setRequestProperty( "Accept-Charset", charset );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded;charset=" + charset );
            for (String k : headers.keySet()) {
                connection.addRequestProperty( k, headers.get( k ) );
            }

            if (connection.getHeaderFields().containsKey( "Set-Cookie" )) {
                String csrfToken = connection.getHeaderFields().get( "Set-Cookie" ).get( 1 );
                String sessionToken = connection.getHeaderFields().get( "Set-Cookie" ).get( 0 );
                csrf = csrfToken.substring( 10, csrfToken.indexOf( ';' ) );
                session = sessionToken.substring( 10, sessionToken.indexOf( ';' ) );
                cookies = "csrftoken=" + csrf + "; sessionid=" + session;
            }

            InputStream is = connection.getInputStream();
            output = is.readAllBytes();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[][]{output, csrf.getBytes(), cookies.getBytes()};
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
            Long datetime = data.getLong( "datetime" );

            JSONObject jo = new JSONArray( getItemName( product ) ).getJSONObject( 0 );
            product = jo.getJSONObject( jo.keys().next() ).getString( "title" );

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
            DateFormat df = new SimpleDateFormat("yy/MM/dd");
            String finalDate = new DateFormatter( df ).valueToString( (float) datetime );
            date.setText( finalDate );

            receiptsViewer_fp.getChildren().add( view );

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    private String getItemName(String id) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put( "X-CSRFToken", csrf );
        byte[][] output = sendRequest( "GET", serverDomain, "getItems/", headers, "IID=" + id );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        return response;
    }

    /**
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        tEvent.bu_Exit( mouseEvent );
    }

}
