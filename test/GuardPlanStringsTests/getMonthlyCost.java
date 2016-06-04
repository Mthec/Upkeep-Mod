package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class getMonthlyCost extends GuardPlanStringsTest {
    public getMonthlyCost() {
        // TEMP
        if (Objects.equals(getClass().getName(), "badGetMonthlyCost")) {
            return;
        }
        methodsToTest.put("public long getMonthlyCost", GuardPlanStrings.getMonthlyCost);
    }

    private long call() throws Exception {
        return (long)GuardPlan.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
    }

    @Test
    public void testTileUpkeep() throws Exception {
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, 10L);
        Object village = GuardPlan.getDeclaredField("village").get(gPlan);
        Village.getDeclaredField("numTiles").setLong(village, 10L);
        Assert.assertEquals(100L, call());
    }

    @Test
    public void testPerimeterUpkeep() throws Exception {
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, 10L);
        Object village = GuardPlan.getDeclaredField("village").get(gPlan);
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(village, 10L);
        Assert.assertEquals(100L, call());
    }
    
    @Test
    public void testUpkeepOff() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreeTiles() throws Exception {
        
    }

    @Test
    public void testLowFreeTiles() throws Exception {

    }

    @Test
    public void testOverFreeTiles() throws Exception {

    }

    @Test
    public void testZeroFreePerimeter() throws Exception {

    }

    @Test
    public void testLowFreePerimeter() throws Exception {

    }

    @Test
    public void testOverFreePerimeter() throws Exception {

    }

    @Test
    public void testGuardCostAdded() throws Exception {

    }

    @Test
    public void testCapital() throws Exception {

    }

    @Test
    public void testTooManyCitizens() throws Exception {

    }

    @Test
    public void testMinimumUpkeep() throws Exception {

    }

    @Test
    public void testDifferentSizes() throws Exception {

    }

}
