package au.gov.amsa.detection;

public interface Api {

    void createDetectionRule(String name, String description, long startTime, long endTime,
            int minIntervalSeconds, int minIntervalSecondsOutsideRegion, boolean mustCross,
            String craftIdentifierPattern, String regionId);

    void updateDetectionRule(String name, String description, long startTime, long endTime,
            int minIntervalSeconds, int minIntervalSecondsOutsideRegion, boolean mustCross,
            String craftIdentifierPattern, String regionId);

    void addSimpleRegion(String name, String description, String simpleRegionType, byte[] bytes);

    void modifySimpleRegion(String name, String description, String simpleRegionType, byte[] bytes);

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
