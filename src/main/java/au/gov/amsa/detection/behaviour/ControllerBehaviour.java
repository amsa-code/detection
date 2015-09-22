package au.gov.amsa.detection.behaviour;

import com.google.common.base.Optional;

import au.gov.amsa.detection.model.Controller;
import au.gov.amsa.detection.model.Controller.Behaviour;
import au.gov.amsa.detection.model.Controller.Events.Position;
import au.gov.amsa.detection.model.Controller.Events.StateSignature_Created;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftIdentifierType;

public class ControllerBehaviour implements Behaviour {

    private final Controller self;

    public ControllerBehaviour(Controller self) {
        this.self = self;
    }

    @Override
    public void onEntryPositionArrived(Position event) {
        final CraftIdentifierType cit = CraftIdentifierType
                .select(CraftIdentifierType.Attribute.name.eq(event.getIdentifierTypeName())).one()
                .get();

        Optional<Craft> craft = Craft.select(Craft.Attribute.craftIdentifierType_R20_id
                .eq(cit.getId()).and(Craft.Attribute.identifier.eq(event.getIdentifier()))).any();

        Craft c;
        if (!craft.isPresent()) {
            c = Craft.create(Craft.Events.Create.builder().craftIdentifier(event.getIdentifier())
                    .craftIdentifierTypeName("MMSI").build());
        } else {
            c = craft.get();
        }
        c.signal(Craft.Events.Position.builder().altitudeMetres(0.0)
                .latitude((double) event.getLatitude()).longitude((double) event.getLongitude())
                .time(event.getTime()).build());
    }

    @Override
    public void onEntryCreated(StateSignature_Created event) {
        self.setId("1");
    }

}
