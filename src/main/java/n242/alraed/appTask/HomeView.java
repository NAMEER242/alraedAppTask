package n242.alraed.appTask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import n242.alraed.appTask.MyTools.ActiveWindows;
import n242.alraed.appTask.MyTools.FindViewById;
import n242.alraed.appTask.MyTools.FixingScrollPaneSpeed;
import n242.alraed.appTask.MyTools.TitleBar_Events;
import n242.alraed.appTask.Tools.json.JSONArray;
import n242.alraed.appTask.Tools.json.JSONObject;
import n242.alraed.appTask.models.CartViewerData;
import n242.alraed.appTask.models.ItemData;
import n242.alraed.appTask.models.ItemViewerInitialData;
import n242.alraed.appTask.models.LoadingWindowData;
import n242.alraed.appTask.models.ReceiptsViewerData;

public class HomeView {

    @FXML
    private HBox navBar_hb;
    @FXML
    private SVGPath full;
    @FXML
    private Button ViewReceipts_bu;
    @FXML
    private Label totalCartItems_lbl;
    @FXML
    private Button myCart_bu;
    @FXML
    private Button refresh_bu;
    @FXML
    private Label userName;
    @FXML
    private ImageView userImg;
    @FXML
    private Button addItem_bu;
    @FXML
    private HBox topItemsList_hb;
    @FXML
    private FlowPane lastItemsList_fp;
    @FXML
    private ScrollPane homeView_sp;

    private String csrf;
    private final String serverDomain = "https://alraedTestingServer.pythonanywhere.com/";
    private ArrayList<String> cart = new ArrayList<>();
    private String receiptsJson;
    TitleBar_Events tEvent;

    public HomeView() {

        javafx.concurrent.Task<String[]> getServerContent = getServerContentTask();
        new Thread( getServerContent ).start();

        Task.stage.setOnShown( windowEvent -> {

            myCart_bu.setOnMouseClicked( openCart() );
            ViewReceipts_bu.setOnMouseClicked( openReceiptsView() );
            new FixingScrollPaneSpeed( homeView_sp );
            refresh_bu.setOnMouseClicked( refreshEvent() );

            tEvent = new TitleBar_Events( Task.stage, full, true );

            //window DragDrop Task.
            Task.scene.setCursor( Cursor.DEFAULT );
            navBar_hb.setOnMousePressed( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseDragged( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseReleased( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseClicked( tEvent.dragDrop_bar_event() );

        } );

    }

    private javafx.concurrent.Task<String[]> getServerContentTask() {

        Task.stage.setOpacity( 0 );

        javafx.concurrent.Task<String[]> getServerContent = new javafx.concurrent.Task<>() {
            @Override
            protected String[] call() {

                String userData = login(
                        "user=" + LoadingWindowData.user,
                        "pass=" + LoadingWindowData.pass
                );
                receiptsJson = getReceiptsData();
                String topItemsJson = getTopItems( csrf );
                String lastItems = getLastItems( csrf, 0 );
                return new String[]{topItemsJson, lastItems, userData};

            }
        };

        getServerContent.setOnSucceeded( workerStateEvent -> {
            try {

                setUserData( getServerContent.get()[2] );
                addAllItemView( getServerContent.get()[0], topItemsList_hb );
                addAllItemView( getServerContent.get()[1], lastItemsList_fp );
                addItem_bu.setOnMouseClicked( addItem() );

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Task.stage.setOpacity( 1 );
        } );

        return getServerContent;
    }

    private EventHandler<MouseEvent> refreshEvent() {
        return mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                new Thread( getServerContentTask() ).start();
            }
        };
    }

    private EventHandler<MouseEvent> addToCartOnClickEven() {
        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                String json = ((VBox) (
                        (HBox) ((Button) mouseEvent.getSource()).getParent()).getParent()
                ).getAccessibleText();
                cart.add( json );
                totalCartItems_lbl.setText( String.valueOf( cart.size() ) );
            }

        };
    }

    private void setUserData(String userData) {

        if (!userData.equals( "Error!!" ) && !userData.equals( "" )) {
            JSONObject data = new JSONArray( userData ).getJSONObject( 0 );
            String fName = data.getJSONObject( data.keys().next() ).getString( "fName" );
            String lName = data.getJSONObject( data.keys().next() ).getString( "lName" );
            String img = data.getJSONObject( data.keys().next() ).getString( "img" );
            img = new File( img ).getName();

            userName.setText( fName + " " + lName );
            byte[] imageBytes = getImageBytes( img );
            if (imageBytes != null) {
                userImg.setImage( new Image( new ByteArrayInputStream( imageBytes ) ) );
                setImageViewRadius( userImg, 40 );
            }
        } else {
            Task.stage.close();
        }

    }

    private String login(String user, String pass) {
        byte[][] output = sendRequest( "POST", serverDomain, "login/", new HashMap<>(), user, pass );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        csrf = new String( output[1], StandardCharsets.UTF_8 );
        return response;
    }

    private String getReceiptsData() {
        byte[][] output = sendRequest( "GET", serverDomain, "getReceipts/", new HashMap<>(),
                "level=0"
                );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        return response;
    }

    private String getTopItems(String csrf) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put( "X-CSRFToken", csrf );
        byte[][] output = sendRequest( "GET", serverDomain, "getTopItems/", headers, null );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        return response;
    }

    private String getLastItems(String csrf, int level) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put( "X-CSRFToken", csrf );
        byte[][] output = sendRequest( "GET", serverDomain, "getItems/", headers, "level=" + level );
        String response = new String( output[0], StandardCharsets.UTF_8 );
        return response;
    }

    private byte[] getImageBytes(String path) {

        File tempFiles = new File( Task.class.getResource("").getFile() + "tempFiles/" + path );
        if (tempFiles.exists()) {
            try {

                InputStream is = new FileInputStream( tempFiles );
                return is.readAllBytes();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (csrf != null) {

            try {

                HashMap<String, String> headers = new HashMap<>();
                headers.put( "X-CSRFToken", csrf );
                byte[] bytes =  sendRequest(
                        "GET", serverDomain, "images/" + path, headers, null
                )[0];

                tempFiles.createNewFile();
                OutputStream os = new FileOutputStream( tempFiles );
                os.write( bytes );
                os.close();

                return bytes;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
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

    private void addAllItemView(String jsonData, Pane viewList) {

        viewList.getChildren().removeAll( viewList.getChildren() );

        JSONArray data = new JSONArray( jsonData );
        for (int i = 0; i < data.length(); i++) {

            ItemData itemData = new ItemData();
            itemData.title = data.getJSONObject( i ).getJSONObject(
                    data.getJSONObject( i ).keys().next() ).getString( "title" );
            itemData.description = data.getJSONObject( i ).getJSONObject(
                    data.getJSONObject( i ).keys().next() ).getString( "description" );
            itemData.itemImagePath = data.getJSONObject( i ).getJSONObject(
                    data.getJSONObject( i ).keys().next() ).getString( "itemImage" );
            itemData.totalSoldProduct = data.getJSONObject( i ).getJSONObject(
                    data.getJSONObject( i ).keys().next() ).getInt( "totalSoldProduct" );

            addItemView( itemData, viewList, data.getJSONObject( i ).toString() );

        }

    }

    private void addItemView(ItemData data, Pane viewList, String jsonData) {

        VBox itemView = null;

        try {

            URL itemViewUrl = Task.class.getResource( "fxmlFiles/itemView.fxml" );
            itemView = (VBox) new FXMLLoader( itemViewUrl ).load();

            ImageView img = (ImageView) new FindViewById(
                    itemViewUrl, itemView, "ImageView", "img"
            ).ctrl;

            Label title = (Label) new FindViewById(
                    itemViewUrl, itemView, "Label", "title_lbl"
            ).ctrl;

            Label des = (Label) new FindViewById(
                    itemViewUrl, itemView, "Label", "des_lbl"
            ).ctrl;

            Button addToCart_bu = (Button) new FindViewById(
                    itemViewUrl, itemView, "Button", "addToCart_bu"
            ).ctrl;

            byte[] imageBytes = getImageBytes( new File( data.itemImagePath ).getName() );
            if (imageBytes != null) {
                img.setImage( new Image( new ByteArrayInputStream( imageBytes ) ) );
                setImageViewRadius( img, 10 );
            }
            title.setText( data.title );
            des.setText( data.description );

            itemView.setOnMouseClicked( onItemClick() );
            itemView.setAccessibleText( jsonData );

            addToCart_bu.setOnMouseClicked( addToCartOnClickEven() );

        } catch (IOException e) {
            e.printStackTrace();
        }

        viewList.getChildren().add( itemView );

    }


    //--------------------------------------------------------------------------------------------//


    private EventHandler<MouseEvent> onItemClick() {

        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                String json = ((VBox) mouseEvent.getSource()).getAccessibleText();

                ItemData itemData = new ItemData();
                JSONObject data = new JSONObject( json );
                itemData.title = data.getJSONObject( data.keys().next() ).getString( "title" );
                itemData.description = data.getJSONObject( data.keys().next() ).getString( "description" );
                itemData.price = data.getJSONObject( data.keys().next() ).getFloat( "price" );
                itemData.IID = data.keys().next();

                VBox itemView = ((VBox) mouseEvent.getSource());
                URL itemViewUrl = Task.class.getResource( "fxmlFiles/itemView.fxml" );
                Image img = ((ImageView) new FindViewById(
                        itemViewUrl, itemView, "ImageView", "img"
                ).ctrl).getImage();

                try {

                    Stage stage = new Stage();
                    ItemViewerInitialData.data = itemData;
                    ItemViewerInitialData.stage = stage;
                    ItemViewerInitialData.csrf = csrf;
                    ItemViewerInitialData.img = img;
                    ItemViewerInitialData.isNewItem = false;

                    FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "secondaryView/itemViewer.fxml" ) );
                    Scene scene = new Scene( fxmlLoader.load() );
                    scene.setFill( Color.TRANSPARENT );
                    stage.setScene( scene );
                    stage.initStyle( StageStyle.TRANSPARENT );
                    stage.setAlwaysOnTop( true );
                    stage.show();
                    ActiveWindows.windows.add( stage );

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };

    }

    private EventHandler<MouseEvent> addItem() {

        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                ItemData itemData = new ItemData();
                itemData.title = "";
                itemData.description = "";
                itemData.price = 0.0f;
                itemData.IID = "";

                try {

                    Stage stage = new Stage();
                    ItemViewerInitialData.data = itemData;
                    ItemViewerInitialData.stage = stage;
                    ItemViewerInitialData.csrf = csrf;
                    ItemViewerInitialData.img = null;
                    ItemViewerInitialData.isNewItem = true;

                    FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "secondaryView/itemViewer.fxml" ) );
                    Scene scene = new Scene( fxmlLoader.load() );
                    scene.setFill( Color.TRANSPARENT );
                    stage.setScene( scene );
                    stage.initStyle( StageStyle.TRANSPARENT );
                    stage.setAlwaysOnTop( true );
                    stage.show();
                    ActiveWindows.windows.add( stage );

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };

    }

    private EventHandler<MouseEvent> openCart() {

        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                try {

                    Stage stage = new Stage();
                    stage.setOnHiding( windowEvent -> {
                        totalCartItems_lbl.setText( String.valueOf( cart.size() ) );
                    } );
                    CartViewerData.stage = stage;
                    CartViewerData.json = cart;
                    CartViewerData.csrf = csrf;
                    CartViewerData.name = userName.getText();

                    FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "secondaryView/cartViewer.fxml" ) );
                    Scene scene = new Scene( fxmlLoader.load() );
                    scene.setFill( Color.TRANSPARENT );
                    stage.setScene( scene );
                    stage.initStyle( StageStyle.TRANSPARENT );
                    stage.setAlwaysOnTop( true );
                    stage.show();
                    ActiveWindows.windows.add( stage );

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };

    }

    private EventHandler<MouseEvent> openReceiptsView() {

        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                try {

                    Stage stage = new Stage();
                    ReceiptsViewerData.stage = stage;
                    ReceiptsViewerData.csrf = csrf;
                    ReceiptsViewerData.receiptsJson = receiptsJson;

                    FXMLLoader fxmlLoader = new FXMLLoader( Task.class.getResource( "secondaryView/receiptsViewer.fxml" ) );
                    Scene scene = new Scene( fxmlLoader.load() );
                    scene.setFill( Color.TRANSPARENT );
                    stage.setScene( scene );
                    stage.initStyle( StageStyle.TRANSPARENT );
                    stage.setAlwaysOnTop( true );
                    stage.show();
                    ActiveWindows.windows.add( stage );

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };

    }

    private void setImageViewRadius(ImageView imageView, int radius) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle(
                imageView.getFitWidth(), imageView.getFitHeight()
        );
        clip.setArcWidth(radius);
        clip.setArcHeight( radius );
        imageView.setClip(clip);

        // snapshot the rounded image.
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill( Color.TRANSPARENT);
        WritableImage image = imageView.snapshot(parameters, null);

        // remove the rounding clip so that our effect can show through.
        imageView.setClip(null);

        // store the rounded image in the imageView.
        imageView.setImage(image);
    }

    /**
     * method for onMouseEntered event used to minimize the program.
     */
    @FXML
    private void bu_Minimize(MouseEvent mouseEvent) {
        tEvent.bu_Minimize( mouseEvent );
    }

    /**
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        for (int i = 0; i < ActiveWindows.windows.size(); i++) {
            ActiveWindows.windows.get( i ).close();
        }
        tEvent.bu_Exit( mouseEvent );
    }

    /**
     * method for onMouseClick event used to maximize the window.
     */
    @FXML
    private void bu_FullS(MouseEvent evt) {
        tEvent.bu_FullS( evt );
    }

}
