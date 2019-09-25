package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

// TODO - Also test logging?
public class pollUpkeep extends GuardPlanMethodsTest {

    private boolean call() throws Exception {
        return (boolean)GuardPlanClass.getDeclaredMethod("pollUpkeep").invoke(gPlan);
    }

    private void causeDisband() throws Exception {
        long moneyLeft = 10L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long upkeep = 100000L;
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, upkeep);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert moneyLeft - calculatedUpkeep <= 0L;
    }

    @Test
    public void testFalseIfIsPermanent() throws Exception {
        Village.class.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        // Cause disband to make certain test is accurate.
        causeDisband();
        Assert.assertEquals(false, call());
    }

    @Test
    public void testFalseIfNotUpkeep() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        // Cause disband to make certain test is accurate.
        causeDisband();
        Assert.assertEquals(false, call());
    }

    @Test
    public void testTrueIfMoneyRunsOut() throws Exception {
        causeDisband();
        Assert.assertEquals(true, call());
    }

    @Test
    public void testFalseOnPass() throws Exception {
        long moneyLeft = 1000L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long upkeep = 100L;
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, upkeep);
        assert moneyLeft - (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true) > 0L;
        Assert.assertEquals(false, call());
    }

    @Test
    public void testGuardPlanUpdated() throws Exception {
        long moneyLeft = 1000L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long upkeep = 1L;
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, upkeep);
        int type = 1;
        GuardPlanClass.getDeclaredField("type").setInt(gPlan, type);
        long newMoneyLeft = moneyLeft - (long)(double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        int guards = 3;
        GuardPlanClass.getDeclaredField("hiredGuardNumber").setInt(gPlan, guards);
        call();
        Assert.assertEquals("type value incorrect:", type, GuardPlanClass.getDeclaredField("updateGuardPlan1").getInt(gPlan));
        Assert.assertEquals("newMoneyLeft value incorrect:", newMoneyLeft, GuardPlanClass.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals("guards value incorrect:", guards, GuardPlanClass.getDeclaredField("updateGuardPlan3").getInt(gPlan));
    }

    @Test
    public void testGuardPlanUpdatedWhenUpkeepLow() throws Exception {
        // Trigger sub-1L upkeep to test that Math.max(1L, upkeep) does not apply.
        long moneyLeft = 1000L;
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long upkeep = 1L;
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, upkeep);
        int type = 1;
        GuardPlanClass.getDeclaredField("type").setInt(gPlan, type);
        int guards = 3;
        GuardPlanClass.getDeclaredField("hiredGuardNumber").setInt(gPlan, guards);
        call();
        Assert.assertEquals("type value incorrect:", type, GuardPlanClass.getDeclaredField("updateGuardPlan1").getInt(gPlan));
        Assert.assertEquals("moneyLeft value incorrect:", moneyLeft, GuardPlanClass.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals("guards value incorrect:", guards, GuardPlanClass.getDeclaredField("updateGuardPlan3").getInt(gPlan));
    }

    @Test
    public void testUpkeepCounterIncremented() throws Exception {
        call();
        Field upkeepCounter = GuardPlanClass.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);
        Assert.assertEquals(1, value);
    }

    @Test
    public void testUpkeepCounterResetAt2() throws Exception {
        Field upkeepCounter = GuardPlanClass.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        upkeepCounter.setInt(gPlan, 1);
        call();
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);

        Assert.assertEquals(0, value);
    }

    @Test
    public void testKingsShopUpdated() throws Exception {
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 25000L);
        double upkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
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
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 0L);
        double upkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert upkeep == 0.0D;
        // Two calls to trigger shop update.
        call();
        call();
        Assert.assertEquals(0, KingsShop.getDeclaredField("money").getLong(null));
        call();
        call();
        Assert.assertEquals(0, KingsShop.getDeclaredField("money").getLong(null));
    }

    @Test
    public void testNoBroadcastOnGreaterThanWeekLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 6040L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 1L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) > 604800000L;
        call();
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).isEmpty());
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).isEmpty());
    }

    @Test
    public void testBroadcastOnWeekLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 604L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 1L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 604800000L;
        call();
        Assert.assertEquals("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)4,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testDelayedBroadcastOnWeekLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 604L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 2L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 604800000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlanClass.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).isEmpty());
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).isEmpty());
    }

    @Test
    public void testBroadcastOnDayTimeLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 60L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 1L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 86400000L;
        call();
        Assert.assertEquals("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testDelayedBroadcastOnDayTimeLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 60L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 86400000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlanClass.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).isEmpty());
        Assert.assertTrue(((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).isEmpty());
    }

    @Test
    public void testBroadcastOnHourTimeLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 6L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 3600000L;
        call();
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testNotDelayedBroadcastOnHourLeft() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 6L);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 3600000L;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlanClass.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > 3600000L);
        call();
        call();
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(0));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(0));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals(null,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
        Assert.assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(2));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(2));
        Assert.assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(3));
        Assert.assertEquals(null,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(3));
    }

    // My Changes
    @Test
    public void testUpkeepBufferIncrementedProperly() throws Exception {
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 4600L);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert calculatedUpkeep < 1.0D && calculatedUpkeep * 2 > 1.0D;
        call();
        Assert.assertEquals(calculatedUpkeep, GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.001);
        call();
        Assert.assertEquals((calculatedUpkeep * 2) - 1.0, GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.001);
    }

    @Test
    public void testUpkeepBufferLessThan1() throws Exception {
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 10L);
        double calculatedUpkeep = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert calculatedUpkeep * 100 < 1.0D;
        for (int i = 0; i <= 100; i++){
            call();
            Assert.assertTrue(GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan) < 1.0D);
        }
    }

    @Test
    public void testCorrectUpkeepRemoved() throws Exception {
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 2420L);
        // Silly, I know.
        assert (long)((double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true) * 1000.0D) == 500L;
        call();
        Assert.assertEquals(moneyLeft, GuardPlanClass.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals(0.5D, GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.01D);
        call();
        Assert.assertEquals(moneyLeft - 1, GuardPlanClass.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals(0.0D, GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.01D);
        call();
        // Not moneyLeft - 1 as the fake updateGuardPlan doesn't affect moneyLeft.
        Assert.assertEquals(moneyLeft, GuardPlanClass.getDeclaredField("updateGuardPlan2").getLong(gPlan));
        Assert.assertEquals(0.5D, GuardPlanClass.getDeclaredField("upkeepBuffer").getDouble(gPlan), 0.01D);
    }

    @Test
    public void testOutput() throws Exception {
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 4500L);
        double upkeepD = (double)GuardPlanClass.getDeclaredMethod("calculateUpkeep", boolean.class).invoke(gPlan, true);
        assert upkeepD < 1.0D && upkeepD * 2 > 1.0D;
        GuardPlanClass.getDeclaredField("output").setBoolean(gPlan, true);
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

    @Test
    public void testBroadcastOnWeekFreeMessage() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 604L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 1L);
        VillagesClass.getDeclaredField("FREE_TILES").setLong(null, 10);
        VillagesClass.getDeclaredField("FREE_PERIMETER").setLong(null, 100);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 604800000L;
        call();
        Assert.assertEquals("You may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals((byte)4,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testBroadcastOnDayFreeMessage() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 60L);
        GuardPlanClass.getDeclaredField("monthlyCost").setLong(gPlan, 1L);
        VillagesClass.getDeclaredField("FREE_TILES").setLong(null, 10);
        VillagesClass.getDeclaredField("FREE_PERIMETER").setLong(null, 100);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 86400000L;
        call();
        Assert.assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }

    @Test
    public void testBroadcastOnHourFreeMessage() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 6L);
        VillagesClass.getDeclaredField("FREE_TILES").setLong(null, 10);
        VillagesClass.getDeclaredField("FREE_PERIMETER").setLong(null, 100);
        assert (long)GuardPlanClass.getDeclaredMethod("getTimeLeft").invoke(gPlan) < 3600000L;
        call();
        Assert.assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                ((List)Village.class.getDeclaredField("broadcastMessage").get(gVillage)).get(1));
        Assert.assertEquals((byte)2,
                ((List)Village.class.getDeclaredField("broadcastBytes").get(gVillage)).get(1));
    }
}
