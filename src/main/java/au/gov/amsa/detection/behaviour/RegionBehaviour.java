package au.gov.amsa.detection.behaviour;

import java.io.ByteArrayInputStream;
import java.util.Date;

import com.google.common.base.Optional;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.Shapefiles;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.Region.Behaviour;
import au.gov.amsa.detection.model.Region.Events.Position;
import au.gov.amsa.detection.model.Region.Events.StateSignature_Created;
import au.gov.amsa.detection.model.RegionCraft;
import au.gov.amsa.gt.Shapefile;

public class RegionBehaviour implements Behaviour {

    private final Region self;

    public RegionBehaviour(Region self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(StateSignature_Created event) {
        throw new RuntimeException("should not be called");
    }

    @Override
    public void onEntryHasPosition(Position event) {
        Optional<RegionCraft> rc = RegionCraft.select(RegionCraft.Attribute.craft_R15_id
                .eq(event.getCraftID()).and(RegionCraft.Attribute.region_R5_id.eq(self.getId())))
                .one();
        boolean inside = contains(event.getLatitude(), event.getLongitude());
        if (rc.isPresent()) {
            if (inside) {
                rc.get().signal(RegionCraft.Events.In.builder()
                        .altitudeMetres(event.getAltitudeMetres()).latitude(event.getLatitude())
                        .longitude(event.getLongitude()).time(event.getTime()).build());
            } else {
                rc.get().signal(RegionCraft.Events.Out.builder()
                        .altitudeMetres(event.getAltitudeMetres()).latitude(event.getLatitude())
                        .longitude(event.getLongitude()).time(event.getTime()).build());
            }
        } else {
            createNewRegionCraft(event, inside);
        }

    }

    private void createNewRegionCraft(Position event, boolean inside) {
        // Creation of RegionCraft that also sets its new state without
        // triggering state machine
        RegionCraft rc = RegionCraft.create(ArbitraryId.next());
        if (inside) {
            rc.setLastExitTimeFromRegion(new Date(Long.MIN_VALUE));
            rc.setLastTimeEntered(event.getTime());
            rc.setState(RegionCraft.State.INSIDE_REGION);
            self.getDetectionRule_R1()
                    .forEach(dr -> dr.signal(DetectionRule.Events.PositionInRegion.builder()
                            .altitudeMetres(event.getAltitudeMetres()).craftID(event.getCraftID())
                            .latitude(event.getLatitude()).longitude(event.getLongitude())
                            .lastTimeEntered(rc.getLastTimeEntered())
                            .lastExitTimeFromRegion(rc.getLastExitTimeFromRegion())
                            .time(event.getTime()).build()));
        } else {
            rc.setLastTimeEntered(event.getTime());
            rc.setLastExitTimeFromRegion(new Date(Long.MAX_VALUE));
            rc.setState(RegionCraft.State.OUTSIDE_REGION);
        }
        Craft craft = Craft.find(event.getCraftID()).get();
        rc.relateAcrossR5(self);
        rc.relateAcrossR15(craft);
        rc.persist();
    }

    private boolean contains(Double latitude, Double longitude) {
        if (self.getSimpleRegion_R4() != null) {
            Shapefile shapefile = Shapefiles.instance().get(self.getName(), () -> {
                byte[] bytes = self.getSimpleRegion_R4().getZippedShapefileBytes();
                return Shapefile.fromZip(new ByteArrayInputStream(bytes));
            });
            return shapefile.contains(latitude, longitude);
        } else {
            return true;
        }
    }

}