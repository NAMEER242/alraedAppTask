package n242.alraed.appTask.MyTools;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import n242.alraed.appTask.Task;

public class TitleBar_Events {

    private boolean canDrag = true;
    private double newOffset = 999999;
    private double xOffset;
    private double yOffset;
    private double lastX;
    private double lastY;
    private double lastWidth;
    private double lastHeight;
    private final Stage s;
    private final SVGPath fullButton_svg;
    private final boolean resizeable;

    public TitleBar_Events(Stage s, SVGPath fullButton_svg, boolean resizeable) {
        this.s = s;
        this.fullButton_svg = fullButton_svg;
        this.resizeable = resizeable;
    }

    public EventHandler<MouseEvent> dragDrop_bar_event() {

        return mouseEvent -> {

            if (!(Cursor.DEFAULT.equals( s.getScene().getCursor() ))) {
                canDrag = false;

            //don't delete this
            } else if (mouseEvent.getEventType().equals( MouseEvent.MOUSE_RELEASED )) {
                canDrag = true;
            } else if (
                    mouseEvent.getTarget().toString().equals( "HBox[id=full_hb]" ) ||
                    mouseEvent.getTarget().toString().equals( "HBox[id=minimize_hb]" ) ||
                    mouseEvent.getTarget().toString().equals( "VBox[id=exit_hb]" )
            ) {
                canDrag = false;
            }

            if (mouseEvent.getButton().equals( MouseButton.PRIMARY ) && canDrag) {

                //ON_MOUSE_CLICKED.
                if (mouseEvent.getEventType().equals( MouseEvent.MOUSE_CLICKED )) {

                    if (mouseEvent.getClickCount() == 2 && resizeable) {
                        bu_FullS(mouseEvent);
                    }

                    //ON_MOUSE_PRESSED.
                } else if (mouseEvent.getEventType().equals( MouseEvent.MOUSE_PRESSED )) {

                    xOffset = mouseEvent.getSceneX();
                    yOffset = mouseEvent.getSceneY();

                    //ON_MOUSE_DRAGGED.
                } else if (mouseEvent.getEventType().equals( MouseEvent.MOUSE_DRAGGED )) {

                    updateNewOffset( mouseEvent );

                    //set the new x and y.
                    if (newOffset != 999999) {
                        s.setX( mouseEvent.getScreenX() - newOffset );
                    } else {
                        s.setX( mouseEvent.getScreenX() - xOffset );
                    }
                    s.setY( mouseEvent.getScreenY() - yOffset );

                    //change full icon.
                    fullButton_svg.setContent(
                            "M2233.189-3250h-7.425v7.034h7.425Z"
                    );

                    //setOnTop.
                    if (s != Task.stage) {
                        s.setAlwaysOnTop( true );
                    }

                    //ON_MOUSE_RELEASED.
                } else if (mouseEvent.getEventType().equals( MouseEvent.MOUSE_RELEASED )) {
                    newOffset = 999999;
                    if (mouseEvent.getScreenY() == 0 && resizeable) {
                        bu_FullS( mouseEvent );
                    }
                }

            }

        };

    }

    private void updateNewOffset(MouseEvent mouseEvent) {
        Node n = (Node)mouseEvent.getSource();
        Window w = n.getScene().getWindow();

        double currentX = w.getX();
        double currentY = w.getY();
        double currentWidth = w.getWidth();
        double currentHeight = w.getHeight();

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        if( currentX == bounds.getMinX() &&
                currentY == bounds.getMinY() &&
                currentWidth == bounds.getWidth() &&
                currentHeight == bounds.getHeight() ) {

            // de-maximize the window (not same as minimize)
            w.setWidth(lastWidth);
            w.setHeight(lastHeight);
            s.setMaximized( false );

            double a = bounds.getMaxX() / mouseEvent.getSceneX();
            double p = (100 / a);
            newOffset = ((p / 100) * w.getWidth());

            w.setX(  mouseEvent.getScreenX() - newOffset );
            w.setY(0);

        }
    }

    /**
     * method for onMouseEntered event used to minimize the program.
     */
    public void bu_Minimize(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals( MouseButton.PRIMARY )) {
            s.setIconified(true);
        }
    }

    /**
     * method for onMouseClick event used to close the program.
     * Note: this is not for the main window.
     */
    public void bu_Exit(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals( MouseButton.PRIMARY )) {
            ActiveWindows.windows.remove( s );
            s.close();
        }
    }

    /**
     * method for onMouseClick event used to maximize the window.
     */
    public void bu_FullS(MouseEvent evt) {

        if (evt.getButton().equals( MouseButton.PRIMARY )) {

            Node n = (Node)evt.getSource();
            Window w = n.getScene().getWindow();

            double currentX = w.getX();
            double currentY = w.getY();
            double currentWidth = w.getWidth();
            double currentHeight = w.getHeight();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            //x,y == x,y  or  w,h == w,h.

            if((currentX != bounds.getMinX() &&
                    currentY != bounds.getMinY()) ||
                    (currentWidth != bounds.getWidth() &&
                            currentHeight != bounds.getHeight()) ) {

                w.setX(bounds.getMinX());
                w.setY(bounds.getMinY());
                w.setWidth(bounds.getWidth());
                w.setHeight(bounds.getHeight());
                s.setMaximized( true );

                lastX = currentX;  // save old dimensions
                lastY = currentY;
                lastWidth = currentWidth;
                lastHeight = currentHeight;

                //setNotOnTop.
                if (s != Task.stage) {
                    s.setAlwaysOnTop( false );
                }

            } else {

                // de-maximize the window
                w.setX(lastX);
                w.setY(lastY);
                w.setWidth(lastWidth);
                w.setHeight(lastHeight);
                s.setMaximized( false );

                //setOnTop.
                if (s != Task.stage) {
                    s.setAlwaysOnTop( true );
                }

            }

            evt.consume();  // don't bubble up to title bar

            if (fullButton_svg.getContent().equals( "M2233.189-3250h-7.425v7.034h7.425Z" )) {

                fullButton_svg.setContent(
                        "M2232.156-3250.682h-6.393v6.056h6.393v-1." +
                                "34h1.39v-6.092h-6.367v0h6.367v6.092h-1.39Z"
                );

            } else {

                fullButton_svg.setContent( "M2233.189-3250h-7.425v7.034h7.425Z" );

            }

        }

    }

}
