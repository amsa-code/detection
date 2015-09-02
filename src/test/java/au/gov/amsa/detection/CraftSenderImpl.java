package au.gov.amsa.detection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.behaviour.CraftSender;

class CraftSenderImpl implements CraftSender {

    private static final Logger log = LoggerFactory.getLogger(CraftSenderImpl.class);

    final class Send {
        final String craftIdentifierType;
        final String craftIdentifier;
        final String subject;
        final String body;

        Send(String craftIdentifierType, String craftIdentifier, String subject, String body) {
            this.craftIdentifierType = craftIdentifierType;
            this.craftIdentifier = craftIdentifier;
            this.subject = subject;
            this.body = body;
        }

    }

    final List<Send> list = new CopyOnWriteArrayList<>();

    @Override
    public void send(String craftIdentifierType, String craftIdentifier, String subject,
            String body) {
        log.info("sending email to {}={}", craftIdentifierType, craftIdentifier);
        log.info(subject);
        log.info("-------------------");
        log.info(body);
        log.info("-------------------");
        list.add(new Send(craftIdentifierType, craftIdentifier, subject, body));
    }

}
