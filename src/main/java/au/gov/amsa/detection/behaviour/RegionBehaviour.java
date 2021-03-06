package au.gov.amsa.detection.behaviour;

import java.io.ByteArrayInputStream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import au.gov.amsa.detection.Dates;
import au.gov.amsa.detection.Shapefiles;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.Region.Behaviour;
import au.gov.amsa.detection.model.Region.Events.Position;
import au.gov.amsa.detection.model.Region.Events.StateSignature_Created;
import au.gov.amsa.detection.model.RegionCraft;
import au.gov.amsa.detection.model.SimpleRegion;
import au.gov.amsa.gt.Shapefile;
import xuml.tools.model.compiler.runtime.ArbitraryId;

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
        boolean inside = contains(self, event.getLatitude(), event.getLongitude());
        if (rc.isPresent()) {
            if (inside) {
                rc.get().signal(
                        RegionCraft.Events.In.builder().altitudeMetres(event.getAltitudeMetres())
                                .latitude(event.getLatitude()).longitude(event.getLongitude())
                                .time(event.getTime()).currentTime(event.getCurrentTime()).build());
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
            rc.setLastExitTimeFromRegion(Dates.MIN);
            rc.setLastTimeEntered(event.getTime());
            rc.setState(RegionCraft.State.NEVER_OUTSIDE);
            self.getDetectionRule_R1()
                    .forEach(dr -> dr.signal(DetectionRule.Events.PositionInRegion.builder()
                            .altitudeMetres(event.getAltitudeMetres()).craftID(event.getCraftID())
                            .latitude(event.getLatitude()).longitude(event.getLongitude())
                            .lastTimeEntered(rc.getLastTimeEntered())
                            .lastExitTimeFromRegion(rc.getLastExitTimeFromRegion())
                            .currentTime(event.getCurrentTime()).hasBeenOutsideRegion(false)
                            .isEntrance(false).time(event.getTime()).build()));
        } else {
            rc.setLastTimeEntered(event.getTime());
            rc.setLastExitTimeFromRegion(Dates.MAX);
            rc.setState(RegionCraft.State.OUTSIDE);
        }
        Craft craft = Craft.find(event.getCraftID()).get();
        rc.setRegion_R5(self);
        rc.relateAcrossR15(craft);
        rc.persist();
    }

    @VisibleForTesting
    public static boolean contains(Region region, Double lat, Double lon) {
        if (region.getSimpleRegion_R4() != null) {
            return simpleRegionContains(region.getSimpleRegion_R4(), lat, lon);
        } else {
            return compositeRegionContains(region, lat, lon);
        }
    }

    private static boolean simpleRegionContains(SimpleRegion region, Double lat, Double lon) {
        // get shapefile from cache
        Shapefile shapefile = Shapefiles.instance().get(region.getRegion_R4().getName(), () -> {
            System.out.println("loading shapefile");
            byte[] bytes = region.getBinary_R21().getBytes();
            return Shapefile.fromZip(new ByteArrayInputStream(bytes));
        });
        return shapefile.contains(lat, lon);
    }

    private static boolean compositeRegionContains(Region region, Double lat, Double lon) {
        // region is CompositeRegion
        return region.getCompositeRegion_R4().getCompositeRegionMember_R10().stream()
                .allMatch(crm -> {
                    if (crm.getInclude()) {
                        return contains(crm.getRegion_R9(), lat, lon);
                    } else {
                        return !contains(crm.getRegion_R9(), lat, lon);
                    }
                });
    }

}