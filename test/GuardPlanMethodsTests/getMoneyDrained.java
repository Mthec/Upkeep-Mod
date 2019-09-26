package GuardPlanMethodsTests;

import com.wurmonline.server.villages.Village;
import org.junit.Assert;
import org.junit.Test;

public class getMoneyDrained extends GuardPlanMethodsTest {
    private long call() throws Exception {
        return (long)GuardPlanClass.getDeclaredMethod("getMoneyDrained").invoke(gPlan);
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
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, moneyLeft);
        long monthlyCost = (long)GuardPlanClass.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
        long minMoneyDrained = GuardPlanClass.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert moneyLeft < minMoneyDrained;
        assert moneyLeft < monthlyCost * 0.15;
        Assert.assertEquals(moneyLeft, call());
    }

    @Test
    public void testDrainModifier() throws Exception {
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = 10000L;
        gPlan.monthlyCost = monthlyCost;
        long minMoneyDrained = GuardPlanClass.getDeclaredField("minMoneyDrained").getLong(gPlan);
        assert monthlyCost * 0.15 > minMoneyDrained;
        assert monthlyCost * 0.15 < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.0), call());
        GuardPlanClass.getDeclaredField("drainModifier").setFloat(gPlan, 0.5f);
        Assert.assertEquals((long)(monthlyCost * 0.15 * 1.5), call());
    }

    @Test
    public void testBelowMinimumDrain() throws Exception {
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = (long)GuardPlanClass.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
        long minMoneyDrained = 1000L;
        GuardPlanClass.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained > monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals(minMoneyDrained, call());
    }

    @Test
    public void testAboveMinimumDrain() throws Exception {
        long moneyLeft = GuardPlanClass.getDeclaredField("moneyLeft").getLong(gPlan);
        long monthlyCost = (long)GuardPlanClass.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
        long minMoneyDrained = 1L;
        GuardPlanClass.getDeclaredField("minMoneyDrained").setLong(gPlan, minMoneyDrained);
        assert minMoneyDrained < monthlyCost * 0.15;
        assert minMoneyDrained < moneyLeft;
        Assert.assertEquals((long)(monthlyCost * 0.15), call());
    }

    @Test
    public void test15Percent() throws Exception {
        GuardPlanClass.getDeclaredField("moneyLeft").setLong(gPlan, 99999999999L);
        GuardPlanClass.getDeclaredField("minMoneyDrained").setLong(gPlan, 0L);

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
