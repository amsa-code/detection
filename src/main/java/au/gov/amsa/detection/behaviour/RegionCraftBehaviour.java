package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.RegionCraft;
import au.gov.amsa.detection.model.RegionCraft.Behaviour;
import au.gov.amsa.detection.model.RegionCraft.Events.Create;
import au.gov.amsa.detection.model.RegionCraft.Events.In;
import au.gov.amsa.detection.model.RegionCraft.Events.Out;

public class RegionCraftBehaviour implements Behaviour {

    private final RegionCraft self;

    // use constructor injection
    public RegionCraftBehaviour(RegionCraft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("not expected");
    }

    @Override
    public void onEntryInside(In event) {
        in(event, true);
    }

    @Override
    public void onEntryNeverOutside(In event) {
        in(event, false);
    }

    private void in(In event, boolean hasBeenOutside) {
        if (self.getLastExitTimeFromRegion().after(self.getLastTimeInRegion())) {
            self.setLastTimeEntered(event.getTime());
            self.setLastTimeInRegion(event.getTime());
        }
        PositionInRegion positionInRegion = DetectionRule.Events.PositionInRegion.builder()
                .altitudeMetres(event.getAltitudeMetres()).craftID(self.getCraft_R15().getId())
                .latitude(event.getLatitude()).longitude(event.getLongitude()).time(event.getTime())
                .lastTimeEntered(self.getLastTimeEntered()).hasBeenOutsideRegion(hasBeenOutside)
                .lastExitTimeFromRegion(self.getLastExitTimeFromRegion()).build();

        self.getRegion_R5().getDetectionRule_R1().forEach(dr -> dr.signal(positionInRegion));
    }

    @Override
    public void onEntryOutside(Out event) {
        if (self.getLastExitTimeFromRegion().before(self.getLastTimeInRegion())) {
            self.setLastExitTimeFromRegion(event.getTime());
        }
    }

}
