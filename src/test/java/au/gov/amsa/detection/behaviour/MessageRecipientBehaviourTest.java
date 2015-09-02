package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.MessageRecipient;

public class MessageRecipientBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        MessageRecipient m = Mockito.mock(MessageRecipient.class);
        new MessageRecipientBehaviour(m).onEntryCreated(null);
    }

}
