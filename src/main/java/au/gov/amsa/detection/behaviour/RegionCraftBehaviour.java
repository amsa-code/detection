package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.DetectionRule;
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
    public void onEntryCreated(Create event) {
        throw new RuntimeException("not expected");
    }

    @Override
    public void onEntryOutsideRegion(Out event) {
        if (self.getLastExitTimeFromRegion().before(self.getLastTimeInRegion())) {
            self.setLastExitTimeFromRegion(event.getTime());
        }
    }

    @Override
    public void onEntryInsideRegion(In event) {
        if (self.getLastExitTimeFromRegion().after(self.getLastTimeInRegion())) {
            self.setLastTimeEntered(event.getTime());
            self.setLastTimeInRegion(event.getTime());
        }
        self.getRegion_R5().getDetectionRule_R1()
                .forEach(dr -> dr.signal(DetectionRule.Events.PositionInRegion.builder()
                        .altitudeMetres(event.getAltitudeMetres())
                        .craftID(self.getCraft_R15().getId()).latitude(event.getLatitude())
                        .longitude(event.getLongitude()).regionID(self.getId())
                        .time(event.getTime()).build()));
    }

}
