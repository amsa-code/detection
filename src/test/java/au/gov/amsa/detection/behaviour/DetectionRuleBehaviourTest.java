package au.gov.amsa.detection.behaviour;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;

public class DetectionRuleBehaviourTest {

    @Test
    public void test() {

        DetectionRule dr = Mockito.mock(DetectionRule.class);
        PositionInRegion p = DetectionRule.Events.PositionInRegion.builder()
                .hasBeenOutsideRegion(false).latitude(-10.0).longitude(135.0)
                .lastExitTimeFromRegion(new Date(0)).lastTimeEntered(new Date(0)).time(new Date(1))
                .craftID("abc").altitudeMetres(0.0).build();
        // DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, 3);
        Mockito.verifyNoMoreInteractions(dr);

    }

}
