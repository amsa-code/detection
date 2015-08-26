package au.gov.amsa.detection.behaviour;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.common.base.Optional;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.Streams;
import au.gov.amsa.detection.Util;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Behaviour;
import au.gov.amsa.detection.model.DetectionRule.Events.Create;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.Region;

public class DetectionRuleBehaviour implements Behaviour {

    private static final long RESEND_INTERVAL_MS = TimeUnit.DAYS.toMillis(30);
    private static final long BEEN_OUTSIDE_RESEND_INTERVAL_MS = TimeUnit.DAYS.toMillis(7);

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
        self.setRegion_R1(Region.find(event.getRegionID()).get());
    }

    @Override
    public void onEntryPositionInRegion(PositionInRegion event) {

        // create a detection only if certain criteria satisfied

        final boolean createDetection;
        if ((event.getTime().after(self.getStartTime()) || event.getTime().equals(event.getTime()))
                && event.getTime().before(self.getEndTime())) {

            long now = System.currentTimeMillis();
            Optional<Detection> latestDetection = Optional.fromNullable(self.getDetection_R18());
            // Optional<Detection> latestDetection = Optional.absent();

            if (!latestDetection.isPresent()) {
                createDetection = true;
            } else
                if (Util.beforeOrEquals(event.getTime(), latestDetection.get().getReportTime())) {
                // ignore if position time before latest detection position
                // time
                createDetection = false;
            } else {
                Stream<MessageTemplate> templates = getTemplates(new Date(now));
                boolean forcedUpdateRequired = templates.filter(t -> latestDetection.get()
                        .getCreatedTime().before(t.getForceUpdateBeforeTime())).findAny()
                        .isPresent();
                if (forcedUpdateRequired) {
                    createDetection = true;
                } else if (event.getTime().getTime()
                        - latestDetection.get().getCreatedTime().getTime() >= RESEND_INTERVAL_MS) {
                    createDetection = true;
                } else if (event.getLastTimeEntered().getTime()
                        - event.getLastExitTimeFromRegion()
                                .getTime() >= BEEN_OUTSIDE_RESEND_INTERVAL_MS
                        && event.getTime().getTime() - latestDetection.get().getCreatedTime()
                                .getTime() >= BEEN_OUTSIDE_RESEND_INTERVAL_MS) {
                    createDetection = true;
                } else
                    createDetection = false;
            }
        } else
            createDetection = false;
        if (createDetection) {
            Detection detection = Detection.create(ArbitraryId.next());
            Craft craft = Craft.find(event.getCraftID()).get();
            detection.setCraft_R6(craft);
            detection.setReportAltitudeMetres(event.getAltitudeMetres());
            detection.setReportLatitude(event.getLatitude());
            detection.setReportLongitude(event.getLongitude());
            detection.setReportTime(event.getTime());
            detection.setCreatedTime(new Date());
            // establish latest detection for rule
            detection.setDetectionRule_R18(self);
            self.setDetection_R18(detection);
            detection.relateAcrossR7(self);
            detection.setState(Detection.State.CREATED);
            detection.persist(Context.em());

            // because detection is not self this signal will run in a distinct
            // transaction after this method has had its transaction committed
            detection.signal(
                    Detection.Events.Send.builder().sendTime(detection.getCreatedTime()).build());
        }

    }

    private Stream<MessageTemplate> getTemplates(Date now) {
        return Streams.fromNullable(self.getMessageTemplate_R8())
                // only between time range
                .filter(template -> Util.between(now, template.getStartTime(),
                        template.getEndTime()));
    }

}
