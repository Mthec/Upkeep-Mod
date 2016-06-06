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
    public long tile_cost;
    public long tile_upkeep;
    public long perimeter_cost;
    public long perimeter_upkeep;
    public long normal_guard_cost;
    public long normal_guard_upkeep;
    public long epic_guard_cost;
    public long epic_guard_upkeep;
    public long minimum_upkeep;
    public long into_upkeep;
    public long name_change;
    public long free_tiles;
    public long free_perimeter;
    public long min_drain;
    public float max_drain_modifier;
    public float drain_modifier_increment;
    ResourceBundle messages = ResourceBundle.getBundle("mod.wurmonline.mods.upkeepcosts.UpkeepCostsBundle");
    private boolean createdDb = false;
    boolean output = false;

    public UpkeepCosts() {
        setDefaults();
    }

    protected void setDefaults() {
        tile_cost = 100;
        tile_upkeep = 20;
        perimeter_cost = 50;
        perimeter_upkeep = 5;
        normal_guard_cost = 20000;
        normal_guard_upkeep = 10000;
        epic_guard_cost = 30000;
        epic_guard_upkeep = 30000;
        minimum_upkeep = 10000;
        into_upkeep = 30000;
        name_change = 50000;
        free_tiles = 0;
        free_perimeter = 0;
        min_drain = 7500;
        max_drain_modifier = 5.0F;
        drain_modifier_increment = 0.5F;
    }
    
    @Override
    public void configure(Properties properties) {
        for (Field field : this.getClass().getFields()) {
            try {
                if ((field.getType().isAssignableFrom(long.class))) {
                    String property = properties.getProperty(field.getName());
                    if (property == null || property.equals("")) {
                        continue;
                    }
                    long value = Long.valueOf(property);
                    if (value < 0) {
                        negative(field.getName());
                        continue;
                    }
                    field.set(this, value);
                }
                else if ((field.getType().isAssignableFrom(float.class))) {
                    String property = properties.getProperty(field.getName());
                    if (property == null || property.equals("")) {
                        continue;
                    }
                    float value = Float.valueOf(property);
                    if (value < 0.0F) {
                        negative(field.getName());
                        continue;
                    }
                    field.set(this, value);
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

        Villages.TILE_COST = tile_cost;
        Villages.TILE_COST_STRING = (new Change(Villages.TILE_COST)).getChangeString();

        Villages.TILE_UPKEEP = tile_upkeep;
        Villages.TILE_UPKEEP_STRING = (new Change(Villages.TILE_UPKEEP)).getChangeString();

        try {
            Villages.class.getDeclaredField("FREE_TILES").set(null, free_tiles);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("free_tiles_not_set"));
            ex.printStackTrace();
        }

        Villages.PERIMETER_COST = perimeter_cost;
        Villages.PERIMETER_COST_STRING = (new Change(Villages.PERIMETER_COST)).getChangeString();

        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        Villages.PERIMETER_UPKEEP_STRING = (new Change(Villages.PERIMETER_UPKEEP)).getChangeString();

        try {
            Villages.class.getDeclaredField("FREE_PERIMETER").set(null, free_perimeter);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("free_perimeter_not_set"));
            ex.printStackTrace();
        }

        if (local.isChallengeOrEpicServer()) {
            Villages.GUARD_COST = epic_guard_cost;
            Villages.GUARD_UPKEEP = epic_guard_upkeep;
        }
        else {
            Villages.GUARD_COST = normal_guard_cost;
            Villages.GUARD_UPKEEP = normal_guard_upkeep;
        }

        Villages.GUARD_COST_STRING = (new Change(Villages.GUARD_COST)).getChangeString();
        Villages.GUARD_UPKEEP_STRING = (new Change(Villages.GUARD_UPKEEP)).getChangeString();


        Villages.MINIMUM_UPKEEP = minimum_upkeep;
        Villages.MINIMUM_UPKEEP_STRING = (new Change(Villages.MINIMUM_UPKEEP)).getChangeString();

        VillageFoundationQuestion.MINIMUM_LEFT_UPKEEP = into_upkeep;

        VillageFoundationQuestion.NAME_CHANGE_COST = name_change;

        // Draining
        try {
            GuardPlan.class.getDeclaredField("minMoneyDrained").setLong(null, min_drain);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("min_drain_not_set"));
            ex.printStackTrace();
        }

        try {
            GuardPlan.class.getDeclaredField("maxDrainModifier").setFloat(null, max_drain_modifier);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("max_drain_modifier_not_set"));
            ex.printStackTrace();
        }

        try {
            GuardPlan.class.getDeclaredField("drainCumulateFigure").setFloat(null, drain_modifier_increment);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("drain_modifier_increment_not_set"));
            ex.printStackTrace();
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

    // TODO - How to prevent writing to intentionally blank values.
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
            if (!(field.getType().isAssignableFrom(long.class)) && !(field.getType().isAssignableFrom(float.class))) {
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
        String FREE_TILES = "?";
        try {
            FREE_TILES = String.valueOf(Villages.class.getDeclaredField("FREE_TILES").getLong(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String FREE_PERIMETER = "?";
        try {
            FREE_PERIMETER = String.valueOf(Villages.class.getDeclaredField("FREE_PERIMETER").getLong(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String minMoneyDrained = "?";
        try {
            minMoneyDrained = String.valueOf(GuardPlan.class.getDeclaredField("minMoneyDrained").getLong(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String maxDrainModifier = "?";
        try {
            maxDrainModifier = String.valueOf(GuardPlan.class.getDeclaredField("maxDrainModifier").getFloat(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String drainCumulateFigure = "?";
        try {
            drainCumulateFigure = String.valueOf(GuardPlan.class.getDeclaredField("drainCumulateFigure").getFloat(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }


        logger.info(String.format(messages.getString("all_values"),
                Villages.TILE_COST_STRING,
                Villages.TILE_UPKEEP_STRING,
                FREE_TILES,
                Villages.PERIMETER_COST_STRING,
                Villages.PERIMETER_UPKEEP_STRING,
                FREE_PERIMETER,
                Villages.GUARD_COST_STRING,
                Villages.GUARD_UPKEEP_STRING,
                Villages.MINIMUM_UPKEEP_STRING,
                new Change(VillageFoundationQuestion.MINIMUM_LEFT_UPKEEP).getChangeString(),
                new Change(VillageFoundationQuestion.NAME_CHANGE_COST).getChangeString(),
                new Change(Long.valueOf(minMoneyDrained)).getChangeString(),
                maxDrainModifier,
                drainCumulateFigure));
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
            CtClass question2 = pool.getCtClass("com.wurmonline.server.questions.VillageUpkeep");
            question2.detach();
            pool.makeClass(UpkeepCosts.class.getResourceAsStream("VillageUpkeep.class"));

            CtClass villages = pool.get("com.wurmonline.server.villages.Villages");
            CtField freeTiles = new CtField(CtClass.longType, "FREE_TILES", villages);
            freeTiles.setModifiers(Modifier.setPublic(Modifier.STATIC));
            villages.addField(freeTiles, "0L");
            CtField freePerimeter = new CtField(CtClass.longType, "FREE_PERIMETER", villages);
            freePerimeter.setModifiers(Modifier.setPublic(Modifier.STATIC));
            villages.addField(freePerimeter, "0L");


            CtClass guardPlan = pool.get("com.wurmonline.server.villages.GuardPlan");
            guardPlan.getDeclaredMethod("getCostForGuards").setBody(GuardPlanStrings.getCostForGuards);
            CtField upkeepBufferField = new CtField(CtClass.doubleType, "upkeepBuffer", guardPlan);
            upkeepBufferField.setModifiers(Modifier.PUBLIC);
            guardPlan.addField(upkeepBufferField, "0.0D");

            // Draining
            guardPlan.getDeclaredField("minMoneyDrained").setModifiers(Modifier.setPublic(Modifier.STATIC));
            CtMethod getMoneyDrained = guardPlan.getDeclaredMethod("getMoneyDrained");
            getMoneyDrained.setBody(GuardPlanStrings.getMoneyDrained);

            guardPlan.getDeclaredField("maxDrainModifier").setModifiers(Modifier.setPublic(Modifier.STATIC));
            guardPlan.getDeclaredField("drainCumulateFigure").setModifiers(Modifier.setPublic(Modifier.STATIC));
            CtMethod drainMoney = guardPlan.getDeclaredMethod("drainMoney");
            drainMoney.setBody(GuardPlanStrings.drainMoney);

            CtMethod getVillageId = new CtMethod(CtPrimitiveType.intType, "getVillageId", null, guardPlan);
            getVillageId.setBody("{return this.villageId;}");
            getVillageId.setModifiers(Modifier.PUBLIC);
            guardPlan.addMethod(getVillageId);
            CtField output = new CtField(CtClass.booleanType, "output", guardPlan);
            output.setModifiers(Modifier.setPublic(Modifier.STATIC));
            guardPlan.addField(output, CtField.Initializer.constant(false));

            CtMethod getTimeLeft = guardPlan.getDeclaredMethod("getTimeLeft");
            getTimeLeft.insertAfter(GuardPlanStrings.getTimeLeft);

            CtMethod getMonthlyCost = guardPlan.getDeclaredMethod("getMonthlyCost");
            getMonthlyCost.setBody(GuardPlanStrings.getMonthlyCost);

            CtMethod pollUpkeep = guardPlan.getDeclaredMethod("pollUpkeep");
            pollUpkeep.setBody(GuardPlanStrings.pollUpkeep);

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
                    ps.setInt(1, (Integer) proxy.getClass().getSuperclass().getDeclaredMethod("getVillageId").invoke(proxy));
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
