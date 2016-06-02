package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanStringsTest {
    public getMoneyDrained() {
        methodsToTest.put("public long getMoneyDrained", GuardPlanStrings.getMoneyDrained);
    }

    //@Test
    public void test() throws Exception {
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(null, 50);
        Assert.assertEquals(50L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(GuardPlan.newInstance()));
    }

    @Test
    public void testVillagePermanence() throws Exception {
        Object gPlan = GuardPlan.newInstance();
        gPlan.getClass().getDeclaredMethod("setIsPermanent", boolean.class).invoke(gPlan, true);
        Assert.assertEquals(0L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(gPlan));
    }
}
