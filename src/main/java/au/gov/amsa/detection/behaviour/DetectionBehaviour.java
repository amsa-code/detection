package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.Detection.Behaviour;
import au.gov.amsa.detection.model.Detection.Events.Create;
import au.gov.amsa.detection.model.Detection.Events.Send;

public class DetectionBehaviour implements Behaviour {

    private final Detection self;

    public DetectionBehaviour(Detection self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        // won't be called
        throw new RuntimeException("unexpected, creation happens in Detection Rule");
    }

    @Override
    public void onEntrySent(Send event) {
        // TODO Auto-generated method stub
    }

}
