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
        // Cause true to make test accurate.
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        assert moneyLeft - upkeep <= 0L;
        Assert.assertEquals(false, call());
    }

    @Test
    public void testFalseIfNotUpkeep() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        // Cause true to make test accurate.
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        assert moneyLeft - upkeep <= 0L;
        Assert.assertEquals(false, call());
    }

    @Test
    public void testTrueIfMoneyRunsOut() throws Exception {
        long moneyLeft = 10L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        assert moneyLeft - upkeep <= 0L;
        Assert.assertEquals(true, call());
    }

    @Test
    public void testGuardPlanUpdated() throws Exception {
        long moneyLeft = 1000L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 1.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        int type = 1;
        GuardPlan.getDeclaredField("type").setInt(gPlan, type);
        long newMoneyLeft = moneyLeft - (long)upkeep;
        int guards = 3;
        GuardPlan.getDeclaredField("hiredGuardNumber").setInt(gPlan, guards);
        call();
        Assert.assertEquals("type value incorrect:", type, GuardPlan.getDeclaredField("updateGuardPlan1").getInt(gPlan));
        Assert.assertEquals("newMoneyLeft value incorrect:", newMoneyLeft, GuardPlan.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals("guards value incorrect:", guards, GuardPlan.getDeclaredField("updateGuardPlan3").getInt(gPlan));
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
        // Trigger sub-1L upkeep to test Math.max(1L, upkeep).
        long moneyLeft = 1000L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 0.02D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        int type = 1;
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
