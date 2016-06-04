package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class pollUpkeep extends GuardPlanStringsTest {
    public pollUpkeep() {
        methodsToTest.put("pollUpkeep", GuardPlanStrings.pollUpkeep);
    }

    private boolean call() throws Exception{
        return (boolean)GuardPlan.getDeclaredMethod("pollUpkeep").invoke(gPlan);
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(true, call());
    }
}
