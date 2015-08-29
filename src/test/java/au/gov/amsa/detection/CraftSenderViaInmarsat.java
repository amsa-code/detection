package au.gov.amsa.detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.behaviour.CraftSender;

public class CraftSenderViaInmarsat implements CraftSender {

    private static final Logger log = LoggerFactory.getLogger(CraftSenderViaInmarsat.class);

    @Override
    public void send(String craftIdentifierType, String craftIdentifier, String subject,
            String body) {
        log.info("sending email to {}={}", craftIdentifierType, craftIdentifier);
        log.info(subject);
        log.info("-------------------");
        log.info(body);
        log.info("-------------------");
    }

}
