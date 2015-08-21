package au.gov.amsa.detection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import au.gov.amsa.detection.behaviour.CraftBehaviour;
import au.gov.amsa.detection.behaviour.CraftTypeBehaviour;
import au.gov.amsa.detection.behaviour.DetectionRuleBehaviour;
import au.gov.amsa.detection.behaviour.RegionBehaviour;
import au.gov.amsa.detection.behaviour.SimpleRegionBehaviour;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.SimpleRegion;

public class App {

    public static void startup() {
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