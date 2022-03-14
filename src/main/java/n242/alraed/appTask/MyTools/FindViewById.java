package n242.alraed.appTask.MyTools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FindViewById {

    public Object ctrl;
    private final List<String> notCtrlNode = Arrays.asList( "content", "children", "tabs" );

    /**
     * get a controller from fxml file by controller id.
     * @param projectFxmlUrl the Url of the fxml file.
     * @param rootController the root controller in the fxml file.
     * @param controllerType controller type.
     * @param controllerId controller Id.
     */
    public FindViewById(URL projectFxmlUrl, Object rootController,
                        String controllerType, String controllerId) {
        ctrl = getController(
                rootController, getParentsList( projectFxmlUrl, controllerType, controllerId )
        );
        if (!ctrl.getClass().getSimpleName().equals( controllerType )) {
            throw new RuntimeException(
                    "can't find this controller!! the last controller was :" +
                            ctrl.getClass().getSimpleName()
            );
        }
   }

    /**
     * get the controller as Object by inserting the controller parent tree
     * and a Url of the fxml where the controller defined.
     *
     * @param rootController the root controller in the fxml file.
     * @param ctrls parent tree.
     * @return the controller as Object.
     */
    private Object getController(Object rootController, ArrayList<Controllers> ctrls) {

        Object ctrl = rootController;
        for (int i = 0; i < ctrls.size() - 1; i++) {
            ctrl = getChild( ctrl, ctrls.get( i ).Ctrl.getNodeName(), ctrls.get( i + 1 ).index );
        }

        return ctrl;
    }

    /**
     * get a specific child from a wrapper controller (ex: AnchorPane).
     * @param wrapperCtrl the wrapper controller.
     * @param objectType wrapper controller type.
     * @param controllerIndex the child controller index.
     * @return child controller as Object.
     */
    private Object getChild(Object wrapperCtrl, String objectType, int controllerIndex) {

        try {
            return switch (objectType) {
                case "ScrollPane" -> ((ScrollPane) wrapperCtrl).getContent();
                case "VBox" -> ((VBox) wrapperCtrl).getChildren().get( controllerIndex );
                case "HBox" -> ((HBox) wrapperCtrl).getChildren().get( controllerIndex );
                case "AnchorPane" -> ((AnchorPane) wrapperCtrl).getChildren().get( controllerIndex );
                case "Tab" -> ((Tab) wrapperCtrl).getContent();
                case "TabPane" -> ((TabPane) wrapperCtrl).getTabs().get( controllerIndex );
                default -> throw new RuntimeException(
                        "The Controller (" + objectType + ") not found," +
                                "add it to the switch statement \n in " + this.getClass().getName() +
                                "in (getChildrenList()) function here(FindViewById.java:74) or add" +
                                " it to notCtrlNode list field to ignore it"
                );
            };
        } catch (ClassCastException e) {
            throw new RuntimeException( "can't find this controller!!" + "\n" + e );
        }

    }

    /**
     * get an ArrayList of parents tree for a controller with a specific id,the list is made
     * of Controller.Class that it contains an int index var and another Node var for each parent.
     * @param projectFxmlUrl the Url of the fxml file.
     * @param ControllerType type of the controller to get it's parents tree.
     * @param ControllerId controller Id.
     * @return an ArrayList of parents.
     */
    private ArrayList<Controllers> getParentsList(
            URL projectFxmlUrl, String ControllerType, String ControllerId) {

        ArrayList<Controllers> nodeList= new ArrayList<>();
        try {

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse( projectFxmlUrl.toString() );
            Element e = doc.getDocumentElement();

            NodeList nl = e.getElementsByTagName( ControllerType );
            Node node;
            NamedNodeMap attributes;

            for (int i = 0; i < nl.getLength(); i++) {
                attributes = nl.item( i ).getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    node = nl.item( i );

                    if (attributes.item( j ).getNodeValue().equals( ControllerId )) {

                        nodeList.add(0, new Controllers( getNodeIndex( node ), node ) );

                        while (!node.getParentNode().getNodeName().equals( "#document" )) {

                            node = node.getParentNode();

                            if (!notCtrlNode.contains( node.getNodeName() )) {
                                nodeList.add( 0, new Controllers(
                                        getNodeIndex( node ), node
                                ) );
                            }

                        }

                    }

                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return nodeList;
    }

    /**
     * get the index of a specific Controller Node.
     * @param node the Controller Node.
     * @return the index of the controller.
     */
    private int getNodeIndex(Node node) {
        Node parentNode = node.getParentNode();
        Node n;
        int notCtrlNodeCount = 0;
        for (int i = 0; i < parentNode.getChildNodes().getLength(); i++) {
            n = parentNode.getChildNodes().item( i );

            if (n.getNodeType() == 1) {
                if (n.equals( node ) && !notCtrlNode.contains( n.getNodeName() )) {
                    return i - notCtrlNodeCount;
                } else if (n.equals( node )) {
                    return -1;
                }
            } else {
                notCtrlNodeCount++;
            }

        }
        return 12;
    }


    /**
     * the controller class that is used to represent the parent list, and
     * it's hold the controller Node alongside with its index.
     */
    private class Controllers {

        int index;
        Node Ctrl;

        public Controllers(int index, Node ctrl) {
            this.index = index;
            Ctrl = ctrl;
        }

    }


}
