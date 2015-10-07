package au.gov.amsa.detection.behaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;

import com.google.common.base.Optional;

import au.gov.amsa.detection.behaviour.DetectionRuleBehaviour.DetectionRuleCraftProvider;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.DetectionRuleCraft;
import au.gov.amsa.detection.model.MessageTemplate;

public class DetectionRuleBehaviourTest {

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRangeAndIsExclusiveOnUpperLimitOfTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(10000));

        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, null));
    }

    @Test
    public void testDoesNotCreateDetectionIfTimeOutsideTimeRange() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(9000));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, null));
    }

    @Test
    public void testDoesNotCreateDetectionIfMustCrossAndHasNotBeenOutside() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, null));
    }

    @Test
    public void testDoesNotCreateDetectionIfDoesNotMatchPattern() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> false, null));
    }

    @Test
    public void testCreateDetectionIfNoPreviousDetection() {
        DetectionRule dr = mock(DetectionRule.class);
        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true,
                (cId, drId) -> Optional.absent()));
    }

    @Test
    public void testDoNotCreateDetectionIfLastDetectionPresentButPositionTimeIsBeforeLastDetectionReportTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        Detection d = mock(Detection.class);
        // last detection report time is after position time for p
        when(d.getReportTime()).thenReturn(new Date(16000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testDoNotCreateDetectionIfLastDetectionPresentButPositionTimeIsEqualsLastDetectionReportTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent().build();
        when(dr.getMustCross()).thenReturn(Boolean.FALSE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        Detection d = mock(Detection.class);
        // last detection report time is same as position time for p
        when(d.getReportTime()).thenReturn(new Date(10000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    private static DetectionRuleCraftProvider drc(Detection d) {
        DetectionRuleCraft drc = mock(DetectionRuleCraft.class);
        when(drc.getDetection_R12()).thenReturn(d);
        return (craftId, detectionRuleId) -> Optional.of(drc);
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeBeforeForceUpdateTime() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(8000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeAfterForceUpdateTimeAndIntervalSinceLastDetectionGreaterThanMinInterval() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).currentTime(new Date(9000))
                .build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        when(dr.getMinIntervalSecs()).thenReturn(3);
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(4000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeAfterForceUpdateTimeAndIntervalSinceLastDetectionEqualToMinInterval() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).currentTime(new Date(10000))
                .build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        when(dr.getMinIntervalSecs()).thenReturn(4);
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(4000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeAfterForceUpdateTimeAndIntervalSinceLastDetectionLessThanMinInterval() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        // big interval
        when(dr.getMinIntervalSecs()).thenReturn(30);
        when(dr.getMinIntervalSecsOut()).thenReturn(30);

        // last detection
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(4000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeAfterForceUpdateTimeAndIntervalSinceLastDetectionGreaterThanMinIntervalButIsEntranceAndBeenOutsideLongEnough() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).currentTime(new Date(9000))
                .lastExitTimeFromRegion(new Date(0)).lastTimeEntered(new Date(9000))
                .isEntrance(true).build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        when(dr.getMinIntervalSecs()).thenReturn(30);
        when(dr.getMinIntervalSecsOut()).thenReturn(1);
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(4000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertTrue(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    @Test
    public void testCreateDetectionIfLastDetectionCreationTimeAfterForceUpdateTimeAndIntervalSinceLastDetectionGreaterThanMinIntervalButIsEntranceAndNotBeenOutsideLongEnough() {
        DetectionRule dr = mock(DetectionRule.class);

        PositionInRegion p = createPositionInRegionEvent(6000, true).currentTime(new Date(9000))
                .lastExitTimeFromRegion(new Date(0)).lastTimeEntered(new Date(1000))
                .isEntrance(true).build();
        when(dr.getMustCross()).thenReturn(Boolean.TRUE);
        when(dr.getStartTime()).thenReturn(new Date(0));
        when(dr.getEndTime()).thenReturn(new Date(20000));
        when(dr.getMinIntervalSecs()).thenReturn(30);
        when(dr.getMinIntervalSecsOut()).thenReturn(2);
        Detection d = mock(Detection.class);
        // last detection report time is before position time for p
        when(d.getReportTime()).thenReturn(new Date(5000));
        when(d.getCreatedTime()).thenReturn(new Date(6000));
        MessageTemplate template = mock(MessageTemplate.class);
        when(dr.getMessageTemplate_R8()).thenReturn(template);
        when(template.getStartTime()).thenReturn(new Date(0));
        when(template.getEndTime()).thenReturn(new Date(20000));
        when(template.getForceUpdateBeforeTime()).thenReturn(new Date(4000));
        DetectionRuleCraftProvider drcProvider = drc(d);
        assertFalse(DetectionRuleBehaviour.shouldCreateDetection(dr, p, x -> true, drcProvider));
    }

    private PositionInRegion.Builder createPositionInRegionEventBuilder() {
        return DetectionRule.Events.PositionInRegion.builder().hasBeenOutsideRegion(false)
                .latitude(-10.0).longitude(135.0).lastExitTimeFromRegion(new Date(0))
                .lastTimeEntered(new Date(0)).time(new Date(10000)).craftID("abc").isEntrance(false)
                .altitudeMetres(0.0).currentTime(new Date(15000));
    }

    private PositionInRegion.Builder createPositionInRegionEvent(long currentTime,
            boolean hasBeenOutsideRegion) {
        return createPositionInRegionEventBuilder().currentTime(new Date(currentTime))
                .hasBeenOutsideRegion(hasBeenOutsideRegion);
    }

    private PositionInRegion.Builder createPositionInRegionEvent() {
        return createPositionInRegionEvent(15000, false);
    }

}
