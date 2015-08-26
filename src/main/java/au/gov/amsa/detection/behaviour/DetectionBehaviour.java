package au.gov.amsa.detection.behaviour;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringEscapeUtils;

import com.github.davidmoten.grumpy.core.Position;

import au.gov.amsa.detection.Streams;
import au.gov.amsa.detection.Util;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.Detection.Behaviour;
import au.gov.amsa.detection.model.Detection.Events.Create;
import au.gov.amsa.detection.model.Detection.Events.Send;
import au.gov.amsa.detection.model.DetectionRule;

public class DetectionBehaviour implements Behaviour {

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
                    String subject = replaceParameters(template.getSubject());
                    String body = replaceParameters(template.getBody());
                });

        // message template might use these parameters:
        // ${craft.identifier.type}
        // ${craft.identifier}
        // ${craft.type}
        // ${craft.name}
        // ${ais.type}
        // ${craft.length.metres}
        // ${craft.width.metres}
        // ${position.time}
        // ${position.lat}
        // ${position.lon}
        // ${position.latlon.formatted}
        // ${detection.rule.name}
        // ${detection.rule.description}
        // ${region.name}

    }

    private String replaceParameters(String s) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC);

        s = replaceParameter(s, "${craft.identifier.type}", self.getCraft_R6().getIdentifierType());
        s = replaceParameter(s, "${craft.identifier}", self.getCraft_R6().getIdentifier());
        s = replaceParameter(s, "${craft.type}", self.getCraft_R6().getCraftType_R3().getName());
        s = replaceParameter(s, "${position.time}",
                dtf.format(Instant.ofEpochMilli(self.getReportTime().getTime())));
        s = replaceParameter(s, "${position.lat}", self.getReportLatitude().toString());
        s = replaceParameter(s, "${position.lon}", self.getReportLongitude().toString());
        // TODO
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
        System.out.println(
                "replacing in '" + s + "', param='" + paramAdjusted + "', value='" + value + "'");
        return s.replaceAll(paramAdjusted, value);
    }

}
