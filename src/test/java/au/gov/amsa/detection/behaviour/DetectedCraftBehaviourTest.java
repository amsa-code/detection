package au.gov.amsa.detection.behaviour;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftIdentifierType;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectedCraft.Events.Send;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionMessage;
import scala.concurrent.duration.Duration;

public class DetectedCraftBehaviourTest {

    @Test
    public void testNoErrorInCraftSend() {
        Send event = Mockito.mock(DetectedCraft.Events.Send.class);
        when(event.getDetectionMessageID()).thenReturn("1");

        DetectedCraft detectedCraft = Mockito.mock(DetectedCraft.class);
        CraftSender craftSender = Mockito.mock(CraftSender.class);

        DetectionMessage m = mock(DetectionMessage.class);
        @SuppressWarnings("unchecked")
        Function<String, DetectionMessage> detectionMessageFinder = Mockito.mock(Function.class);
        when(detectionMessageFinder.apply("1")).thenReturn(m);

        Detection d = mock(Detection.class);
        when(m.getDetection_R11()).thenReturn(d);
        Craft c = mock(Craft.class);
        when(d.getCraft_R6()).thenReturn(c);
        CraftIdentifierType cit = mock(CraftIdentifierType.class);
        when(cit.getName()).thenReturn("Vessel");
        when(c.getCraftIdentifierType_R20()).thenReturn(cit);
        when(c.getIdentifier()).thenReturn("123456789");
        when(m.getSubject()).thenReturn("SUBJECT");
        when(m.getBody()).thenReturn("BODY");

        DetectedCraftBehaviour.doOnEntrySent(event, detectedCraft, craftSender,
                detectionMessageFinder);

        Mockito.verify(craftSender, Mockito.times(1)).send(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());

        Mockito.verifyNoMoreInteractions(detectedCraft);
    }

    private static class MyException extends RuntimeException {
        private static final long serialVersionUID = -2529217170558540363L;
    }

    @Test
    public void testErrorInCraftSend() {
        Send event = Mockito.mock(DetectedCraft.Events.Send.class);
        when(event.getDetectionMessageID()).thenReturn("1");

        DetectedCraft detectedCraft = Mockito.mock(DetectedCraft.class);
        CraftSender craftSender = Mockito.mock(CraftSender.class);

        DetectionMessage m = mock(DetectionMessage.class);
        @SuppressWarnings("unchecked")
        Function<String, DetectionMessage> detectionMessageFinder = Mockito.mock(Function.class);
        when(detectionMessageFinder.apply("1")).thenReturn(m);

        Detection d = mock(Detection.class);
        when(m.getDetection_R11()).thenReturn(d);
        Craft c = mock(Craft.class);
        when(d.getCraft_R6()).thenReturn(c);
        CraftIdentifierType cit = mock(CraftIdentifierType.class);
        when(cit.getName()).thenReturn("Vessel");
        when(c.getCraftIdentifierType_R20()).thenReturn(cit);
        when(c.getIdentifier()).thenReturn("123456789");
        when(m.getSubject()).thenReturn("SUBJECT");
        when(m.getBody()).thenReturn("BODY");

        doThrow(new MyException()).when(craftSender).send(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());

        DetectedCraftBehaviour.doOnEntrySent(event, detectedCraft, craftSender,
                detectionMessageFinder);

        Mockito.verify(detectedCraft, Mockito.times(1)).signal(Mockito.eq(event),
                Mockito.<Duration> anyObject());
    }

}
