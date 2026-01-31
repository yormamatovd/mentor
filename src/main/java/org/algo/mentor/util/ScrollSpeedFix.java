package org.algo.mentor.util;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

public class ScrollSpeedFix {
    
    private static final double SCROLL_SPEED_MULTIPLIER = 3.0;
    
    public static void applyScrollSpeedFix(Node node) {
        if (node instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) node;
            
            scrollPane.setOnScroll(event -> {
                if (event.getDeltaY() == 0) {
                    return;
                }
                
                double deltaY = event.getDeltaY() * SCROLL_SPEED_MULTIPLIER;
                
                double height = scrollPane.getContent().getBoundsInLocal().getHeight();
                double vValue = scrollPane.getVvalue();
                
                scrollPane.setVvalue(vValue + -deltaY / height);
                event.consume();
            });
        }
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyScrollSpeedFix(child);
            }
        }
    }
}
