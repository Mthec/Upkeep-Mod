import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.junit.Assert;
import org.junit.Test;

public class ServerSetupTest extends ServerSetup {
    @Test
    public void testMoneyDrained () throws NoSuchVillageException {
        Village village = Villages.getVillage(1);
        Assert.assertEquals(12, village.plan.getMoneyDrained());
    }
}
