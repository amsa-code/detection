package au.gov.amsa.detection.behaviour;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Contact.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import scala.concurrent.duration.Duration;

public class ContactBehaviourTest {

    @Test
    public void testNoErrorInSend() {
        Send event = mock(Send.class);
        Contact contact = mock(Contact.class);
        DetectionMessage m = mock(DetectionMessage.class);
        ContactSender sender = mock(ContactSender.class);

        when(contact.getEmail()).thenReturn("fred@amsa.gov.au");
        when(contact.getEmailSubjectPrefix()).thenReturn("DEV: ");
        when(m.getSubject()).thenReturn("SUBJECT");
        when(m.getBody()).thenReturn("Hi there");

        ContactBehaviour.doOnEntrySent(event, contact, m, sender);
        Mockito.verify(contact, Mockito.never()).signal(Mockito.eq(event),
                Mockito.<Duration> anyObject());
    }

    @Test
    public void testErrorInSend() {
        Send event = mock(Send.class);
        Contact contact = mock(Contact.class);
        DetectionMessage m = mock(DetectionMessage.class);
        ContactSender sender = mock(ContactSender.class);

        when(contact.getEmail()).thenReturn("fred@amsa.gov.au");
        when(contact.getEmailSubjectPrefix()).thenReturn("DEV: ");
        when(m.getSubject()).thenReturn("SUBJECT");
        when(m.getBody()).thenReturn("Hi there");
        Mockito.doThrow(new RuntimeException()).when(sender).send(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());

        ContactBehaviour.doOnEntrySent(event, contact, m, sender);
        Mockito.verify(contact, Mockito.times(1)).signal(Mockito.eq(event),
                Mockito.<Duration> anyObject());
    }

}
