package au.gov.amsa.detection.behaviour;

public interface CraftSender {

    void send(String craftIdentifierType, String craftIdentifier, String subject, String body);

}
