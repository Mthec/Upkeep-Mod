package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.Assert;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanMethodsTest {
    private long call() {
        return gPlan.getMoneyDrained();
    }

    @Test
    public void testPermanentVillage() throws Exception {
        Village.class.getDeclaredField("isPermanent").setBoolean(gVillage, true);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testNotPermanentVillage() throws Exception {
        Village.class.getDeclaredField("isPermanent").setBoolean(gVillage, false);
        Assert.assertNotEquals(0L, call());
    }

    @Test
    public void testLowMoneyLeft() throws Exception {
        long moneyLeft = 10L;
        gPlan.moneyLeft = moneyLeft;
        long monthlyCost = gPlan.getMonthlyCost();
        long minMoneyDrained = UpkeepCosts.min_drain;
        assert moneyLeft < minMoneyDrained;
        assert moneyLeft < monthlyCost * 0.15;
        Assert.assertEquals(moneyLeft, call());
    }

    @Test
    public void testDrainModifier() throws Exception {
        long moneyLeft = gPlan.moneyLeft;
        long monthlyCost = 10000L;
        gPlan.monthlyCost = monthlyCost;
        long minMoneyDrained = UpkeepCosts.min_drain;
        assert monthlyCost * 0.15 > minMoneyDrained;
        assert monthlyCost * 0.15 < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.0), call());
        gPlan.drainModifier = 0.5f;
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.5), call());
    }

    @Test
    public void testBelowMinimumDrain() throws Exception {
        long moneyLeft = gPlan.moneyLeft;
        long monthlyCost = gPlan.getMonthlyCost();
        long minMoneyDrained = 1000L;
        UpkeepCosts.min_drain = minMoneyDrained;
        assert minMoneyDrained > monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals(minMoneyDrained, call());
    }

    @Test
    public void testAboveMinimumDrain() throws Exception {
        long moneyLeft = gPlan.moneyLeft;
        long monthlyCost = gPlan.getMonthlyCost();
        long minMoneyDrained = 1L;
        UpkeepCosts.min_drain = minMoneyDrained;
        assert minMoneyDrained < monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
    }

    @Test
    public void test15Percent() throws Exception {
        gPlan.moneyLeft = 99999999999L;
        UpkeepCosts.min_drain = 0L;

        long monthlyCost = 1000L;
        gPlan.monthlyCost = monthlyCost;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 250000L;
        gPlan.monthlyCost = monthlyCost;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
        monthlyCost = 50000000L;
        gPlan.monthlyCost = monthlyCost;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());

    }
}