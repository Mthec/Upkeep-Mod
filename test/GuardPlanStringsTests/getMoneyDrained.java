package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanStringsTest {
    public getMoneyDrained() {
        methodsToTest.put("public long getMoneyDrained", GuardPlanStrings.getMoneyDrained);
    }

    @Test
    public void testPermanentVillage() throws Exception {
        gPlan.getClass().getDeclaredMethod("setIsPermanent", boolean.class).invoke(gPlan, true);
        Assert.assertEquals(0L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void testNotPermanentVillage() throws Exception {
        gPlan.getClass().getDeclaredMethod("setIsPermanent", boolean.class).invoke(gPlan, false);
        Assert.assertNotEquals(0L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void testLowMoneyLeft() throws Exception {
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long monthlyCost = (long)GuardPlan.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
        long minMoneyDrained = GuardPlan.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert moneyLeft < minMoneyDrained;
        assert moneyLeft < monthlyCost * 0.15;
        Assert.assertEquals(moneyLeft, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void testDrainModifier() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = 10000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        long minMoneyDrained = GuardPlan.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert monthlyCost * 0.15 > minMoneyDrained;
        assert monthlyCost * 0.15 < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.0), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
        GuardPlan.getDeclaredField("drainModifier").setFloat(gPlan, 0.5f);
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.5), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void testBelowMinimumDrain() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = GuardPlan.getDeclaredField("monthlyCost").getLong(gPlan);
        long minMoneyDrained = 1000L;
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained > monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals(minMoneyDrained, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void testAboveMinimumDrain() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = GuardPlan.getDeclaredField("monthlyCost").getLong(gPlan);
        long minMoneyDrained = 1L;
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained < monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }

    @Test
    public void test15Percent() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 99999999999L);
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, 0L);

        long monthlyCost = 1000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
        monthlyCost = 250000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
        monthlyCost = 50000000L;
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);
        Assert.assertEquals((long)(monthlyCost * 0.15), GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));

    }
}
