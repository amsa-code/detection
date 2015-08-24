package au.gov.amsa.detection.behaviour;

import java.util.Date;
import java.util.List;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.Region.Behaviour;
import au.gov.amsa.detection.model.Region.Events.Create;
import au.gov.amsa.detection.model.Region.Events.Position;
import au.gov.amsa.detection.model.RegionCraft;

public class RegionBehaviour implements Behaviour {

    private final Region self;

    public RegionBehaviour(Region self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("should not be called");
    }

    @Override
    public void onEntryHasPosition(Position event) {
        List<RegionCraft> list = Context.em()
                .createQuery(
                        "select rc from RegionCraft rc where region_R5.id=:region_id and craft_R15.id=:craft_id",
                        RegionCraft.class)
                .setParameter("region_id", self.getId())
                .setParameter("craft_id", event.getCraftID()).getResultList();
        boolean inside = contains(event.getLatitude(), event.getLongitude());
        RegionCraft rc;
        if (list.size() > 0) {
            rc = list.get(0);
            if (inside) {
                rc.signal(RegionCraft.Events.In.builder().altitudeMetres(event.getAltitudeMetres())
                        .latitude(event.getLatitude()).longitude(event.getLongitude())
                        .time(event.getTime()).build());
            } else {
                rc.signal(RegionCraft.Events.Out.builder().altitudeMetres(event.getAltitudeMetres())
                        .latitude(event.getLatitude()).longitude(event.getLongitude())
                        .time(event.getTime()).build());
            }
        } else {
            // Creation of RegionCraft that also sets its new state without
            // triggering state machine
            rc = RegionCraft.create(ArbitraryId.next());
            if (inside) {
                rc.setLastExitTimeFromRegion(new Date(Long.MIN_VALUE));
                rc.setLastTimeEntered(event.getTime());
                rc.setState(RegionCraft.State.INSIDE_REGION.toString());
            } else {
                rc.setLastTimeEntered(event.getTime());
                rc.setLastExitTimeFromRegion(new Date(Long.MAX_VALUE));
                rc.setState(RegionCraft.State.OUTSIDE_REGION.toString());
            }
            Craft craft = Craft.find(event.getCraftID()).get();
            rc.setRegion_R5(self);
            rc.setCraft_R15(craft);
            rc.persist(Context.em());
        }

    }

    private boolean contains(Double latitude, Double longitude) {
        return true;
    }

}