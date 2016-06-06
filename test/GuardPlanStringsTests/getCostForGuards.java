package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class getCostForGuards extends GuardPlanStringsTest {
    public getCostForGuards() {
        methodsToTest.put("public long getCostForGuards(int numGuards)", GuardPlanStrings.getCostForGuards);
    }

    private long call(int value) throws Exception{
        Method method = GuardPlan.getDeclaredMethod("getCostForGuards", int.class);
        method.setAccessible(true);
        return (long)method.invoke(gPlan, value);
    }

    @Test
    public void testGuardCosts() throws Exception {
        // Probably don't need a test for this, but going to do it just as a precaution.
        long guardUpkeep = 100L;
        Villages.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(guardUpkeep * i, call(i));
        }
        guardUpkeep *= 12;
        Villages.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(guardUpkeep * i, call(i));
        }
    }
}
