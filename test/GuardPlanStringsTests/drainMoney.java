package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class drainMoney extends GuardPlanStringsTest {
    public drainMoney() {
        methodsToTest.put("public long getMoneyDrained", GuardPlanStrings.getMoneyDrained);
        methodsToTest.put("public long drainMoney", GuardPlanStrings.drainMoney);
    }

    @Test
    public void test() throws Exception {
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(null, 50);
        Assert.assertEquals(50L, GuardPlan.getDeclaredMethod("drainMoney").invoke(GuardPlan.newInstance()));
    }
}