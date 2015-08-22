package au.gov.amsa.detection.behaviour;

import java.util.Date;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Region;
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
        self.setCraft_R5(Craft.find(event.getCraftID()).get());
        self.setRegion_R5(Region.find(event.getRegionID()).get());
        if (event.getInside()) {
            self.signal(RegionCraft.Events.In.builder().altitudeMetres(event.getAltitudeMetres())
                    .latitude(event.getLatitude()).longitude(event.getLongitude())
                    .time(event.getTime()).build());
        } else {
            self.signal(RegionCraft.Events.Out.builder().altitudeMetres(event.getAltitudeMetres())
                    .latitude(event.getLatitude()).longitude(event.getLongitude())
                    .time(event.getTime()).build());
        }
    }

    @Override
    public void onEntryInsideRegion(In event) {
        // TODO Auto-generated method stub

    }

}
