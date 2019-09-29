package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import org.junit.Assert;
import org.junit.Test;

public class getTimeLeft extends GuardPlanMethodsTest {
    private long specialValue = 29030400000L;

    private long call() throws Exception{
        return gPlan.getTimeLeft();
    }

    @Test
    public void testPermanentVillage() throws Exception {
        Village.class.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testNoUpkeep() throws Exception {
        setUpkeep(false);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testMoneyLeftOverDisbandLevel() throws Exception {
        long moneyLeft = 10000L;
        gPlan.moneyLeft = moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testMoneyLeftUnderDisbandLevel() throws Exception {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testNoMinimumCalculatedUpkeep() throws Exception {
        // Original version has 1.0D minimum.  Should be removed.
        long moneyLeft = gPlan.moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testCalculatedUpkeep() throws Exception {
        long moneyLeft = gPlan.moneyLeft;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
        calculatedUpkeep = gPlan.calculateUpkeep(true);
        result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }
}
