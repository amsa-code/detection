package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.SimpleRegionType;
import au.gov.amsa.detection.model.SimpleRegionType.Behaviour;
import au.gov.amsa.detection.model.SimpleRegionType.Events.Create;
import xuml.tools.model.compiler.runtime.ArbitraryId;

public class SimpleRegionTypeBehaviour implements Behaviour {

    private final SimpleRegionType self;

    public SimpleRegionTypeBehaviour(SimpleRegionType self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setName(event.getName());
        self.setDescription(event.getDescription());
    }

}
