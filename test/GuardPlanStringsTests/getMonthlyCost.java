package GuardPlanStringsTests;

import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Test;

public class getMonthlyCost extends GuardPlanStringsTest {
    public getMonthlyCost() {
        methodsToTest.put("public long getMonthlyCost()", GuardPlanStrings.getMonthlyCost);
    }

    private long call() throws Exception {
        return (long)GuardPlan.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
    }

    @Test
    public void testTileUpkeep() throws Exception {
        long tileUpkeep = 10L;
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        long upkeep = Village.getDeclaredField("numTiles").getLong(gVillage) * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testPerimeterUpkeep() throws Exception {
        long perimeter_upkeep = 10L;
        long perimeter_tiles = 100L;
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, perimeter_tiles);
        long upkeep = perimeter_tiles * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }
    
    @Test
    public void testUpkeepOff() throws Exception {
        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, false);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Villages.getDeclaredField("FREE_TILES").setLong(null, 0L);
        long upkeep = numTiles * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        long freeTiles = numTiles - 100L;
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Villages.getDeclaredField("FREE_TILES").setLong(null, freeTiles);
        long upkeep = (numTiles - freeTiles) * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        long freeTiles = numTiles + 100L;
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Villages.getDeclaredField("FREE_TILES").setLong(null, freeTiles);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreePerimeters() throws Exception {
        Village.getDeclaredField("numTiles").setLong(gVillage, 0L);
        long perimeter_upkeep = 10L;
        long perimeterNonFreeTiles = 100L;
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, perimeterNonFreeTiles);
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        Villages.getDeclaredField("FREE_PERIMETER").setLong(null, 0L);
        long upkeep = perimeterNonFreeTiles * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreePerimeters() throws Exception {
        Village.getDeclaredField("numTiles").setLong(gVillage, 0L);
        long perimeter_upkeep = 10L;
        long perimeterNonFreeTiles = 100L;
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles - 100L;
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        Villages.getDeclaredField("FREE_PERIMETER").setLong(null, freePerimeter);
        long upkeep = (perimeterNonFreeTiles - freePerimeter) * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreePerimeters() throws Exception {
        Village.getDeclaredField("numTiles").setLong(gVillage, 0L);
        long perimeter_upkeep = 10L;
        long perimeterNonFreeTiles = 100L;
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles + 100L;
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        Villages.getDeclaredField("FREE_PERIMETER").setLong(null, freePerimeter);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testGuardCostAdded() throws Exception {
        GuardPlan.getDeclaredField("hiredGuardNumber").setInt(gPlan, 2);
        Villages.getDeclaredField("GUARD_UPKEEP").setLong(null, 10L);
        Assert.assertEquals(20L, call());
    }

    @Test
    public void testCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Village.getDeclaredField("isCapital").setBoolean(gVillage, true);
        long upkeep = (numTiles * tileUpkeep) / 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizens() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Village.getDeclaredField("tooManyCitizens").setBoolean(gVillage, true);
        long upkeep = (numTiles * tileUpkeep) * 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizensAndCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Village.getDeclaredField("tooManyCitizens").setBoolean(gVillage, true);
        Village.getDeclaredField("isCapital").setBoolean(gVillage, true);
        long upkeep = (numTiles * tileUpkeep);
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testMinimumUpkeep() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = Village.getDeclaredField("numTiles").getLong(gVillage);
        long minimumUpkeep = 2000L;
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Villages.getDeclaredField("MINIMUM_UPKEEP").setLong(null, minimumUpkeep);
        long upkeep = (numTiles * tileUpkeep);
        assert upkeep < minimumUpkeep;
        Assert.assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testDifferentSizes() throws Exception {
        long tileUpkeep = 100L;
        long perimeterUpkeep = 10L;
        long perimeterNonFreeTiles;
        long numTiles;
        long upkeep;
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeterUpkeep);
        for (int i = 11; i <= 100; i++) {
            for (int p = 0; p <= 10; p++) {
                numTiles = i * i;
                Village.getDeclaredField("numTiles").setLong(gVillage, numTiles);
                perimeterNonFreeTiles = 100L;
                Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, perimeterNonFreeTiles);
                upkeep = (numTiles * tileUpkeep) + (perimeterNonFreeTiles * perimeterUpkeep);
                Assert.assertEquals(upkeep, call());
            }
        }
    }

}
