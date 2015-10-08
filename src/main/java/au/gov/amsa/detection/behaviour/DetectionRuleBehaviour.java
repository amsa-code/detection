package au.gov.amsa.detection.behaviour;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.Streams;
import au.gov.amsa.detection.Util;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Behaviour;
import au.gov.amsa.detection.model.DetectionRule.Events.Create;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.DetectionRuleCraft;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.Region;

public class DetectionRuleBehaviour implements Behaviour {

    private final DetectionRule self;

    public DetectionRuleBehaviour(DetectionRule self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setName(event.getName());
        self.setDescription(event.getDescription());
        self.setStartTime(event.getStartTime());
        self.setEndTime(event.getEndTime());
        self.setMinIntervalSecs(event.getMinIntervalSecs());
        self.setMinIntervalSecsOut(event.getMinIntervalSecsOut());
        self.setMustCross(event.getMustCross());
        self.setCraftIdentifierPattern(event.getCraftIdentifierPattern());
        self.setRegion_R1(Region.find(event.getRegionID()).get());
    }

    @Override
    public void onEntryPositionInRegion(PositionInRegion event) {

        // create a detection only if certain criteria satisfied
        if (shouldCreateDetection(event)) {
            Detection detection = createNewDetection(event);
            Optional<DetectionRuleCraft> drc = DetectionRuleCraft
                    .select(DetectionRuleCraft.Attribute.craft_R18_id.eq(event.getCraftID()).and(
                            DetectionRuleCraft.Attribute.detectionRule_R17_id.eq(self.getId())))
                    .any();
            if (drc.isPresent()) {
                drc.get().relateAcrossR12(detection);
            } else {
                createDetectionRuleCraft(detection, event.getCraftID());
            }

            // because detection is not self this signal will run in a distinct
            // transaction after this method has had its transaction committed
            detection.signal(
                    Detection.Events.Send.builder().sendTime(detection.getCreatedTime()).build());
        }

    }

    private boolean shouldCreateDetection(PositionInRegion event) {

        // use a lambda for this bit so we can mock it out for unit testing
        Predicate<PositionInRegion> matchesCraftIdentifierPattern = p -> {
            Craft craft = Craft.find(p.getCraftID()).get();
            String craftIdentifier = craft.getCraftIdentifierType_R20().getName() + "="
                    + craft.getIdentifier();
            return Pattern.matches(self.getCraftIdentifierPattern(), craftIdentifier);
        };

        return shouldCreateDetection(self, event, matchesCraftIdentifierPattern,
                (craftId, detectionRuleId) -> DetectionRuleCraft
                        .select(DetectionRuleCraft.Attribute.craft_R18_id.eq(craftId)
                                .and(DetectionRuleCraft.Attribute.detectionRule_R17_id
                                        .eq(detectionRuleId)))
                        .any());
    }

    static interface DetectionRuleCraftProvider {
        Optional<DetectionRuleCraft> find(String craftId, String detectionRuleId);
    }

    @VisibleForTesting
    static boolean shouldCreateDetection(DetectionRule self, PositionInRegion event,
            Predicate<PositionInRegion> matchesCraftIdentifierPattern,
            DetectionRuleCraftProvider drcProvider) {
        final boolean createDetection;
        if (self.getMustCross() && !event.getHasBeenOutsideRegion()) {
            createDetection = false;
        } else if (!matchesCraftIdentifierPattern.test(event))
            createDetection = false;
        else if (Util.between(event.getTime(), self.getStartTime(), self.getEndTime())) {
            Optional<DetectionRuleCraft> drc = drcProvider.find(event.getCraftID(), self.getId());
            final Optional<Detection> latestDetection;
            if (drc.isPresent()) {
                latestDetection = Optional.of(drc.get().getDetection_R12());
            } else {
                latestDetection = Optional.absent();
            }
            if (!latestDetection.isPresent()) {
                createDetection = true;
            } else if (Util.beforeOrEquals(event.getTime(),
                    latestDetection.get().getReportTime())) {
                // ignore if position time before latest detection position time
                createDetection = false;
            } else if (forcedUpdateRequired(self, event.getCurrentTime(), latestDetection.get())) {
                createDetection = true;
            } else if (event.getCurrentTime().getTime()
                    - latestDetection.get().getCreatedTime().getTime() >= TimeUnit.SECONDS
                            .toMillis(self.getMinIntervalSecs())) {
                createDetection = true;
            } else
            // if been outside for a while and
            if (event.getIsEntrance() && timeOutside(event) >= TimeUnit.SECONDS
                    .toMillis(self.getMinIntervalSecsOut())) {
                createDetection = true;
            } else
                createDetection = false;
        } else
            createDetection = false;

        return createDetection;

    }

    private static long timeOutside(PositionInRegion event) {
        return event.getLastTimeEntered().getTime() - event.getLastExitTimeFromRegion().getTime();
    }

    private static boolean forcedUpdateRequired(DetectionRule self, Date detectionCreateTime,
            Detection latestDetection) {
        Stream<MessageTemplate> templates = getTemplate(self, detectionCreateTime);
        return templates
                .filter(t -> latestDetection.getCreatedTime().before(t.getForceUpdateBeforeTime()))
                .findAny().isPresent();
    }

    private Detection createNewDetection(PositionInRegion event) {
        Detection detection = Detection.create(ArbitraryId.next());
        Craft craft = Craft.find(event.getCraftID()).get();
        detection.relateAcrossR6(craft);
        detection.setReportAltitudeMetres(event.getAltitudeMetres());
        detection.setReportLatitude(event.getLatitude());
        detection.setReportLongitude(event.getLongitude());
        detection.setReportTime(event.getTime());
        detection.setCreatedTime(event.getCurrentTime());
        detection.setDetectionRule_R7(self);
        detection.setState(Detection.State.CREATED);
        detection.persist();
        return detection;
    }

    private void createDetectionRuleCraft(Detection detection, String craftId) {
        DetectionRuleCraft drc = DetectionRuleCraft.create(ArbitraryId.next());
        drc.setDetectionRule_R17(self);
        drc.setDetection_R12(detection);
        drc.setCraft_R18(Craft.find(craftId).get());
        drc.setState(DetectionRuleCraft.State.CREATED);
        drc.persist();
    }

    private static Stream<MessageTemplate> getTemplate(DetectionRule self,
            Date detectionCreateTime) {
        return Streams.fromNullable(self.getMessageTemplate_R8())
                // only between time range
                .filter(template -> Util.between(detectionCreateTime, template.getStartTime(),
                        template.getEndTime()));
    }

}
