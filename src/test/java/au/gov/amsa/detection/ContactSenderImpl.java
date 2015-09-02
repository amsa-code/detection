package au.gov.amsa.detection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.behaviour.ContactSender;

class ContactSenderImpl implements ContactSender {

    private static final Logger log = LoggerFactory.getLogger(ContactSenderImpl.class);

    final List<Send> list = new CopyOnWriteArrayList<>();

    final class Send {
        final String email;
        final String subject;
        final String body;

        Send(String email, String subject, String body) {
            this.email = email;
            this.subject = subject;
            this.body = body;
        }

    }

    @Override
    public void send(String email, String subject, String body) {
        log.info("sending email to {}", email);
        log.info(subject);
        log.info("-------------------");
        log.info(body);
        log.info("-------------------");
        list.add(new Send(email, subject, body));
    }

}
