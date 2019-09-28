package GuardPlanMethodsTests;

import com.wurmonline.server.villages.MyCitizen;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

public class getMonthlyCost extends GuardPlanMethodsTest {    
    private long call() throws Exception {
        return (long)GuardPlanClass.getDeclaredMethod("getMonthlyCost").invoke(gPlan);
    }

    private int numTiles() {
        return gVillage.getDiameterX() * gVillage.getDiameterY();
    }

    private void setDimensions(int width, int height) {
        gVillage.startx = width;
        gVillage.starty = height;
    }

    private void setTilesToZero() {
        gVillage.startx = -1;
        gVillage.starty = -1;
    }
    
    private void fillMaxCitizens() throws IOException {
        for (long i = 0; i <= gVillage.getMaxCitizens(); ++i)
            gVillage.citizens.put(i, new MyCitizen());
    }

    @Test
    public void testTileUpkeep() throws Exception {
        long tileUpkeep = 10L;
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        long upkeep = numTiles() * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testPerimeterUpkeep() throws Exception {
        long perimeter_upkeep = 10L;
        int perimeter_tiles = 100;
        VillagesClass.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeter_tiles);
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
        long numTiles = numTiles();
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        UpkeepCosts.free_tiles = 0L;
        long upkeep = numTiles * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        long freeTiles = numTiles - 100L;
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        UpkeepCosts.free_tiles = freeTiles;
        long upkeep = (numTiles - freeTiles) * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        long freeTiles = numTiles + 100L;
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        UpkeepCosts.free_tiles = freeTiles;
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreePerimeters() throws Exception {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        VillagesClass.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        UpkeepCosts.free_perimeter = 0L;
        long upkeep = perimeterNonFreeTiles * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreePerimeters() throws Exception {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles - 100L;
        VillagesClass.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        UpkeepCosts.free_perimeter = freePerimeter;
        long upkeep = (perimeterNonFreeTiles - freePerimeter) * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreePerimeters() throws Exception {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles + 100L;
        VillagesClass.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeter_upkeep);
        UpkeepCosts.free_perimeter = freePerimeter;
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testGuardCostAdded() throws Exception {
        GuardPlanClass.getDeclaredField("hiredGuardNumber").setInt(gPlan, 2);
        VillagesClass.getDeclaredField("GUARD_UPKEEP").setLong(null, 10L);
        Assert.assertEquals(20L, call());
    }

    @Test
    public void testCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        when(gVillage.isCapital()).thenReturn(true);
        long upkeep = (numTiles * tileUpkeep) / 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizens() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        fillMaxCitizens();
        long upkeep = (numTiles * tileUpkeep) * 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizensAndCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        fillMaxCitizens();
        when(gVillage.isCapital()).thenReturn(true);
        long upkeep = (numTiles * tileUpkeep);
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testMinimumUpkeep() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        long minimumUpkeep = 2000L;
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        VillagesClass.getDeclaredField("MINIMUM_UPKEEP").setLong(null, minimumUpkeep);
        long upkeep = (numTiles * tileUpkeep);
        assert upkeep < minimumUpkeep;
        Assert.assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testDifferentSizes() throws Exception {
        long tileUpkeep = 100L;
        long perimeterUpkeep = 10L;
        int perimeterNonFreeTiles;
        long numTiles;
        long upkeep;
        VillagesClass.getDeclaredField("TILE_UPKEEP").setLong(null, tileUpkeep);
        VillagesClass.getDeclaredField("PERIMETER_UPKEEP").setLong(null, perimeterUpkeep);
        for (int i = 11; i <= 100; i++) {
            for (int p = 0; p <= 10; p++) {
                numTiles = i * i;
                setDimensions(i, i);
                perimeterNonFreeTiles = 100;
                when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
                upkeep = (numTiles * tileUpkeep) + (perimeterNonFreeTiles * perimeterUpkeep);
                Assert.assertEquals(upkeep, call());
            }
        }
    }
}
