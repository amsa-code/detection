package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.CraftType.Behaviour;
import au.gov.amsa.detection.model.CraftType.Events.Create;

public class CraftTypeBehaviour implements Behaviour {

    private final CraftType self;

    public CraftTypeBehaviour(CraftType self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setName(event.getName());
        self.setDescription(event.getDescription());
    }

}
