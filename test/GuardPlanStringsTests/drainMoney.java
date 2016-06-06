package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class drainMoney extends GuardPlanStringsTest {
    public drainMoney() {
        methodsToTest.put("public long getMoneyDrained", GuardPlanStrings.getMoneyDrained);
        methodsToTest.put("public long drainMoney", GuardPlanStrings.drainMoney);
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

    }

    @Test
    public void testSaveDrainModCalled() throws Exception {

    }

    @Test
    public void testDrainIncrements() throws Exception {

    }

    @Test
    public void testDrainAmount() throws Exception {

    }
}
