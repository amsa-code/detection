package au.gov.amsa.detection.behaviour;

import java.util.List;

import au.gov.amsa.detection.model.Context;
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
        throw new RuntimeException("doesn't get called");
    }

    @Override
    public void onEntryHasPosition(Position event) {
        List<RegionCraft> list = Context.em()
                .createQuery(
                        "select rc from RegionCraft rc where region_R5.id=:region_id and craft_R5.id=:craft_id",
                        RegionCraft.class)
                .setParameter("region_id", self.getId())
                .setParameter("craft_id", event.getCraftID()).getResultList();
        boolean inside = contains(event.getLatitude(), event.getLongitude());
        RegionCraft rc;
        if (list.size() > 0) {
            rc = list.get(0);
        } else {
            rc = Context.create(RegionCraft.class,
                    RegionCraft.Events.Create.builder().altitudeMetres(event.getAltitudeMetres())
                            .craftID(event.getCraftID()).latitude(event.getLatitude())
                            .longitude(event.getLongitude()).regionID(self.getId()).inside(inside)
                            .time(event.getTime()).build());
        }

        if (contains(event.getLatitude(), event.getLongitude())) {
            rc.signal(RegionCraft.Events.In.builder().altitudeMetres(event.getAltitudeMetres())
                    .latitude(event.getLatitude()).longitude(event.getLongitude())
                    .time(event.getTime()).build());
        } else {
            rc.signal(RegionCraft.Events.Out.builder().altitudeMetres(event.getAltitudeMetres())
                    .latitude(event.getLatitude()).longitude(event.getLongitude())
                    .time(event.getTime()).build());
        }
    }

    private boolean contains(Double latitude, Double longitude) {
        return true;
    }

}