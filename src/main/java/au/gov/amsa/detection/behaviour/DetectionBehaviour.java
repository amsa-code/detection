package au.gov.amsa.detection.behaviour;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.grumpy.core.Position;

import au.gov.amsa.detection.Streams;
import au.gov.amsa.detection.Util;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.Detection.Behaviour;
import au.gov.amsa.detection.model.Detection.Events.Create;
import au.gov.amsa.detection.model.Detection.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;

public class DetectionBehaviour implements Behaviour {

    private static Logger log = LoggerFactory.getLogger(DetectionBehaviour.class);

    private final Detection self;

    public DetectionBehaviour(Detection self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        // won't be called
        throw new RuntimeException("unexpected, creation happens in Detection Rule");
    }

    @Override
    public void onEntrySent(Send event) {
        DetectionRule dr = self.getDetectionRule_R7();
        Streams.fromNullable(dr.getMessageTemplate_R8())
                // only between time range
                .filter(template -> Util.between(event.getSendTime(), template.getStartTime(),
                        template.getEndTime()))
                .forEach(template -> {
                    // max of one template found
                    String subject = replaceParameters(template.getSubject());
                    String body = replaceParameters(template.getBody());
                    log.info("email is\n" + "{}\n" + "--------------\n" + "{}\n" + "--------------",
                            subject, body);
                    dr.getMessageRecipient_R16().stream().forEach(r -> {
                        DetectionMessage m = DetectionMessage.create(DetectionMessage.Events.Create
                                .builder().body(body).subject(subject).detectionID(self.getId())
                                .messageRecipientID(r.getId()).messageTemplateID(template.getId())
                                .sentTime(event.getSendTime()).build());
                        r.signal(MessageRecipient.Events.Send.builder()
                                .detectionMessageID(m.getId()).build());
                    });
                });
    }

    private String replaceParameters(String s) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC);

        s = replaceParameter(s, "${craft.identifier.type}",
                self.getCraft_R6().getCraftIdentifierType_R20().getName());
        s = replaceParameter(s, "${craft.identifier}", self.getCraft_R6().getIdentifier());
        s = replaceParameter(s, "${craft.type}",
                self.getCraft_R6().getCraftIdentifierType_R20().getCraftType_R3().getName());
        s = replaceParameter(s, "${position.time}",
                dtf.format(Instant.ofEpochMilli(self.getReportTime().getTime())));
        s = replaceParameter(s, "${position.lat}", self.getReportLatitude().toString());
        s = replaceParameter(s, "${position.lon}", self.getReportLongitude().toString());
        String latFormatted = StringEscapeUtils.ESCAPE_HTML4.translate(
                Position.toDegreesMinutesDecimalMinutesLatitude(self.getReportLatitude()));
        String lonFormatted = StringEscapeUtils.ESCAPE_HTML4.translate(
                Position.toDegreesMinutesDecimalMinutesLongitude(self.getReportLongitude()));
        s = replaceParameter(s, "${position.lat.formatted.html}", latFormatted);
        s = replaceParameter(s, "${position.lon.formatted.html}", lonFormatted);
        s = replaceParameter(s, "${detection.rule.name}", self.getDetectionRule_R7().getName());
        s = replaceParameter(s, "${detection.rule.description}",
                self.getDetectionRule_R7().getName());
        s = replaceParameter(s, "${region.name}",
                self.getDetectionRule_R7().getRegion_R1().getName());
        return s;
    }

    private String replaceParameter(String s, String param, String value) {
        String paramAdjusted = param.replace("$", "\\$").replace("{", "\\{").replace("}", "\\}");
        return s.replaceAll(paramAdjusted, value);
    }

}
