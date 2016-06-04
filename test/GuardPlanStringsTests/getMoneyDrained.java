package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanStringsTest {
    public getMoneyDrained() {
        methodsToTest.put("public long getMoneyDrained", GuardPlanStrings.getMoneyDrained);
    }

    private long call() throws Exception {
        return (long)GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan);
    }

    @Test
    public void testPermanentVillage() throws Exception {
        Village.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testNotPermanentVillage() throws Exception {
        Village.getDeclaredField("isPermanent").setBoolean(gVillage, false);
        Assert.assertNotEquals(0L, call());
    }

    @Test
    public void testLowMoneyLeft() throws Exception {
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long monthlyCost = (long)GuardPlan.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
        long minMoneyDrained = GuardPlan.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert moneyLeft < minMoneyDrained;
        assert moneyLeft < monthlyCost * 0.15;
        Assert.assertEquals(moneyLeft, call());
    }

    @Test
    public void testDrainModifier() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = 10000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        long minMoneyDrained = GuardPlan.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert monthlyCost * 0.15 > minMoneyDrained;
        assert monthlyCost * 0.15 < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.0), call());
        GuardPlan.getDeclaredField("drainModifier").setFloat(gPlan, 0.5f);
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.5), call());
    }

    @Test
    public void testBelowMinimumDrain() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = GuardPlan.getDeclaredField("monthlyCost").getLong(gPlan);
        long minMoneyDrained = 1000L;
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained > monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals(minMoneyDrained, call());
    }

    @Test
    public void testAboveMinimumDrain() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = GuardPlan.getDeclaredField("monthlyCost").getLong(gPlan);
        long minMoneyDrained = 1L;
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained < monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
    }

    @Test
    public void test15Percent() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 99999999999L);
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, 0L);

        long monthlyCost = 1000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 250000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 50000000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), call());

    }
}
