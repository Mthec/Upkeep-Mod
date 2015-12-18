import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseEntriesTest {
    Connection db;
    PreparedStatement ps;
    ResultSet rs;

    @Before
    public void setUp() throws SQLException {
        String currentDir = "Creative Copy";
        ServerDirInfo.setFileDBPath(currentDir + (currentDir.endsWith(File.separator) ? "" : File.separator));
        ServerDirInfo.setConstantsFileName(ServerDirInfo.getFileDBPath() + "wurm.ini");
        Constants.load();
        Constants.dbHost = currentDir;
        Constants.dbPort = "";
        Constants.loginDbHost = currentDir;
        Constants.loginDbPort = "";
        Constants.siteDbHost = currentDir;
        Constants.siteDbPort = "";
        DbConnector.initialize();
        db = DbConnector.getZonesDbCon();
    }

    @After
    public void tearDown() {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(db);
        DbConnector.closeAll();
    }

    @Test
    public void testUpkeepBufferInDB () throws SQLException {
        ps = db.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='UPKEEP_BUFFER';");
        rs = ps.executeQuery();
        Assert.assertTrue(rs.next());
    }

    @Test
    public void testBufferForEveryVillage () throws SQLException {
        PreparedStatement ps2 = db.prepareStatement("SELECT ID FROM VILLAGES;");
        ResultSet rs2 = ps2.executeQuery();
        Assert.assertTrue(rs2.isBeforeFirst());
        while (rs2.next()) {
            ps = db.prepareStatement("SELECT * FROM UPKEEP_BUFFER WHERE VILLAGEID=?");
            ps.setInt(1, rs2.getInt("ID"));
            rs = ps.executeQuery();
            Assert.assertTrue(rs.isBeforeFirst());
        }
        DbUtilities.closeDatabaseObjects(ps2, rs2);
    }
}
