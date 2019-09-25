package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import org.junit.Assert;
import org.junit.Test;

public class getTimeLeft extends GuardPlanMethodsTest {
    private long specialValue = 29030400000L;

    private long call() throws Exception{
        return (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan);
    }

    @Test
    public void testPermanentVillage() throws Exception {
        Village.class.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testNoUpkeep() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testMoneyLeftOverDisbandLevel() throws Exception {
        long moneyLeft = 10000L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testMoneyLeftUnderDisbandLevel() throws Exception {
        long moneyLeft = 10L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testNoMinimumCalculatedUpkeep() throws Exception {
        // Original version has 1.0D minimum.  Should be removed.
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testCalculatedUpkeep() throws Exception {
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
        calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }
}
