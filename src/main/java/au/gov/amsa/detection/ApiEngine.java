package au.gov.amsa.detection;

import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.base.Optional;

import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftIdentifierType;

public class ApiEngine implements Api {

    @Override
    public void addCompositeRegion() {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifyCompositeRegion() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMessageTemplate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifyMessageTemplate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMessageRecipient() {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifyMessageRecipient() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reportPosition(String craftIdentifierTypeName, String craftIdentifier,
            double latitude, double longitude, double altitudeMetres, long time) {
        Optional<Craft> craft = getCraft(craftIdentifierTypeName, craftIdentifier);
        Craft c;
        if (!craft.isPresent()) {
            try {
                c = Craft.create(Craft.Events.Create.builder().craftIdentifier(craftIdentifier)
                        .craftIdentifierTypeName("MMSI").build());
            } catch (RuntimeException e) {
                // if already exists then loading it again should not fail
                c = getCraft(craftIdentifierTypeName, craftIdentifier).get();
            }
        } else {
            c = craft.get();
        }
        c.signal(Craft.Events.Position.builder().altitudeMetres(0.0).latitude(latitude)
                .longitude(longitude).time(new Date(time)).currentTime(new Date(Clock.now()))
                .build());

    }

    private Optional<Craft> getCraft(String craftIdentifierTypeName, String craftIdentifier) {
        EntityManager em = Context.createEntityManager();
        try {
            CraftIdentifierType cit = CraftIdentifierType
                    .select(CraftIdentifierType.Attribute.name.eq(craftIdentifierTypeName)).one(em)
                    .get();
            return Craft.select(Craft.Attribute.craftIdentifierType_R20_id.eq(cit.getId())
                    .and(Craft.Attribute.identifier.eq(craftIdentifier))).any(em);
        } finally {
            em.close();
        }
    }

    @Override
    public long queueSize() {
        return Context.queueSize();
    }

    @Override
    public void clearQueue() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void createDetectionRule(String name, String description, long startTime, long endTime,
            int minIntervalSeconds, int minIntervalSecondsOutsideRegion, boolean mustCross,
            String craftIdentifierPattern, String regionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDetectionRule(String name, String description, long startTime, long endTime,
            int minIntervalSeconds, int minIntervalSecondsOutsideRegion, boolean mustCross,
            String craftIdentifierPattern, String regionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSimpleRegion(String name, String description, String simpleRegionType,
            byte[] bytes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifySimpleRegion(String name, String description, String simpleRegionType,
            byte[] bytes) {
        // TODO Auto-generated method stub

    }

}
