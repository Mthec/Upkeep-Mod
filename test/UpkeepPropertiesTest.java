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
        Assert.assertEquals(Long.valueOf((String)properties.get("tile_cost")).longValue(), upkeep.tile_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("tile_upkeep")).longValue(), upkeep.tile_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_cost")).longValue(), upkeep.perimeter_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_upkeep")).longValue(), upkeep.perimeter_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_cost")).longValue(), upkeep.normal_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_upkeep")).longValue(), upkeep.normal_guard_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_cost")).longValue(), upkeep.epic_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_upkeep")).longValue(), upkeep.epic_guard_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("minimum_upkeep")).longValue(), upkeep.minimum_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("into_upkeep")).longValue(), upkeep.into_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("name_change")).longValue(), upkeep.name_change);
    }

    @Test
    public void testNegative() {
        long initial = upkeep.tile_cost;
        properties.setProperty("tile_cost", Long.toString(-1));
        upkeep.configure(properties);
        Assert.assertEquals(initial, upkeep.tile_cost);
    }

    @Test
    public void testInvalid() {
        long initial = upkeep.tile_cost;
        properties.setProperty("tile_cost", "0.0");
        upkeep.configure(properties);
        Assert.assertEquals(initial, upkeep.tile_cost);
    }

    @Test
    public void testMissing() {
        long initial = upkeep.tile_cost;
        properties.setProperty("tile_cost", "99999");
        properties.remove("tile_cost");
        upkeep.configure(properties);
        Assert.assertEquals(initial, upkeep.tile_cost);

    }
}
