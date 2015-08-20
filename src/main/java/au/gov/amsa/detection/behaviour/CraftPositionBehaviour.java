package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftPosition;
import au.gov.amsa.detection.model.CraftPosition.Behaviour;
import au.gov.amsa.detection.model.CraftPosition.Events.Create;

public class CraftPositionBehaviour implements Behaviour {

    private final CraftPosition self;

    public CraftPositionBehaviour(CraftPosition self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setTimestamp(event.getTime());
        self.setLatitude(event.getLatitude());
        self.setLongitude(event.getLongitude());
        self.setAltitudeMetres(event.getAltitudeMetres());
        Craft craft = Craft.select(Craft.Attribute.mmsi.eq(event.getMmsi())).one();
        self.setCraft_R2(craft);
    }

}
