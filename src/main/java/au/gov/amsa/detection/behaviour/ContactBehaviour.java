package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Contact.Behaviour;
import au.gov.amsa.detection.model.Contact.Events.Create;
import au.gov.amsa.detection.model.Contact.Events.Send;

public class ContactBehaviour implements Behaviour {

    private final Contact contact;
    private final ContactSender sender;

    public ContactBehaviour(Contact contact, ContactSender sender) {
        this.contact = contact;
        this.sender = sender;
    }

    @Override
    public void onEntryCreated(Create event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEntrySent(Send event) {
        // TODO Auto-generated method stub

    }

}
