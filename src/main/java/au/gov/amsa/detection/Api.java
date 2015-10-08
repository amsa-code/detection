package au.gov.amsa.detection;

public interface Api {

    void addDetectionRule();

    void modifyDetectionRule();

    void addCraftType();

    void modifyCraftType();

    void addSimpleRegion();

    void modifySimpleRegion();

    void addCompositeRegion();

    void modifyCompositeRegion();

    void addMessageTemplate();

    void modifyMessageTemplate();

    void addMessageRecipient();

    void modifyMessageRecipient();

    void reportPosition(String craftIdentifierTypeName, String craftIdentifier, double latitude,
            double longitude, double altitudeMetres, long time);

    long queueSize();

    void clearQueue();

}
