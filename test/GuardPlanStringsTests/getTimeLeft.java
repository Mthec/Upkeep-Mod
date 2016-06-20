package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

// TODO - calculatedUpkeep not right.
public class getTimeLeft extends GuardPlanStringsTest {
    private long specialValue = 29030400000L;

    public getTimeLeft() {
        insertAftersToTest.put("getTimeLeft", GuardPlanStrings.getTimeLeft);
    }

    private long call() throws Exception{
        return (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan);
    }

    @Test
    public void testPermanentVillage() throws Exception {
        Village.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testNoUpkeep() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        Assert.assertEquals(specialValue, call());
    }

    @Test
    public void testMoneyLeftOver() throws Exception {
        long moneyLeft = 10000L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double calculatedUpkeep = 100L;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testMoneyLeftUnder() throws Exception {
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double calculatedUpkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testNoMinimumCalculatedUpkeep() throws Exception {
        // Original version has 1.0D minimum.  Should be removed.
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        double calculatedUpkeep = 0.1D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }

    @Test
    public void testCalculatedUpkeep() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        double calculatedUpkeep = 10.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        long result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
        calculatedUpkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        result = (long)((double)moneyLeft / calculatedUpkeep * 500000.0D);
        Assert.assertEquals(result, call());
    }
}
