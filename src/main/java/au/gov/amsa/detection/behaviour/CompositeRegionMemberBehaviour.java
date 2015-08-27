package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.CompositeRegionMember;
import au.gov.amsa.detection.model.CompositeRegionMember.Behaviour;
import au.gov.amsa.detection.model.CompositeRegionMember.Events.Create;

public class CompositeRegionMemberBehaviour implements Behaviour {

    public CompositeRegionMemberBehaviour(CompositeRegionMember self) {
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("unexpected");
    }

}
