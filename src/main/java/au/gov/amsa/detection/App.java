package au.gov.amsa.detection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import au.gov.amsa.detection.behaviour.CompositeRegionBehaviour;
import au.gov.amsa.detection.behaviour.CompositeRegionMemberBehaviour;
import au.gov.amsa.detection.behaviour.ContactBehaviourFactory;
import au.gov.amsa.detection.behaviour.ContactSender;
import au.gov.amsa.detection.behaviour.CraftBehaviour;
import au.gov.amsa.detection.behaviour.CraftSender;
import au.gov.amsa.detection.behaviour.CraftTypeBehaviour;
import au.gov.amsa.detection.behaviour.DetectedCraftBehaviourFactory;
import au.gov.amsa.detection.behaviour.DetectionBehaviour;
import au.gov.amsa.detection.behaviour.DetectionMessageBehaviour;
import au.gov.amsa.detection.behaviour.DetectionRuleBehaviour;
import au.gov.amsa.detection.behaviour.MessageRecipientBehaviour;
import au.gov.amsa.detection.behaviour.MessageTemplateBehaviour;
import au.gov.amsa.detection.behaviour.RegionBehaviour;
import au.gov.amsa.detection.behaviour.RegionCraftBehaviour;
import au.gov.amsa.detection.behaviour.SimpleRegionBehaviour;
import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.CompositeRegionMember;
import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.RegionCraft;
import au.gov.amsa.detection.model.SimpleRegion;

public final class App {

    public static void startup(CraftSender craftSender, ContactSender contactSender) {
        // create the entity manager factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPersistenceUnit");

        // pass the EntityManagerFactory to the generated xuml Context
        Context.setEntityManagerFactory(emf);

        // setup behaviour factories and assign them to Context here

        Craft.setBehaviourFactory(CraftBehaviour.class);
        CraftType.setBehaviourFactory(CraftTypeBehaviour.class);
        DetectionRule.setBehaviourFactory(DetectionRuleBehaviour.class);
        SimpleRegion.setBehaviourFactory(SimpleRegionBehaviour.class);
        Region.setBehaviourFactory(RegionBehaviour.class);
        RegionCraft.setBehaviourFactory(RegionCraftBehaviour.class);
        Detection.setBehaviourFactory(DetectionBehaviour.class);
        MessageTemplate.setBehaviourFactory(MessageTemplateBehaviour.class);
        DetectionMessage.setBehaviourFactory(DetectionMessageBehaviour.class);
        MessageRecipient.setBehaviourFactory(MessageRecipientBehaviour.class);
        CompositeRegion.setBehaviourFactory(CompositeRegionBehaviour.class);
        CompositeRegionMember.setBehaviourFactory(CompositeRegionMemberBehaviour.class);
        DetectedCraft.setBehaviourFactory(new DetectedCraftBehaviourFactory(craftSender));
        Contact.setBehaviourFactory(new ContactBehaviourFactory(contactSender));

        // send any signals not processed from last shutdown
        Context.sendSignalsInQueue();
    }

    public static void shutdown() {
        // shutdown the actor system
        Context.stop();

        // close the entity manager factory if desired
        Context.close();

    }

}