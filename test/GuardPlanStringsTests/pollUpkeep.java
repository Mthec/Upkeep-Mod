package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

// TODO - Also test logging?
// TODO - calculateUpkeep not right.  See getTimeLeft.
public class pollUpkeep extends GuardPlanStringsTest {
    public pollUpkeep() {
        methodsToTest.put("public boolean pollUpkeep()", GuardPlanStrings.pollUpkeep);
    }

    private boolean call() throws Exception{
        return (boolean)GuardPlan.getDeclaredMethod("pollUpkeep").invoke(gPlan);
    }

    @Test
    public void testFalseIfIsPermanent() throws Exception {
        Village.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(false, call());
    }

    @Test
    public void testFalseIfNotUpkeep() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        Assert.assertEquals(false, call());
    }

    @Test
    public void testTrueIfMoneyRunsOut() throws Exception {
        long moneyLeft = GuardPlan.getDeclaredField("moneyLeft").getLong(gPlan);
        long upkeep = (long)GuardPlan.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert moneyLeft - upkeep <= 0L;
        Assert.assertEquals(true, call());
    }

    @Test
    public void testGuardPlanUpdated() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testUpkeepCounterIncremented() throws Exception {
        call();
        Field upkeepCounter = GuardPlan.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);
        Assert.assertEquals(1, value);
    }

    @Test
    public void testUpkeepCounterResetAt2() throws Exception {
        Field upkeepCounter = GuardPlan.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        upkeepCounter.setInt(gPlan, 1);
        call();
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);

        Assert.assertEquals(0, value);
    }

    @Test
    public void testKingsShopUpdated() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testGuardPlanUpdatedWhenUpkeepLow() throws Exception {
        if (true) {
            throw new Exception("???");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testBroadcastOnLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testDelayedBroadcastOnLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testBroadcastOnVeryLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testDelayedBroadcastOnVeryLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testBroadcastOnExtremelyLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testNotDelayedBroadcastOnExtremelyLowTimeLeft() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testFalseOnPass() throws Exception {
        Assert.assertEquals(false, call());
    }

    // My Changes
    @Test
    public void testWhyUpkeepIsLessThanPoint0() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testUpkeepBufferIncremented() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        /*long monthlyCost = 10L;

        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, monthlyCost);

        call();

        Assert.assertEquals(true, GuardPlan.getDeclaredField("upkeepBuffer").getDouble(gPlan));*/
    }

    @Test
    public void testUpkeepBufferLessThan1() throws Exception {
        Assert.assertTrue(GuardPlan.getDeclaredField("upkeepBuffer").getDouble(gPlan) < 1.0D);
    }

    @Test
    public void testOutput() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testFalseOnWholeUpkeep() throws Exception {
        // TODO - Should this return false?  Why not continue?
        Assert.assertEquals(false, call());
    }
}
