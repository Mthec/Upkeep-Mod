package GuardPlanMethodsTests;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.villages.MyCitizen;
import com.wurmonline.server.villages.Villages;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class getMonthlyCost extends GuardPlanMethodsTest {    
    private long call() {
        return gPlan.getMonthlyCost();
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
    public void testTileUpkeep() {
        long tileUpkeep = 10L;
        Villages.TILE_UPKEEP = tileUpkeep;
        long upkeep = gVillage.getNumTiles() * tileUpkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testPerimeterUpkeep() {
        long perimeter_upkeep = 10L;
        int perimeter_tiles = 100;
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeter_tiles);
        long upkeep = perimeter_tiles * perimeter_upkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testUpkeepOff() throws Exception {
        ReflectionUtil.setPrivateField(Servers.localServer, ServerEntry.class.getDeclaredField("upkeep"), false);
        assertEquals(0L, call());
    }

    @Test
    public void testZeroFreeTiles() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = 0L;
        long upkeep = numTiles * tileUpkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreeTiles() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        long freeTiles = numTiles - 100L;
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = freeTiles;
        long upkeep = (numTiles - freeTiles) * tileUpkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreeTiles() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        long freeTiles = numTiles + 100L;
        Villages.TILE_UPKEEP = tileUpkeep;
        UpkeepCosts.free_tiles = freeTiles;
        assertEquals(0L, call());
    }

    @Test
    public void testZeroFreePerimeters() {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        UpkeepCosts.free_perimeter = 0L;
        long upkeep = perimeterNonFreeTiles * perimeter_upkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testLowFreePerimeters() {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles - 100L;
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        UpkeepCosts.free_perimeter = freePerimeter;
        long upkeep = (perimeterNonFreeTiles - freePerimeter) * perimeter_upkeep;
        assertEquals(upkeep, call());
    }

    @Test
    public void testOverFreePerimeters() {
        setTilesToZero();
        long perimeter_upkeep = 10L;
        int perimeterNonFreeTiles = 100;
        when(gVillage.getPerimeterNonFreeTiles()).thenReturn(perimeterNonFreeTiles);
        long freePerimeter = perimeterNonFreeTiles + 100L;
        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        UpkeepCosts.free_perimeter = freePerimeter;
        assertEquals(0L, call());
    }

    @Test
    public void testGuardCostAdded() {
        gPlan.hiredGuardNumber = 2;
        Villages.GUARD_UPKEEP = 10L;
        assertEquals(20L, call());
    }

    @Test
    public void testCapital() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        when(gVillage.isCapital()).thenReturn(true);
        long upkeep = (numTiles * tileUpkeep) / 2;
        assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizens() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        fillMaxCitizens();
        long upkeep = (numTiles * tileUpkeep) * 2;
        assertEquals(upkeep, call());
    }

    @Test
    public void testTooManyCitizensAndCapital() throws Exception {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        Villages.TILE_UPKEEP = tileUpkeep;
        fillMaxCitizens();
        when(gVillage.isCapital()).thenReturn(true);
        long upkeep = (numTiles * tileUpkeep);
        assertEquals(upkeep, call());
    }

    @Test
    public void testMinimumUpkeep() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        long minimumUpkeep = 2000L;
        Villages.TILE_UPKEEP = tileUpkeep;
        Villages.MINIMUM_UPKEEP = minimumUpkeep;
        long upkeep = (numTiles * tileUpkeep);
        assert upkeep < minimumUpkeep;
        assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testMinimumUpkeepWithPerimeter() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles() + gVillage.getPerimeterNonFreeTiles();
        long minimumUpkeep = 4000L;
        Villages.TILE_UPKEEP = tileUpkeep;
        Villages.PERIMETER_UPKEEP = tileUpkeep;
        Villages.MINIMUM_UPKEEP = minimumUpkeep;
        long upkeep = (numTiles * tileUpkeep);
        assert upkeep < minimumUpkeep;
        assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testMinimumUpkeepWithPerimeterAndGuards() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles() + gVillage.getPerimeterNonFreeTiles();
        long minimumUpkeep = 5000L;
        when(gPlan.getNumHiredGuards()).thenReturn(10);
        Villages.TILE_UPKEEP = tileUpkeep;
        Villages.PERIMETER_UPKEEP = tileUpkeep;
        Villages.GUARD_UPKEEP = tileUpkeep;
        Villages.MINIMUM_UPKEEP = minimumUpkeep;
        long upkeep = (numTiles * tileUpkeep) + gPlan.getNumHiredGuards() * tileUpkeep;
        assert upkeep < minimumUpkeep;
        assertEquals(minimumUpkeep, call());
    }

    @Test
    public void testAboveMinimumUpkeep() {
        long tileUpkeep = 10L;
        long numTiles = gVillage.getNumTiles();
        assert numTiles == 100;
        long minimumUpkeep = numTiles * tileUpkeep - 1;
        Villages.TILE_UPKEEP = tileUpkeep;
        Villages.MINIMUM_UPKEEP = minimumUpkeep;
        long upkeep = (numTiles * tileUpkeep);
        assertEquals(upkeep, call());
    }

    @Test
    public void testDifferentSizes() {
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
                assertEquals(upkeep, call());
            }
        }
    }
}
