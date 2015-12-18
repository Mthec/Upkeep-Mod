import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class UpkeepPropertiesTest {
    UpkeepCosts upkeep;
    Properties properties;

    @Before
    public void setUp() throws IOException{
        upkeep = new UpkeepCosts();
        properties = new Properties();
        properties.load(UpkeepPropertiesTest.class.getResourceAsStream("sampleupkeepcosts.properties"));
    }

    @After
    public void tearDown() {
        upkeep = null;
        properties = null;
    }

    @Test
    public void testConfigure() {
        upkeep.configure(properties);
        Assert.assertEquals(Long.valueOf((String)properties.get("tile_cost")), upkeep.tile_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("tile_upkeep")), upkeep.tile_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_cost")), upkeep.perimeter_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_upkeep")), upkeep.perimeter_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_cost")), upkeep.normal_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_upkeep")), upkeep.normal_guard_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_cost")), upkeep.epic_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_upkeep")), upkeep.epic_guard_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("minimum_upkeep")), upkeep.minimum_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("into_upkeep")), upkeep.into_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("name_change")), upkeep.name_change);
    }

    @Test
    public void testNegative() {
        properties.setProperty("tile_cost", Long.toString(-1));
        upkeep.configure(properties);
        Assert.assertNull(upkeep.tile_cost);
    }

    @Test
    public void testInvalid() {
        properties.setProperty("tile_cost", "0.0");
        upkeep.configure(properties);
        Assert.assertNull(upkeep.tile_cost);
    }

    @Test
    public void testMissing() {
        properties.remove("tile_cost");
        upkeep.configure(properties);
        Assert.assertNull(upkeep.tile_cost);

    }
}
