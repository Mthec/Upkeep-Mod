import com.wurmonline.server.villages.GuardPlan;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class GuardPlanTest extends ServerSetup {
    UpkeepCosts upkeep;
    Properties properties;

    @Before
    public void setUp2() throws IOException {
        upkeep = new UpkeepCosts();
        properties = new Properties();
        properties.load(GuardPlanTest.class.getResourceAsStream("sampleupkeepcosts.properties"));
    }

    @After
    public void tearDown2() {
        upkeep = null;
        properties = null;
    }

    @Test
    public void testGuardCostsChangesWork() throws InterruptedException {
        properties.setProperty("epic_guard_upkeep", "0");
        upkeep.configure(properties);

//        controller.startDB("Creative Copy");
//        // TODO - onServerStart is not called for some reason.
//        upkeep.onServerStarted();
//        Thread.sleep(10000);
        Assert.assertEquals(0, GuardPlan.getCostForGuards(1));
    }
}
