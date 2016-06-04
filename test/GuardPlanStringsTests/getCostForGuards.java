package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getCostForGuards extends GuardPlanStringsTest {
    public getCostForGuards() {
        methodsToTest.put("getCostForGuards", GuardPlanStrings.getCostForGuards);
    }

    private long call() throws Exception{
        return (long)GuardPlan.getDeclaredMethod("getCostForGuards").invoke(gPlan);
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(50L, call());
    }
}
