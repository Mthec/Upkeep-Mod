package com.wurmonline.server.villages;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.economy.Economy;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

// TODO - Also test logging?
public class pollUpkeep extends GuardPlanMethodsTest {
    private final List<String> broadcastMessages = new ArrayList<>();
    private final List<Byte> broadcastBytes = new ArrayList<>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        broadcastMessages.clear();
        broadcastBytes.clear();

        Answer<String> answer = i -> {
            broadcastMessages.add(i.getArgument(0));
            if (i.getArguments().length == 2)
                broadcastBytes.add(i.getArgument(1));
            else
                broadcastBytes.add(null);
            return null;
        };

        doAnswer(answer).when(gVillage).broadCastAlert(anyString(), anyByte());
        doAnswer(answer).when(gVillage).broadCastAlert(anyString());
        doAnswer(answer).when(gVillage).broadCastNormal(anyString());
    }

    private boolean call() throws Exception {
        return gPlan.pollUpkeep();
    }

    private void causeDisband() {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = 100000L;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        assert moneyLeft - calculatedUpkeep <= 0L;
    }

    @Test
    public void testFalseIfIsPermanent() throws Exception {
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("isPermanent"), true);
        // Cause disband to make certain test is accurate.
        causeDisband();
        assertFalse(call());
    }

    @Test
    public void testFalseIfNotUpkeep() throws Exception {
        setUpkeep(false);
        // Cause disband to make certain test is accurate.
        causeDisband();
        assertFalse(call());
    }

    @Test
    public void testTrueIfMoneyRunsOut() throws Exception {
        causeDisband();
        assertTrue(call());
    }

    @Test
    public void testFalseOnPass() throws Exception {
        long moneyLeft = 1000L;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = 100L;
        assert moneyLeft - gPlan.calculateUpkeep(true) > 0L;
        assertFalse(call());
    }

    @Test
    public void testGuardPlanUpdated() throws Exception {
        long moneyLeft = 1000L;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = 1L;
        int type = 1;
        gPlan.type = type;
        long newMoneyLeft = moneyLeft - (long)gPlan.calculateUpkeep(true);
        int guards = 3;
        gPlan.hiredGuardNumber = guards;
        call();
        assertEquals("type value incorrect:", type, gPlan.updateGuardPlan1);
        assertEquals("newMoneyLeft value incorrect:", newMoneyLeft, gPlan.updateGuardPlan2);
        assertEquals("guards value incorrect:", guards, gPlan.updateGuardPlan3);
    }

    @Test
    public void testGuardPlanUpdatedWhenUpkeepLow() throws Exception {
        // Trigger sub-1L upkeep to test that Math.max(1L, upkeep) does not apply.
        long moneyLeft = 1000L;
        gPlan.moneyLeft = moneyLeft;
        gPlan.monthlyCost = 1L;
        int type = 1;
        gPlan.type = type;
        int guards = 3;
        gPlan.hiredGuardNumber = guards;
        call();
        assertEquals("type value incorrect:", type, gPlan.updateGuardPlan1);
        assertEquals("moneyLeft value incorrect:", moneyLeft, gPlan.updateGuardPlan2);
        assertEquals("guards value incorrect:", guards, gPlan.updateGuardPlan3);
    }

    @Test
    public void testUpkeepCounterIncremented() throws Exception {
        call();
        Field upkeepCounter = GuardPlan.class.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);
        assertEquals(1, value);
    }

    @Test
    public void testUpkeepCounterResetAt2() throws Exception {
        Field upkeepCounter = GuardPlan.class.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        upkeepCounter.setInt(gPlan, 1);
        call();
        int value = upkeepCounter.getInt(gPlan);
        upkeepCounter.setAccessible(false);

        assertEquals(0, value);
    }

    @Test
    public void testKingsShopUpdated() throws Exception {
        gPlan.monthlyCost = 25000L;
        double upkeep = gPlan.calculateUpkeep(true);
        // Two calls to trigger shop update.
        call();
        call();
        assertEquals((long)upkeep, Economy.getEconomy().getKingsShop().getMoney());
        call();
        call();
        assertEquals((long)upkeep * 2, Economy.getEconomy().getKingsShop().getMoney());
    }

    @Test
    public void testKingsShopUpdatedWhenUpkeep0() throws Exception {
        gPlan.monthlyCost = 0L;
        double upkeep = gPlan.calculateUpkeep(true);
        assert upkeep == 0.0D;
        // Two calls to trigger shop update.
        call();
        call();
        assertEquals(0, Economy.getEconomy().getKingsShop().getMoney());
        call();
        call();
        assertEquals(0, Economy.getEconomy().getKingsShop().getMoney());
    }

    private void setMoneyLeftTo(long millis) {
        double upkeep = ((double)gPlan.monthlyCost / TimeConstants.MONTH_MILLIS) * millis;
        double buffered = upkeep - (long)upkeep;

        gPlan.moneyLeft = (long)upkeep + 1;
        gPlan.upkeepBuffer = 1 - buffered;
    }

    @Test
    public void testNoBroadcastOnGreaterThanWeekLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.WEEK_MILLIS + TimeConstants.DAY_MILLIS);
        assert gPlan.getTimeLeft() > TimeConstants.WEEK_MILLIS;
        call();
        assertTrue(broadcastMessages.isEmpty());
        assertTrue(broadcastBytes.isEmpty());
    }

    @Test
    public void testBroadcastOnWeekLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.WEEK_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.WEEK_MILLIS;
        call();
        assertEquals("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.",
                broadcastMessages.get(0));
        assertEquals((byte)4,
                (byte)broadcastBytes.get(0));
        assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                broadcastMessages.get(1));
        assertNull(broadcastBytes.get(1));
    }

    @Test
    public void testDelayedBroadcastOnWeekLeft() throws Exception {
        gPlan.monthlyCost = 2L;
        setMoneyLeftTo(TimeConstants.WEEK_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.WEEK_MILLIS;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.class.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > TimeConstants.HOUR_MILLIS);
        call();
        call();
        assertTrue(broadcastMessages.isEmpty());
        assertTrue(broadcastBytes.isEmpty());
    }

    @Test
    public void testBroadcastOnDayTimeLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.DAY_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.DAY_MILLIS;
        call();
        assertEquals("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.",
                broadcastMessages.get(0));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(0));
        assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                broadcastMessages.get(1));
        assertNull(broadcastBytes.get(1));
    }

    @Test
    public void testDelayedBroadcastOnDayTimeLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.DAY_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.DAY_MILLIS;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.class.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > TimeConstants.HOUR_MILLIS);
        call();
        call();
        assertTrue(broadcastMessages.isEmpty());
        assertTrue(broadcastBytes.isEmpty());
    }

    @Test
    public void testBroadcastOnHourTimeLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.HOUR_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.HOUR_MILLIS;
        call();
        assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                broadcastMessages.get(0));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(0));
        assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                broadcastMessages.get(1));
        assertNull(broadcastBytes.get(1));
    }

    @Test
    public void testNotDelayedBroadcastOnHourLeft() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.HOUR_MILLIS - 1);
        assert gPlan.getTimeLeft() < TimeConstants.HOUR_MILLIS;
        long lastSentWarning = System.currentTimeMillis();
        Field field = GuardPlan.class.getDeclaredField("lastSentWarning");
        field.setAccessible(true);
        field.setLong(gPlan, lastSentWarning);
        field.setAccessible(false);
        assert !(System.currentTimeMillis() - lastSentWarning > TimeConstants.HOUR_MILLIS);
        call();
        call();
        assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                broadcastMessages.get(0));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(0));
        assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                broadcastMessages.get(1));
        assertNull(broadcastBytes.get(1));
        assertEquals("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.",
                broadcastMessages.get(2));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(2));
        assertEquals("Any traders who are citizens of VILLAGE_NAME will disband without refund.",
                broadcastMessages.get(3));
        assertNull(broadcastBytes.get(3));
    }

    // My Changes
    @Test
    public void testUpkeepBufferIncrementedProperly() throws Exception {
        gPlan.monthlyCost = 4600L;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        assert calculatedUpkeep < 1.0D && calculatedUpkeep * 2 > 1.0D;
        call();
        assertEquals(calculatedUpkeep, gPlan.upkeepBuffer, 0.001);
        call();
        assertEquals((calculatedUpkeep * 2) - 1.0, gPlan.upkeepBuffer, 0.001);
    }

    @Test
    public void testUpkeepBufferLessThan1() throws Exception {
        gPlan.monthlyCost = 10L;
        double calculatedUpkeep = gPlan.calculateUpkeep(true);
        assert calculatedUpkeep * 100 < 1.0D;
        for (int i = 0; i <= 100; i++){
            call();
            assertTrue(gPlan.upkeepBuffer < 1.0D);
        }
    }

    @Test
    public void testCorrectUpkeepRemoved() throws Exception {
        long moneyLeft = gPlan.moneyLeft;
        gPlan.monthlyCost = 2420L;
        // Silly, I know.
        assert (long)(gPlan.calculateUpkeep(true) * 1000.0D) == 500L;
        call();
        assertEquals(moneyLeft, gPlan.updateGuardPlan2);
        assertEquals(0.5D, gPlan.upkeepBuffer, 0.01D);
        call();
        assertEquals(moneyLeft - 1, gPlan.updateGuardPlan2);
        assertEquals(0.0D, gPlan.upkeepBuffer, 0.01D);
        call();
        assertEquals(moneyLeft - 1, gPlan.updateGuardPlan2);
        assertEquals(0.5D, gPlan.upkeepBuffer, 0.01D);
    }

    @Test
    public void testOutput() throws Exception {
        gPlan.monthlyCost = 4500L;
        double upkeepD = gPlan.calculateUpkeep(true);
        assert upkeepD < 1.0D && upkeepD * 2 > 1.0D;
        UpkeepCosts.output = true;
        PrintStream outStream = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        call();
        assertEquals("Village upkeep - VILLAGE_NAME paid 0.0 this turn.  Upkeep buffer is now " + upkeepD + System.lineSeparator(),
                out.toString());
        out.reset();
        call();
        assertEquals("Village upkeep - VILLAGE_NAME paid 1.0 this turn.  Upkeep buffer is now " + ((upkeepD * 2) - 1) + System.lineSeparator(),
                out.toString());
        System.setOut(outStream);
    }

    @Test
    public void testBroadcastOnWeekFreeMessage() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.WEEK_MILLIS);
        UpkeepCosts.free_tiles = 10;
        UpkeepCosts.free_perimeter = 100;
        assert gPlan.getTimeLeft() == TimeConstants.WEEK_MILLIS;
        call();
        assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                broadcastMessages.get(1));
        assertEquals((byte)4,
                (byte)broadcastBytes.get(1));
    }

    @Test
    public void testBroadcastOnDayFreeMessage() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.DAY_MILLIS - 1);
        UpkeepCosts.free_tiles = 10;
        UpkeepCosts.free_perimeter = 100;
        assert gPlan.getTimeLeft() < TimeConstants.DAY_MILLIS;
        call();
        assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                broadcastMessages.get(1));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(1));
    }

    @Test
    public void testBroadcastOnHourFreeMessage() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.HOUR_MILLIS - 1);
        UpkeepCosts.free_tiles = 10;
        UpkeepCosts.free_perimeter = 100;
        assert gPlan.getTimeLeft() < TimeConstants.HOUR_MILLIS;
        call();
        assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles and 100 free perimeter tiles.",
                broadcastMessages.get(1));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(1));
    }

    @Test
    public void testBroadcastOnHourFreeMessageOnlyTiles() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.HOUR_MILLIS - 1);
        UpkeepCosts.free_tiles = 10;
        UpkeepCosts.free_perimeter = 0;
        assert gPlan.getTimeLeft() < TimeConstants.HOUR_MILLIS;
        call();
        assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 10 free tiles.",
                broadcastMessages.get(1));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(1));
    }

    @Test
    public void testBroadcastOnHourFreeMessageOnlyPerimeter() throws Exception {
        gPlan.monthlyCost = 1L;
        setMoneyLeftTo(TimeConstants.HOUR_MILLIS - 1);
        UpkeepCosts.free_tiles = 0;
        UpkeepCosts.free_perimeter = 100;
        assert gPlan.getTimeLeft() < TimeConstants.HOUR_MILLIS;
        call();
        assertEquals("Or you may resize to remove any non-free tiles.  You can have up to 100 free perimeter tiles.",
                broadcastMessages.get(1));
        assertEquals((byte)2,
                (byte)broadcastBytes.get(1));
    }

    @Test
    public void testBroadcastOnGracePeriodAlmostOver() throws Exception {
        UpkeepCosts.upkeep_grace_period = 1;
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("creationDate"), System.currentTimeMillis() - 1000);
        long timeLeft = GuardPlanMethods.graceTimeRemaining(gVillage);
        assert timeLeft > 0 && timeLeft < TimeConstants.DAY_MILLIS;
        assertFalse(call());
        assertEquals("Your village upkeep grace period will run out soon.",
                broadcastMessages.get(0));
        assertEquals(1, broadcastMessages.size());
    }

    @Test
    public void testMonthlyTotalUpkeep() throws Exception {
        gPlan.monthlyCost = 100L;
        long startingMoney = gPlan.monthlyCost + 1;
        gPlan.moneyLeft = startingMoney;

        // days in month * 24 hours * 60 minutes / pollInterval
        int polls = 28 * 24 * 60 / 8;

        for (int i = 0; i < polls; ++i) {
            call();
        }

        assertEquals(gPlan.moneyLeft, startingMoney - gPlan.monthlyCost);
    }

    @Test
    public void testZeroCostDeedDoesNotDisband() throws Exception {
        UpkeepCosts.free_tiles = 441;
        UpkeepCosts.free_guards = 1;
        Villages.MINIMUM_UPKEEP = 0;

        gPlan.moneyLeft = 0;

        assert gPlan.calculateUpkeep(true) == 0;
        assertFalse(call());
    }

    @Test
    public void testGradePeriodLeftReturnsNegativeForNotUsed() {
        UpkeepCosts.upkeep_grace_period = 0;

        assertEquals(-1, GuardPlanMethods.graceTimeRemaining(gVillage));
    }

    @Test
    public void testGradePeriodLeft() throws NoSuchFieldException {
        UpkeepCosts.upkeep_grace_period = 1;
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("creationDate"), System.currentTimeMillis());

        assertWithin(TimeConstants.DAY_MILLIS, GuardPlanMethods.graceTimeRemaining(gVillage), 100);
    }

    @Test
    public void testGradePeriodLeftOtherValue() throws NoSuchFieldException {
        UpkeepCosts.upkeep_grace_period = 2;
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("creationDate"), System.currentTimeMillis());

        assertWithin(TimeConstants.DAY_MILLIS * 2, GuardPlanMethods.graceTimeRemaining(gVillage), 100);
    }

    @SuppressWarnings("SameParameterValue")
    private void assertWithin(long expected, long actual, long epsilon) {
        assertTrue(expected < actual + epsilon);
        assertTrue(expected > actual - epsilon);
    }
}
