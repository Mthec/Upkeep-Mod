package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import static org.junit.Assert.assertEquals;

public class getTimeLeft extends GuardPlanMethodsTest {
    private final long specialValue = 29030400000L;

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
    public void testZeroCostDeed() {
        gPlan.moneyLeft = 0;
        gPlan.monthlyCost = 0;

        assertEquals(specialValue, call());
    }

    @Test
    public void testMoneyLeftOverDisbandLevel() {
        gPlan.moneyLeft = 10000L;
        gPlan.monthlyCost = (long)(gPlan.moneyLeft * 0.9f);
        long result = (long)((double)gPlan.moneyLeft / gPlan.calculateUpkeep(true) * 500000.0D);
        assertEquals(result, call());
    }

    @Test
    public void testMoneyLeftUnderDisbandLevel() {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = (long)(moneyLeft * 1.1f);
        long result = (long)((double)moneyLeft / gPlan.calculateUpkeep(true) * 500000.0D);
        assertEquals(result, call());
    }

    @Test
    public void testNoMinimumCalculatedUpkeep() {
        // Original version has 1.0D minimum.  Should be removed.
        long moneyLeft = 2;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = 1;
        Villages.MINIMUM_UPKEEP = 0;
        long result = (long)((double)moneyLeft / gPlan.calculateUpkeep(true) * 500000.0D);
        assertEquals(result, call());
    }
}
