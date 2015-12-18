package mod.wurmonline.mods.upkeepcosts;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Villages;
import javassist.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpkeepCosts implements WurmMod, Configurable, PreInitable, ServerStartedListener {
    protected static final Logger logger = Logger.getLogger(UpkeepCosts.class.getName());
    public Long tile_upkeep;
    public Long tile_cost;
    public Long perimeter_cost;
    public Long perimeter_upkeep;
    public Long normal_guard_cost;
    public Long normal_guard_upkeep;
    public Long epic_guard_cost;
    public Long epic_guard_upkeep;
    public Long minimum_upkeep;
    public Long into_upkeep;
    public Long name_change;
    ResourceBundle messages = ResourceBundle.getBundle("mod.wurmonline.mods.upkeepcosts.UpkeepCosts");
    private boolean createdDb = false;

    @Override
    public void configure(Properties properties) {
        for (Field field : this.getClass().getFields()) {
            if (!(field.getType().isAssignableFrom(Long.class))) {
                continue;
            }
            try {
                field.set(this, Long.valueOf(properties.getProperty(field.getName())));
                if ((Long) field.get(this) < 0) {
                    field.set(this, null);
                    negative(field.getName());
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                System.exit(-1);
            } catch (NumberFormatException ex) {
                invalid(field.getName());
            }
        }
    }

    @Override
    public void onServerStarted() {
        lateConfigure();
        ServerEntry local = Servers.localServer;
        if (!local.isUpkeep()) {
            logger.info(messages.getString("no_upkeep"));
        }
        if (local.isFreeDeeds()) {
            logger.info(messages.getString("free_deeds"));
        }
        if (tile_cost != null) {
            Villages.TILE_COST = tile_cost;
            Villages.TILE_COST_STRING = (new Change(Villages.TILE_COST)).getChangeString();
        }

        if (tile_upkeep != null) {
            Villages.TILE_UPKEEP = tile_upkeep;
            Villages.TILE_UPKEEP_STRING = (new Change(Villages.TILE_UPKEEP)).getChangeString();
        }

        if (perimeter_cost != null) {
            Villages.PERIMETER_COST = perimeter_cost;
            Villages.PERIMETER_COST_STRING = (new Change(Villages.PERIMETER_COST)).getChangeString();
        }

        if (perimeter_upkeep != null) {
            Villages.PERIMETER_UPKEEP = perimeter_upkeep;
            Villages.PERIMETER_UPKEEP_STRING = (new Change(Villages.PERIMETER_UPKEEP)).getChangeString();
        }
        
        if (local.isChallengeOrEpicServer()) {
            if (epic_guard_cost != null) {
                Villages.GUARD_COST = epic_guard_cost;
            }
            if (epic_guard_upkeep != null) {
                Villages.GUARD_UPKEEP = epic_guard_upkeep;
            }
        }
        else {
            if (normal_guard_cost != null) {
                Villages.GUARD_COST = normal_guard_cost;
            }
            if (normal_guard_upkeep != null) {
                Villages.GUARD_UPKEEP = normal_guard_upkeep;
            }
        }
        Villages.GUARD_COST_STRING = (new Change(Villages.GUARD_COST)).getChangeString();
        Villages.GUARD_UPKEEP_STRING = (new Change(Villages.GUARD_UPKEEP)).getChangeString();

        if (minimum_upkeep != null) {
            Villages.MINIMUM_UPKEEP = minimum_upkeep;
            Villages.MINIMUM_UPKEEP_STRING = (new Change(Villages.MINIMUM_UPKEEP)).getChangeString();
        }

        if (into_upkeep != null) {
            VillageFoundationQuestion.MINIMUM_LEFT_UPKEEP = into_upkeep;
        }

        if (name_change != null) {
            VillageFoundationQuestion.NAME_CHANGE_COST = name_change;
        }

        logValues();
    }

    File getFile () {
        return new File(Paths.get(ServerDirInfo.getFileDBPath(), "mods", "upkeepcosts", "upkeepcosts.properties").toUri());
    }

    void lateConfigure () {
        try {
            File file = getFile();
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                created = file.createNewFile();
            }

            if (created) {
                saveUpkeep();
                return;
            }

            FileInputStream stream = new FileInputStream(file.toString());
            Properties properties = new Properties();
            properties.load(stream);
            configure(properties);
        } catch (IOException ex) {
            logger.warning(messages.getString("load_properties_error"));
            ex.printStackTrace();
        }
    }

    void saveUpkeep () {
        File file = getFile();
        Properties properties = new Properties();

        try {
            boolean created = file.createNewFile();
            if (!created) {
                FileInputStream stream = new FileInputStream(file.toString());
                properties.load(stream);
            }
        } catch (IOException ex) {
            logger.warning(messages.getString("load_properties_error"));
            ex.printStackTrace();
        }
        for (Field field : this.getClass().getFields()) {
            if (!(field.getType().isAssignableFrom(Long.class))) {
                continue;
            }
            try {
                properties.setProperty(field.getName(), field.get(this).toString());
            } catch (IllegalAccessException ex) {
                logger.warning(messages.getString("error"));
                ex.printStackTrace();
            }
        }
        try (FileOutputStream stream = new FileOutputStream(file.toString())) {
            properties.store(stream, messages.getString("properties_comment"));
        } catch (IOException ex) {
            logger.warning(messages.getString("save_properties_error"));
            ex.printStackTrace();
        }
    }

    void negative (String property) {
        logger.warning(String.format("%s cannot be negative.  Value will not be changed.", property));
    }
    
    void invalid (String property) {
        logger.warning(String.format("Invalid value for %s.  Value will not be changed.", property));
    }

    void logValues () {
        logger.info(String.format("Upkeep costs are as follows: Tile %s, %s - Perimeter %s, %s - Guards %s, %s - Minimum %s - Into Upkeep %s - Name change %s",
                Villages.TILE_COST_STRING,
                Villages.TILE_UPKEEP_STRING,
                Villages.PERIMETER_COST_STRING,
                Villages.PERIMETER_UPKEEP_STRING,
                Villages.GUARD_COST_STRING,
                Villages.GUARD_UPKEEP_STRING,
                Villages.MINIMUM_UPKEEP_STRING,
                new Change(VillageFoundationQuestion.MINIMUM_LEFT_UPKEEP).getChangeString(),
                new Change(VillageFoundationQuestion.NAME_CHANGE_COST).getChangeString()));
    }
    
    
    void createOrPass () {
        if (!createdDb) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement("CREATE TABLE IF NOT EXISTS UPKEEP_BUFFER (" +
                                "VILLAGEID INT primary key UNIQUE," +
                                "BUFFER DOUBLE NOT NULL" +
                                ");"
                );
                ps.executeUpdate();
                ps = dbcon.prepareStatement("SELECT * FROM GUARDPLAN");
                rs = ps.executeQuery();

                while (rs.next()) {
                    ps = dbcon.prepareStatement("INSERT OR IGNORE INTO UPKEEP_BUFFER (VILLAGEID, BUFFER) VALUES(?, ?)");
                    ps.setInt(1, rs.getInt("VILLAGEID"));
                    ps.setDouble(2, 0.0D);
                    ps.executeUpdate();
                }

            } catch (SQLException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            } finally {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
            createdDb = true;
        }
    }
    
    @Override
    public void preInit() {
        // Note for Future - VillageFoundationQuestion.getFoundingCharge shows settlement form discount.
        // i.e. 10s for form, money goes into upkeep.
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass question = pool.getCtClass("com.wurmonline.server.questions.VillageFoundationQuestion");
            question.detach();
            pool.makeClass(UpkeepCosts.class.getResourceAsStream("VillageFoundationQuestion.class"));

            CtClass guardPlan = pool.get("com.wurmonline.server.villages.GuardPlan");
            guardPlan.getDeclaredMethod("getCostForGuards").setBody("return (long)$1 * com.wurmonline.server.villages.Villages.GUARD_UPKEEP;");
            CtField upkeepBufferField = new CtField(CtClass.doubleType, "upkeepBuffer", guardPlan);
            upkeepBufferField.setModifiers(Modifier.PUBLIC);
            guardPlan.addField(upkeepBufferField, "0.0D");
            CtMethod getVillageId = new CtMethod(CtPrimitiveType.intType, "getVillageId", null, guardPlan);
            getVillageId.setBody("{return this.villageId;}");
            getVillageId.setModifiers(Modifier.PUBLIC);
            guardPlan.addMethod(getVillageId);

            CtMethod getTimeLeft = guardPlan.getDeclaredMethod("getTimeLeft");
            getTimeLeft.insertAfter("if ($_ != 0L) {" +
                    "if ($_ == 29030400000L) {" +
                    "    return 29030400000L;" +
                    "} else {" +                    
                    "    return (long)((double)this.moneyLeft / this.calculateUpkeep(true) * 500000.0D);" +
                    "}}");

            CtMethod pollUpkeep = guardPlan.getDeclaredMethod("pollUpkeep");
            pollUpkeep.setBody("{try {" +
                    "            if(this.getVillage().isPermanent) {" +
                    "                return false;" +
                    "            }" +
                    "        } catch (com.wurmonline.server.villages.NoSuchVillageException var11) {" +
                    "            ;" +
                    "        }" +
                    "double upkeepD = this.calculateUpkeep(true);" +
                    "if (upkeepD < 1.0D) {" +
                    "    this.upkeepBuffer += upkeepD;" +
                    "}" +
                    "if (this.upkeepBuffer < 1.0D) {" +
                    "    return false;" +
                    "} else {" +
                    "    this.upkeepBuffer -= 1.0D;" +
                    "}" +
                    "        long upkeep = (long)upkeepD;" +
                    "        if(this.moneyLeft - upkeep <= 0L) {" +
                    "            try {" +
                    "                logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" disbanding. Money left=\" + this.moneyLeft + \", upkeep=\" + upkeep);" +
                    "            } catch (com.wurmonline.server.villages.NoSuchVillageException var6) {" +
                    "                logger.log(java.util.logging.Level.INFO, var6.getMessage(), var6);" +
                    "            }" +
                    "" +
                    "            return true;" +
                    "        } else {" +
                    "            if(upkeep >= 100L) {" +
                    "                try {" +
                    "                    logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" upkeep=\" + upkeep);" +
                    "                } catch (com.wurmonline.server.villages.NoSuchVillageException var10) {" +
                    "                    logger.log(java.util.logging.Level.INFO, var10.getMessage(), var10);" +
                    "                }" +
                    "            }" +
                    "" +
                    "            this.updateGuardPlan(this.type, this.moneyLeft - Math.max(1L, upkeep), this.hiredGuardNumber);" +
                    "            ++this.upkeepCounter;" +
                    "            if(this.upkeepCounter == 2) {" +
                    "                this.upkeepCounter = 0;" +
                    "                com.wurmonline.server.economy.Shop tl = com.wurmonline.server.economy.Economy.getEconomy().getKingsShop();" +
                    "                if(tl != null) {" +
                    "                    if(upkeep <= 1L) {" +
                    "                        tl.setMoney(tl.getMoney() + Math.max(1L, upkeep));" +
                    "                    } else {" +
                    "                        tl.setMoney(tl.getMoney() + upkeep);" +
                    "                    }" +
                    "                } else {" +
                    "                    logger.log(java.util.logging.Level.WARNING, \"No shop when \" + this.villageId + \" paying upkeep.\");" +
                    "                }" +
                    "            }" +
                    "" +
                    "            long var12 = this.getTimeLeft();" +
                    "            if(var12 < 3600000L) {" +
                    "                try {" +
                    "                    this.getVillage().broadCastAlert(\"The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.\");" +
                    "                    this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
                    "                } catch (com.wurmonline.server.villages.NoSuchVillageException var9) {" +
                    "                    logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var9);" +
                    "                }" +
                    "            } else if(var12 < 86400000L) {" +
                    "                if(System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
                    "                    this.lastSentWarning = System.currentTimeMillis();" +
                    "" +
                    "                    try {" +
                    "                        this.getVillage().broadCastAlert(\"The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.\");" +
                    "                        this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
                    "                    } catch (com.wurmonline.server.villages.NoSuchVillageException var8) {" +
                    "                        logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var8);" +
                    "                    }" +
                    "                }" +
                    "            } else if(var12 < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
                    "                this.lastSentWarning = System.currentTimeMillis();" +
                    "" +
                    "                try {" +
                    "                    this.getVillage().broadCastAlert(\"The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.\");" +
                    "                    this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
                    "                } catch (com.wurmonline.server.villages.NoSuchVillageException var7) {" +
                    "                    logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var7);" +
                    "                }" +
                    "            }" +
                    "            return false;" +
                    "        }}");

            HookManager.getInstance().registerHook("com.wurmonline.server.villages.DbGuardPlan", "load", "()V", () -> (proxy, method, args) -> {
                createOrPass();
                Field upkeepBuffer = proxy.getClass().getField("upkeepBuffer");
                Connection dbcon = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement("SELECT * FROM UPKEEP_BUFFER WHERE VILLAGEID=?");
                    ps.setInt(1, (Integer) proxy.getClass().getSuperclass().getDeclaredMethod("getVillageId").invoke(proxy));
                    rs = ps.executeQuery();
                    upkeepBuffer.set(proxy, rs.getDouble("BUFFER"));
                } catch (SQLException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                }
                return method.invoke(proxy, args);
            });

            HookManager.getInstance().registerHook("com.wurmonline.server.villages.DbGuardPlan", "create", "()V", () -> (proxy, method, args) -> {
                createOrPass();
                Connection dbcon = null;
                PreparedStatement ps = null;

                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement("INSERT INTO UPKEEP_BUFFER (VILLAGEID, BUFFER) VALUES(?,?)");
                    ps.setInt(1, (Integer)proxy.getClass().getSuperclass().getDeclaredMethod("getVillageId").invoke(proxy));
                    ps.setDouble(2, proxy.getClass().getField("upkeepBuffer").getDouble(proxy));
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                return method.invoke(proxy, args);
            });

            HookManager.getInstance().registerHook("com.wurmonline.server.villages.DbGuardPlan", "updateGuardPlan", "(IJI)V", () -> (proxy, method, args) -> {
                createOrPass();
                Field upkeepBuffer = proxy.getClass().getField("upkeepBuffer");
                Connection dbcon = null;
                PreparedStatement ps = null;

                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement("UPDATE UPKEEP_BUFFER SET BUFFER=? WHERE VILLAGEID=?");
                    ps.setDouble(1, upkeepBuffer.getDouble(proxy));
                    ps.setInt(2, (Integer)proxy.getClass().getSuperclass().getDeclaredMethod("getVillageId").invoke(proxy));
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                return method.invoke(proxy, args);
            });

            HookManager.getInstance().registerHook("com.wurmonline.server.villages.DbGuardPlan", "delete", "()V", () -> (proxy, method, args) -> {
                createOrPass();
                Connection dbcon = null;
                PreparedStatement ps = null;

                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement("DELETE FROM UPKEEP_BUFFER WHERE VILLAGEID=?");
                    ps.setInt(1, (Integer)proxy.getClass().getSuperclass().getDeclaredMethod("getVillageId").invoke(proxy));
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                return method.invoke(proxy, args);
            });
        } catch (NotFoundException | CannotCompileException | IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
