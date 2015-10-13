package au.gov.amsa.detection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

import akka.actor.ActorSystem;
import akka.actor.Props;
import xuml.tools.model.compiler.runtime.actor.EntityActor;

public class UtilTest {

    @Test
    public void testBeforeOrEqualsWhenEqual() {
        assertTrue(Util.beforeOrEquals(new Date(0), new Date(0)));
    }

    @Test
    public void testBeforeOrEqualsWhenBefore() {
        assertTrue(Util.beforeOrEquals(new Date(0), new Date(1)));
    }

    @Test
    public void testBeforeOrEqualsWhenAfter() {
        assertFalse(Util.beforeOrEquals(new Date(1), new Date(0)));
    }

    @Test
    public void testIsBetweenLowerLimitIsInclusive() {
        assertTrue(Util.between(new Date(0), new Date(0), new Date(2)));
    }

    @Test
    public void testIsBetweenUpperLimitIsExclusive() {
        assertFalse(Util.between(new Date(2), new Date(0), new Date(2)));
    }

    @Test
    public void testIsBetween() {
        assertTrue(Util.between(new Date(1), new Date(0), new Date(2)));
    }

    @Test
    public void testNotBetweenBefore() {
        assertFalse(Util.between(new Date(0), new Date(1), new Date(2)));
    }

    @Test
    public void testNotBetweenAfter() {
        assertFalse(Util.between(new Date(3), new Date(1), new Date(2)));
    }

    @Test
    public void testConstructorIsPrivate() {
        Asserts.assertIsUtilityClass(Util.class);
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();
        system.actorOf(Props.create(EntityActor.class).withDispatcher("akka.entity-dispatcher"));
    }

}
