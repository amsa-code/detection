package au.gov.amsa.detection;

import java.io.IOException;

import com.github.davidmoten.xuml.StateDiagramViewer;

public class StateDiagramViewerMain {

    public static void main(String[] args) throws IOException {
        new StateDiagramViewer().start("/domains.xml", "detection-domain");
    }
}
