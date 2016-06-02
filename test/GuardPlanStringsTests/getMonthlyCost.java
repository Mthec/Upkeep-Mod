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

    @Test
    public void testTileUpkeep() throws Exception {
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, 10L);
        Object village = GuardPlan.getDeclaredField("village").get(gPlan);
        Village.getDeclaredField("numTiles").setLong(village, 10L);
        Assert.assertEquals(100L, GuardPlan.getDeclaredMethod("getMonthlyCost").invoke(gPlan));
    }
}
