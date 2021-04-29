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
        Assert.assertEquals(Long.valueOf((String)properties.get("free_tiles")).longValue(), UpkeepCosts.free_tiles);
        Assert.assertEquals(Boolean.valueOf((String)properties.get("free_tiles_upkeep")), UpkeepCosts.free_tiles_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_cost")).longValue(), upkeep.perimeter_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("free_perimeter")).longValue(), UpkeepCosts.free_perimeter);
        Assert.assertEquals(Boolean.valueOf((String)properties.get("free_perimeter_upkeep")), UpkeepCosts.free_perimeter_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("perimeter_upkeep")).longValue(), upkeep.perimeter_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_cost")).longValue(), upkeep.normal_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("normal_guard_upkeep")).longValue(), upkeep.normal_guard_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_cost")).longValue(), upkeep.epic_guard_cost);
        Assert.assertEquals(Long.valueOf((String)properties.get("epic_guard_upkeep")).longValue(), upkeep.epic_guard_upkeep);
        Assert.assertEquals(Boolean.valueOf((String)properties.get("epic_guard_upkeep_scaling")), UpkeepCosts.epic_guard_upkeep_scaling);
        Assert.assertEquals(Long.valueOf((String)properties.get("free_guards")).longValue(), UpkeepCosts.free_guards);
        Assert.assertEquals(Boolean.valueOf((String)properties.get("free_guards_upkeep")), UpkeepCosts.free_guards_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("minimum_upkeep")).longValue(), upkeep.minimum_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("into_upkeep")).longValue(), upkeep.into_upkeep);
        Assert.assertEquals(Long.valueOf((String)properties.get("name_change")).longValue(), upkeep.name_change);
        Assert.assertEquals(Integer.valueOf((String)properties.get("upkeep_grace_period")).intValue(), UpkeepCosts.upkeep_grace_period);
        Assert.assertEquals(Long.valueOf((String)properties.get("min_drain")).longValue(), UpkeepCosts.min_drain);
        Assert.assertEquals(Float.valueOf((String)properties.get("max_drain_modifier")), UpkeepCosts.max_drain_modifier, 0.0F);
        Assert.assertEquals(Float.valueOf((String)properties.get("drain_modifier_increment")), UpkeepCosts.drain_modifier_increment, 0.0F);
        Assert.assertEquals(Boolean.valueOf((String)properties.get("use_per_server_settings")), upkeep.use_per_server_settings);
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
