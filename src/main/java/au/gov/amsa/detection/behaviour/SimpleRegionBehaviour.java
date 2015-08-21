package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.SimpleRegion;
import au.gov.amsa.detection.model.SimpleRegion.Behaviour;
import au.gov.amsa.detection.model.SimpleRegion.Events.Create;

public class SimpleRegionBehaviour implements Behaviour {

    private final SimpleRegion self;

    public SimpleRegionBehaviour(SimpleRegion self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setZippedShapefileBytes("B64 encoded zip archive bytes");
        Region region = Region.create(ArbitraryId.next());
        region.setName(event.getName());
        region.setDescription(event.getDescription());
        region.setSimpleRegion_R4(self);
        Context.em().persist(region);
        self.setRegion_R4(region);
    }

}
