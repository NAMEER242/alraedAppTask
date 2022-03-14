package n242.alraed.appTask.secondaryView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import n242.alraed.appTask.MyTools.FixingScrollPaneSpeed;
import n242.alraed.appTask.MyTools.TitleBar_Events;
import n242.alraed.appTask.Task;
import n242.alraed.appTask.Tools.json.JSONObject;
import n242.alraed.appTask.models.CartViewerData;
import n242.alraed.appTask.models.ItemData;

public class CartViewer {

    private TitleBar_Events tEvent;
    @FXML
    private HBox navBar_hb;
    @FXML
    private TextField byr_tf;
    @FXML
    private Button buy_bu;
    @FXML
    private VBox items_vb;
    @FXML
    private Label totItems_lbl;
    @FXML
    private Button clear_bu;
    @FXML
    private ScrollPane cart_sp;

    private final Stage stage;
    private ArrayList<String> itemsJson;
    private final String csrf;
    private final String name;
    private final String serverDomain = "https://alraedTestingServer.pythonanywhere.com/";

    public CartViewer() {

        stage = CartViewerData.stage;
        itemsJson = CartViewerData.json;
        csrf = CartViewerData.csrf;
        name = CartViewerData.name;

        stage.setOnShown( windowEvent -> {

            tEvent = new TitleBar_Events( stage, new SVGPath(), false );

            //window DragDrop Task.
            stage.getScene().setCursor( Cursor.DEFAULT );
            navBar_hb.setOnMousePressed( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseDragged( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseReleased( tEvent.dragDrop_bar_event() );

            totItems_lbl.setText( String.valueOf( itemsJson.size() ) );
            clear_bu.setOnMouseClicked( onClearClick() );
            buy_bu.setOnMouseClicked( buyEvent() );
            addAllItems( items_vb );
            new FixingScrollPaneSpeed( cart_sp );

        } );

    }

    private EventHandler<MouseEvent> onClearClick() {
        return mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                itemsJson.clear();
                totItems_lbl.setText( "0" );
                items_vb.getChildren().removeAll( items_vb.getChildren() );
            }
        };
    }

    private void addItem(VBox items_vb, int i) {

        try {

            ItemData itemData = new ItemData();
            JSONObject data = new JSONObject( itemsJson.get( i ) );
            itemData.title = data.getJSONObject( data.keys().next() ).getString( "title" );
            itemData.price = data.getJSONObject( data.keys().next() ).getFloat( "price" );
            itemData.IID = data.keys().next();

            URL itemUrl = Task.class.getResource( "fxmlFiles/cartVboxItem.fxml" );
            Label item = (Label) new FXMLLoader( itemUrl ).load();
            item.setText( itemData.title );

            Button removeItem_bu = ((Button) item.getGraphic());
            removeItem_bu.setOnMouseClicked( removeItemEvent( itemsJson.get( i ) ) );

            items_vb.getChildren().add( item );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addAllItems(VBox items_vb) {
        items_vb.getChildren().removeAll( items_vb.getChildren() );
        for (int i = 0; i < itemsJson.size(); i++) {
            addItem( items_vb, i );
        }
    }

    private EventHandler<MouseEvent> removeItemEvent(String item) {
        return mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                if (itemsJson.contains( item )) {
                    itemsJson.remove( item );
                    addAllItems( items_vb );
                    totItems_lbl.setText( String.valueOf( itemsJson.size() ) );
                }
            }
        };
    }

    private EventHandler<MouseEvent> buyEvent() {
        return mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                buyAllItems();
            }
        };
    }

    private void buyItem(String prods, float price) {

        HashMap<String, String> headers = new HashMap<>();
        headers.put( "X-CSRFToken", csrf );
        byte[][] output = sendRequest( "POST", serverDomain, "addReceipt/", headers,
                "slr=" + name,
                "byr=" + byr_tf.getText(),
                "prod=" + prods,
                "notes=None",
                "price=" + price
        );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        System.out.println(response);

    }

    private void buyAllItems() {
        if (!byr_tf.getText().equals( "" )) {

            String prods = new JSONObject( itemsJson.get( 0 ) ).keys().next();
            float price = 0;

            for (int i = 1; i < itemsJson.size(); i++) {
                JSONObject data = new JSONObject( itemsJson.get( i ) );
                prods += "," + data.keys().next();
                price += data.getJSONObject( data.keys().next() ).getFloat( "price" );
            }

            buyItem( prods, price );
            itemsJson.clear();
            totItems_lbl.setText( "0" );
            items_vb.getChildren().removeAll( items_vb.getChildren() );

        }
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

    /**
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        tEvent.bu_Exit( mouseEvent );
    }

}
