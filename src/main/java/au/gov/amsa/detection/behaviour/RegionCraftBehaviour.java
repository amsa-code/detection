package au.gov.amsa.detection.behaviour;

import java.util.Date;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.RegionCraft;
import au.gov.amsa.detection.model.RegionCraft.Behaviour;
import au.gov.amsa.detection.model.RegionCraft.Events.Create;
import au.gov.amsa.detection.model.RegionCraft.Events.In;
import au.gov.amsa.detection.model.RegionCraft.Events.Out;

public class RegionCraftBehaviour implements Behaviour {

    private final RegionCraft self;

    public RegionCraftBehaviour(RegionCraft self) {
        this.self = self;
    }

    @Override
    public void onEntryOutsideRegion(Out event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setLastExitTimeFromRegion(new Date(Long.MIN_VALUE));
        self.setLastTimeEntered(new Date(Long.MIN_VALUE));
        self.setLastTimeInRegion(new Date(Long.MIN_VALUE));
    }

    @Override
    public void onEntryInsideRegion(In event) {
        // TODO Auto-generated method stub

    }

}
