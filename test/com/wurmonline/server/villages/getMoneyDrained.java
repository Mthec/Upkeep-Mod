package com.wurmonline.server.villages;

import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class getMoneyDrained extends GuardPlanMethodsTest {
    private long call() {
        return gPlan.getMoneyDrained();
    }

    @Test
    public void testPermanentVillage() throws Exception {
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("isPermanent"), true);
        assertEquals(0L, call());
    }

    @Test
    public void testNotPermanentVillage() throws Exception {
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("isPermanent"), false);
        assertNotEquals(0L, call());
    }

    @Test
    public void testLowMoneyLeft() {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        Villages.TILE_UPKEEP = 1;
        long monthlyCost = gPlan.getMonthlyCost();
        assert moneyLeft < UpkeepCosts.min_drain;
        assert moneyLeft < monthlyCost * 0.15;
        assertEquals(moneyLeft, call());
    }

    @Test
    public void testDrainModifier() {
        long monthlyCost = 100000L;
        gPlan.monthlyCost = monthlyCost;
        long minMoneyDrained = UpkeepCosts.min_drain;
        gPlan.moneyLeft = monthlyCost;
        assert monthlyCost * 0.15 > minMoneyDrained;
        assert monthlyCost * 0.15 < gPlan.moneyLeft;
        assertEquals((long)(monthlyCost * 0.15 * 1.0), call());
        gPlan.drainModifier = 0.5f;
        assertEquals((long)(monthlyCost * 0.15 * 1.5), call());
    }

    @Test
    public void testBelowMinimumDrain() {
        Villages.TILE_UPKEEP = 10;
        long monthlyCost = gPlan.getMonthlyCost();
        long minMoneyDrained = 1000L;
        gPlan.moneyLeft = minMoneyDrained * 10;
        UpkeepCosts.min_drain = minMoneyDrained;
        assert minMoneyDrained > monthlyCost * 0.15;
        assertEquals(minMoneyDrained, call());
    }

    @Test
    public void testAboveMinimumDrain() {
        long moneyLeft = gPlan.moneyLeft;
        Villages.TILE_UPKEEP = 1;
        long monthlyCost = gPlan.getMonthlyCost();
        long minMoneyDrained = 1L;
        UpkeepCosts.min_drain = minMoneyDrained;
        assert minMoneyDrained < monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        assertEquals((long)(monthlyCost * 0.15), call());
    }

    @Test
    public void test15Percent() {
        gPlan.moneyLeft = 99999999999L;
        UpkeepCosts.min_drain = 0L;

        long monthlyCost = 1000L;
        gPlan.monthlyCost = monthlyCost;
        assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 250000L;
        gPlan.monthlyCost = monthlyCost;
        assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 50000000L;
        gPlan.monthlyCost = monthlyCost;
        assertEquals((long)(monthlyCost * 0.15), call());

    }
}
