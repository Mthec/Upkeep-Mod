package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

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
    public void testFalseOnPass() throws Exception {
        long moneyLeft = 1000L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 100.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        assert moneyLeft - upkeep > 0L;
        Assert.assertEquals(false, call());
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
    public void testGuardPlanUpdatedWhenUpkeepLow() throws Exception {
        // Trigger sub-1L upkeep to test that Math.max(1L, upkeep) does not apply.
        long moneyLeft = 1000L;
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        double upkeep = 0.02D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        int type = 1;
        GuardPlan.getDeclaredField("type").setInt(gPlan, type);
        int guards = 3;
        GuardPlan.getDeclaredField("hiredGuardNumber").setInt(gPlan, guards);
        call();
        Assert.assertEquals("type value incorrect:", type, GuardPlan.getDeclaredField("updateGuardPlan1").getInt(gPlan));
        Assert.assertEquals("moneyLeft value incorrect:", moneyLeft, GuardPlan.getDeclaredField("updateGuardPlan2").getLong(gPlan));
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
        double upkeep = 5.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        // Two calls to trigger shop update.
        call();
        call();
        Assert.assertEquals((long)upkeep, KingsShop.getDeclaredField("money").getLong(null));
        call();
        call();
        Assert.assertEquals((long)upkeep * 2, KingsShop.getDeclaredField("money").getLong(null));
    }

    @Test
    public void testKingsShopUpdatedWhenUpkeep0() throws Exception {
        double upkeep = 0.0D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeep);
        // Two calls to trigger shop update.
        call();
        call();
        Assert.assertEquals(0, KingsShop.getDeclaredField("money").getLong(null));
        call();
        call();
        Assert.assertEquals(0, KingsShop.getDeclaredField("money").getLong(null));
    }

    @Test
    public void testBroadcastOnWeekLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 604L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 604800000L;
        call();
        Assert.assertEquals("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)4,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testDelayedBroadcastOnWeekLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 604L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 604800000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertTrue(((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).isEmpty());
        Assert.assertTrue(((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).isEmpty());
    }

    @Test
    public void testBroadcastOnDayTimeLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 60L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 86400000L;
        call();
        Assert.assertEquals("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testDelayedBroadcastOnDayTimeLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 60L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 86400000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertTrue(((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).isEmpty());
        Assert.assertTrue(((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).isEmpty());
    }

    @Test
    public void testBroadcastOnHourTimeLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 6L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 3600000L;
        call();
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testNotDelayedBroadcastOnHourLeft() throws Exception {
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 6L);
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, 1.0D);
        assert (long)GuardPlan.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 3600000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(2));
        Assert.assertEquals((byte)2,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(2));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.getDeclaredField("broadcastMessage").get(gVillage)).get(3));
        Assert.assertEquals(null,
                ((List)Village.getDeclaredField("broadcastBytes").get(gVillage)).get(3));
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
    public void testUpkeepDNotRoundResults() throws Exception {
        if (true) {
            throw new Exception("TODO");
        }
        Assert.assertEquals(true, call());
    }

    @Test
    public void testUpkeepBufferIncremented() throws Exception {
        double calculatedUpkeep = 0.75D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        call();
        Assert.assertEquals(calculatedUpkeep, GuardPlan.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.001);
        call();
        Assert.assertEquals((calculatedUpkeep * 2) - 1.0, GuardPlan.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.001);
    }

    @Test
    public void testUpkeepBufferLessThan1() throws Exception {
        double calculatedUpkeep = 0.75D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, calculatedUpkeep);
        for (int i = 0; i <= 100; i++){
            Assert.assertTrue(GuardPlan.getDeclaredField("upkeepBuffer").getDouble(gPlan) < 1.0D);
        }
    }

    @Test
    public void testOutput() throws Exception {
        double upkeepD = 0.9D;
        GuardPlan.getDeclaredField("calculatedUpkeep").setDouble(gPlan, upkeepD);
        GuardPlan.getDeclaredField("output").setBoolean(gPlan, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        call();
        Assert.assertEquals("Village upkeep - VILLAGE_NAME paid 0.0 this turn.  Upkeep buffer is now " + Double.toString(upkeepD) + System.lineSeparator(),
                out.toString());
        out.reset();
        call();
        Assert.assertEquals("Village upkeep - VILLAGE_NAME paid 1.0 this turn.  Upkeep buffer is now " + Double.toString((upkeepD * 2) - 1) + System.lineSeparator(),
                out.toString());
        System.setOut(null);
    }
}
