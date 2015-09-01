package au.gov.amsa.detection.behaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;

import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.MessageTemplate;

public class DetectionRuleBehaviourTest {

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRangeAndIsExclusiveOnUpperLimitOfTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(10));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(9));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testDoesNotCreateDetectionIfMustCrossAndHasNotBeenOutside() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testDoesNotCreateDetectionIfDoesNotMatchPattern() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> false));
    }

    @Test
    public void testCreateDetectionIfNoPreviousDetection() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testDoNotCreateDetectionIfLastDetectionPresentButPositionTimeIsBeforeLastDetectionReportTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        Detection d = mock(Detection.class);
        when(dr.getDetection_R18()).thenReturn(d);
        // last detection report time is after position time for p
        when(d.getReportTime()).thenReturn(new Date(16));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testDoNotCreateDetectionIfLastDetectionPresentButPositionTimeIsEqualsLastDetectionReportTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        Detection d = mock(Detection.class);
        when(dr.getDetection_R18()).thenReturn(d);
        // last detection report time is same as position time for p
        when(d.getReportTime()).thenReturn(new Date(10));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeBeforeForceUpdateTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6);
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20));
        Detection d = mock(Detection.class);
        when(dr.getDetection_R18()).thenReturn(d);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5));
        when(d.getCreatedTime()).thenReturn(new Date(6));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(8));

        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true));
    }

    private PositionInRegion createPositionInRegionEvent(long currentTime) {
        return DetectionRule.Events.PositionInRegion.builder().hasBeenOutsideRegion(false)
                .latitude(-10.0).longitude(135.0).lastExitTimeFromRegion(new Date(0))
                .lastTimeEntered(new Date(0)).time(new Date(10)).craftID("abc").altitudeMetres(0.0)
                .currentTime(new Date(currentTime)).build();
    }

    private PositionInRegion createPositionInRegionEvent() {
        return createPositionInRegionEvent(15);
    }

}
