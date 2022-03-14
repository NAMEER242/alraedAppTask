package n242.alraed.appTask.MyTools;

import javafx.scene.control.ScrollPane;

public class FixingScrollPaneSpeed {
    public FixingScrollPaneSpeed(ScrollPane sp) {
        //Fix scroll pane slow scrolling
        sp.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();
            double contentHeight = sp.getContent().getBoundsInLocal().getHeight();
            double scrollPaneHeight = sp.getHeight();
            double diff = contentHeight - scrollPaneHeight;
            if (diff < 1) diff = 1;
            double vvalue = sp.getVvalue();
            sp.setVvalue(vvalue + -deltaY/diff);
        });
    }
}
