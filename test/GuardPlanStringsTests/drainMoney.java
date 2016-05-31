package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class drainMoney extends GuardPlanStringsTest {
    drainMoney() {
        methodToTest = GuardPlanStrings.getMoneyDrained;
    }

    @Test
    public void test() throws Exception {
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(null, 50);
        Assert.assertEquals(50L, GuardPlan.getDeclaredMethod("drainMoney").invoke(GuardPlan.newInstance()));
    }
}
