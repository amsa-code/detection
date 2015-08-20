package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Craft.Events.Create;
import au.gov.amsa.detection.model.CraftType;

public class CraftBehaviour implements Craft.Behaviour {

    private final Craft self;

    public CraftBehaviour(Craft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setMmsi(event.getMmsi());
        self.setCraftType_R3(CraftType.find(event.getCraftTypeID()).get());
    }

}
