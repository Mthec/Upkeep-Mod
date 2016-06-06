package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getTimeLeft extends GuardPlanStringsTest {
    public getTimeLeft() {
        insertAftersToTest.put("public long getTimeLeft()", GuardPlanStrings.getTimeLeft);
    }

    private long call() throws Exception{
        return (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan);
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(50L, call());
    }
}
