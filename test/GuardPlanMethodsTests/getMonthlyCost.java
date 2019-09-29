package GuardPlanMethodsTests;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.villages.MyCitizen;
import com.wurmonline.server.villages.Villages;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

public class getMonthlyCost extends GuardPlanMethodsTest {    
    private long call() {
        return gPlan.getMonthlyCost();
    }

    private int numTiles() {
        return gVillage.getDiameterX() * gVillage.getDiameterY();
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
        Villages.TILE_UPKEEP = tileUpkeep;
        long upkeep = numTiles() * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testPerimeterUpkeep() throws Exception {
        long perimeter_upkeep = 10L;
        int perimeter_tiles = 100;
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeter_tiles);
        long upkeep = perimeter_tiles * perimeter_upkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testUpkeepOff() throws Exception {
        ReflectionUtil.setPrivateField(Servers.localServer, ServerEntry.class.getDeclaredField("upkeep"), false);
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = 0L;
        long upkeep = numTiles * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        long freeTiles = numTiles - 100L;
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = freeTiles;
        long upkeep = (numTiles - freeTiles) * tileUpkeep;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreeTiles() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        long freeTiles = numTiles + 100L;
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = freeTiles;
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testZeroFreePerimeters() throws Exception {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
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
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
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
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        UpkeepCosts.free_perimeter = freePerimeter;
        Assert.assertEquals(0L, call());
    }

    @Test
    public void testGuardCostAdded() throws Exception {
        gPlan.hiredGuardNumber = 2;
        Villages.GUARD_UPKEEP = 10L;
        Assert.assertEquals(20L, call());
    }

    @Test
    public void testCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        when(gVillage.isCapital()).thenReturn(true);
        long upkeep = (numTiles * tileUpkeep) / 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizens() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        fillMaxCitizens();
        long upkeep = (numTiles * tileUpkeep) * 2;
        Assert.assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizensAndCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = numTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
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
        Villages.TILE_UPKEEP = tileUpkeep;
        VillagesClass.getDeclaredField("MINIMUM_UPKEEP").setLong(null, minimumUpkeep);
        long upkeep = (numTiles * tileUpkeep);
        assert upkeep < minimumUpkeep;
        Assert.assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testDifferentSizes() throws Exception {
        long tileUpkeep = 100L;
        long perimeterUpkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        long numTiles;
        long upkeep;
        Villages.TILE_UPKEEP = tileUpkeep;
        Villages.PERIMETER_UPKEEP = perimeterUpkeep;
        for (int i = 11; i <= 100; i++) {
            for (int p = 0; p <= 10; p++) {
                numTiles = i * i;
                when(gVillage.getNumTiles()).thenReturn((int)numTiles);
                upkeep = (numTiles * tileUpkeep) + (perimeterNonFreeTiles * perimeterUpkeep);
                Assert.assertEquals(upkeep, call());
            }
        }
    }
}
