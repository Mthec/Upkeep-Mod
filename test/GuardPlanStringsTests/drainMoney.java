package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class drainMoney extends GuardPlanStringsTest {
    public drainMoney() {
        methodsToTest.put("public long getMoneyDrained()", GuardPlanStrings.getMoneyDrained);
        methodsToTest.put("public long drainMoney()", GuardPlanStrings.drainMoney);
    }

    private long call() throws Exception{
        return (long)GuardPlan.getDeclaredMethod("drainMoney").invoke(gPlan);
    }

    @Test
    public void testMaxDrain() throws Exception {
        float maxDrainModifier = 9.0f;
        GuardPlan.getDeclaredField("drainCumulateFigure").setFloat(gPlan, 10.0f);
        GuardPlan.getDeclaredField("maxDrainModifier").setFloat(gPlan, maxDrainModifier);
        call();
        Assert.assertEquals(maxDrainModifier, GuardPlan.getDeclaredField("drainModifier").getFloat(gPlan), 0.0001f);
    }

    @Test
    public void testDrainGuardPlanCalled() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long moneyDrained = (long)GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan);
        call();
        Assert.assertEquals(moneyLeft - moneyDrained, GuardPlan.getDeclaredField("guardPlanDrained").getLong(gPlan));
    }

    @Test
    public void testSaveDrainModCalled() throws Exception {
        call();
        Assert.assertTrue(GuardPlan.getDeclaredField("savedDrainMod").getBoolean(gPlan));
    }

    @Test
    public void testDrainAmount() throws Exception {
        Assert.assertEquals((long)GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan), call());
    }

    @Test
    public void testDrainIncrements() throws Exception {
        long drainCost = (long)GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan);
        GuardPlan.getDeclaredField("drainCumulateFigure").setFloat(gPlan, 0.5f);
        GuardPlan.getDeclaredField("maxDrainModifier").setFloat(gPlan, 5.0f);
        Assert.assertEquals(drainCost, call());
        long newDrainCost = (long)(drainCost * 1.5);
        Assert.assertEquals(newDrainCost, call());
        newDrainCost = (long)(drainCost * 2.0);
        Assert.assertEquals(newDrainCost, call());
    }
}
