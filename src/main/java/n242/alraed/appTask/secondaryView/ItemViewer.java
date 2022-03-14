package n242.alraed.appTask.secondaryView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import n242.alraed.appTask.MyTools.TitleBar_Events;
import n242.alraed.appTask.Tools.MultipartUtility;
import n242.alraed.appTask.models.ItemData;
import n242.alraed.appTask.models.ItemViewerInitialData;

public class ItemViewer {

    private TitleBar_Events tEvent;
    @FXML private HBox navBar_hb;
    @FXML private Button bu;
    @FXML private ImageView img;
    @FXML private TextField itemName_tf;
    @FXML private TextField price_tf;
    @FXML private TextArea des_ta;

    private final String serverDomain = "https://alraedTestingServer.pythonanywhere.com/";
    private final Stage stage;
    private final String csrf;
    private final Image image;
    private ItemData data;
    private File newImage;
    private boolean isNewItem;

    public ItemViewer() {

        csrf = ItemViewerInitialData.csrf;
        stage = ItemViewerInitialData.stage;
        data = ItemViewerInitialData.data;
        image = ItemViewerInitialData.img;
        isNewItem = ItemViewerInitialData.isNewItem;

        stage.setOnShown( windowEvent -> {

            tEvent = new TitleBar_Events( stage, new SVGPath(), false );

            //window DragDrop Task.
            stage.getScene().setCursor( Cursor.DEFAULT );
            navBar_hb.setOnMousePressed( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseDragged( tEvent.dragDrop_bar_event() );
            navBar_hb.setOnMouseReleased( tEvent.dragDrop_bar_event() );

            setViewerData( data );
            if (!isNewItem) {
                bu.setOnMouseClicked( edit() );
            } else {
                itemName_tf.setEditable( true );
                price_tf.setEditable( true );
                des_ta.setEditable( true );
                img.setOnMouseClicked( onImageClick() );

                bu.setOnMouseClicked( saveEditing() );
                bu.setText( "Save" );
            }

        } );

    }

    private void setViewerData(ItemData data) {

        img.setImage( image );
        setImageViewRadius( img, 30 );
        itemName_tf.setText( data.title );
        des_ta.setText( data.description );
        price_tf.setText( String.valueOf(data.price) + '$' );

    }

    private EventHandler<MouseEvent> edit() {
        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                itemName_tf.setEditable( true );
                price_tf.setEditable( true );
                des_ta.setEditable( true );
                img.setOnMouseClicked( onImageClick() );

                bu.setOnMouseClicked( saveEditing() );
                bu.setText( "Save" );

            }

        };
    }

    private EventHandler<MouseEvent> onImageClick() {
        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){
                imageChooser();
            }

        };
    }

    private EventHandler<MouseEvent> saveEditing() {
        return mouseEvent -> {

            if (mouseEvent.getButton() == MouseButton.PRIMARY){

                itemName_tf.setEditable( false );
                price_tf.setEditable( false );
                des_ta.setEditable( false );
                img.setOnMouseClicked( null );
                bu.setOnMouseClicked( null );
                bu.setText( "Saving!!" );

                ItemData newData = new ItemData();
                String s = price_tf.getText().substring( 0, price_tf.getText().length() - 1 );
                newData.price = Float.parseFloat( s );
                newData.description = des_ta.getText();
                newData.title = itemName_tf.getText();
                newData.IID = data.IID;

                if (!ItemDataEquals( data, newData ) || newImage != null) {

                    String res = null;
                    try {
                        res = editItem( newData, csrf );
                    } catch (ExecutionException | TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    while (true) {
                        if (res != null) {
                            String sRes = res;
                            System.out.println( sRes );
                            break;
                        }
                        try {
                            Thread.sleep( 10 );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    System.out.println( "noChanges!!" );
                }

                bu.setOnMouseClicked( edit() );
                bu.setText( "Edit" );

            }

        };
    }

    private boolean ItemDataEquals(ItemData id1, ItemData id2) {
        return (id1.title.equals( id2.title ) &&
                id1.IID.equals( id2.IID ) &&
                id1.price == id2.price &&
                id1.description.equals( id2.description ));
    }

    private String editItem(ItemData data, String csrf) throws ExecutionException, InterruptedException, TimeoutException {
        Task<String> t = new Task<>(){

            @Override
            protected String call() {
                if (csrf != null) {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put( "X-CSRFToken", csrf );

                    if (!isNewItem){
                        return sendRequestWithFile(
                                serverDomain, "editItem/", headers, newImage,
                                "id=" + data.IID,
                                "title=" + data.title,
                                "des=" + data.description,
                                "price=" + data.price
                        );
                    } else {
                        return sendRequestWithFile(
                                serverDomain, "addItem/", headers, newImage,
                                "title=" + data.title,
                                "des=" + data.description,
                                "totSld=" + 0,
                                "price=" + data.price
                        );
                    }

                }
                return "Error!!!";
            }
        };
        t.run();
        return t.get();
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

            connection = (HttpURLConnection ) (new URL(
                    host + apiEndPoint + parameters
            ).openConnection());

            connection.setRequestMethod( requestType );
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
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

    private String sendRequestWithFile(String host, String apiEndPoint, HashMap<String, String> headers, File upload, String... prams) {

        String res = "";

        StringBuilder parameters = new StringBuilder();
        if (prams != null && prams.length > 0) {
            parameters.append( "?" );
            for (String p : prams) {
                parameters.append( p ).append( "&" );
            }
        }

        String url = host + apiEndPoint + parameters;
        try {

            MultipartUtility multipart = new MultipartUtility(url, "UTF-8");
            for (String k : headers.keySet()) {
                multipart.addHeaderField( k, headers.get( k ) );
            }

            if (upload != null) {
                multipart.addFilePart( "img", upload );
            }
            res = multipart.finish();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * fileChooser dialog for image choose.
     */
    private void imageChooser() {
        try {
            FileChooser ic = new FileChooser();
            Stage stage = new Stage();
            ic.setTitle( "selectImages" );
            ic.getExtensionFilters().addAll( new FileChooser.ExtensionFilter( "Images", "*.*" ) );
            newImage = ic.showOpenMultipleDialog( stage ).get( 0 );
        } catch (NullPointerException e) {
            System.out.println("there are no images chosen");
        }
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
     * method for onMouseClick event used to close the program.
     */
    @FXML
    private void bu_Exit(MouseEvent mouseEvent) {
        tEvent.bu_Exit( mouseEvent );
    }

}
