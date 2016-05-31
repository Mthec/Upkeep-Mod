package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanStringsTest {
    @Before
    public void setUp() throws Exception {
        this.createGuardPlan(GuardPlanStrings.getMoneyDrained);
    }

    @Test
    public void test() throws Exception {
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(null, 50);
        Assert.assertEquals(50L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(GuardPlan.newInstance()));
    }
}
