package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Contact.Behaviour;
import au.gov.amsa.detection.model.Contact.BehaviourFactory;

public class ContactBehaviourFactory implements BehaviourFactory {

    private final ContactSender sender;

    public ContactBehaviourFactory(ContactSender sender) {
        this.sender = sender;
    }

    @Override
    public Behaviour create(Contact entity) {
        return new ContactBehaviour(entity, sender);
    }

}
