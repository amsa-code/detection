package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectedCraft.Behaviour;
import au.gov.amsa.detection.model.DetectedCraft.BehaviourFactory;

public class DetectedCraftBehaviourFactory implements BehaviourFactory {

    private final CraftSender sender;

    public DetectedCraftBehaviourFactory(CraftSender sender) {
        this.sender = sender;
    }

    @Override
    public Behaviour create(DetectedCraft entity) {
        return new DetectedCraftBehaviour(entity, sender);
    }

}
