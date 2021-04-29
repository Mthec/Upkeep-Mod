import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.utils.DbUtilities;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseEntriesTest {
    Connection db;
    PreparedStatement ps;
    ResultSet rs;

    @Before
    public void setUp() throws SQLException {
        String currentDir = "Creative Copy";
        ServerDirInfo.setPath(Paths.get(currentDir + (currentDir.endsWith(File.separator) ? "" : File.separator)));
        Constants.load();
        Constants.dbHost = currentDir;
        Constants.dbPort = "";
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
}
