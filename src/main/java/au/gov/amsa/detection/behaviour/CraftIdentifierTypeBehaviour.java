package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.CraftIdentifierType;
import au.gov.amsa.detection.model.CraftIdentifierType.Behaviour;
import au.gov.amsa.detection.model.CraftIdentifierType.Events.Create;
import au.gov.amsa.detection.model.CraftType;

public class CraftIdentifierTypeBehaviour implements Behaviour {

    private final CraftIdentifierType self;

    public CraftIdentifierTypeBehaviour(CraftIdentifierType self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setName(event.getName());
        self.setDescription(event.getDescription());
        self.setCraftType_R3(CraftType.find(event.getCraftTypeID()).get());
    }

}
