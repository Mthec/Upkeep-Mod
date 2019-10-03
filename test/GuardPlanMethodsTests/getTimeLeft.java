package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import static org.junit.Assert.assertEquals;

public class getTimeLeft extends GuardPlanMethodsTest {
    private long specialValue = 29030400000L;

    private long call() {
        return gPlan.getTimeLeft();
    }

    @Test
    public void testPermanentVillage() throws Exception {
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("isPermanent"), true);
        assertEquals(specialValue, call());
    }

    @Test
    public void testNoUpkeep() throws Exception {
        setUpkeep(false);
        assertEquals(specialValue, call());
    }

    @Test
    public void testMoneyLeftOverDisbandLevel() {
        long moneyLeft = 10000L;
        gPlan.moneyLeft = moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        assertEquals(result, call());
    }

    @Test
    public void testMoneyLeftUnderDisbandLevel() {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        assertEquals(result, call());
    }

    @Test
    public void testNoMinimumCalculatedUpkeep() {
        // Original version has 1.0D minimum.  Should be removed.
        long moneyLeft = gPlan.moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        assertEquals(result, call());
    }

    @Test
    public void testCalculatedUpkeep() {
        long moneyLeft = gPlan.moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        assertEquals(result, call());
        calculatedUpkeep = gPlan.calculateUpkeep(true);
        result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        assertEquals(result, call());
    }
}
