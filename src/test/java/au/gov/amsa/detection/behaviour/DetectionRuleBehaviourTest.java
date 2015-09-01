package au.gov.amsa.detection.behaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;

import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;

public class DetectionRuleBehaviourTest {

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRangeAndIsExclusiveOnUpperLimitOfTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(10));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, 3));
    }

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(9));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, 3));
    }

    @Test
    public void testDoesNotCreateDetectionIfMustCrossAndHasNotBeenOutside() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, 3));
    }

    @Test
    public void testDoesNotCreateDetectionIfDoesNotMatchPattern() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> false, 3));
    }

    @Test
    public void testCreateDetectionIfNoPreviousDetection() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, 3));
    }

    private PositionInRegion createPositionInRegionEvent() {
        return DetectionRule.Events.PositionInRegion.builder().hasBeenOutsideRegion(false)
                .latitude(-10.0).longitude(135.0).lastExitTimeFromRegion(new Date(0))
                .lastTimeEntered(new Date(0)).time(new Date(10)).craftID("abc").altitudeMetres(0.0)
                .build();
    }

}
