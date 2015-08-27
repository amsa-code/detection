package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.CompositeRegionMember;
import au.gov.amsa.detection.model.CompositeRegionMember.Behaviour;
import au.gov.amsa.detection.model.CompositeRegionMember.Events.Create;

public class CompositeRegionMemberBehaviour implements Behaviour {

    private final CompositeRegionMember self;

    public CompositeRegionMemberBehaviour(CompositeRegionMember self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("unexpected");
    }

}
