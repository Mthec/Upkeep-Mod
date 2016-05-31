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
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

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

public class UpkeepCosts implements WurmServerMod, Configurable, PreInitable, ServerStartedListener {
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
            Villages.class.getDeclaredField("FREE_TILES").set(Villages.class, free_tiles);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("free_tiles_not_set"));
            ex.printStackTrace();
        }

        Villages.PERIMETER_COST = perimeter_cost;
        Villages.PERIMETER_COST_STRING = (new Change(Villages.PERIMETER_COST)).getChangeString();

        Villages.PERIMETER_UPKEEP = perimeter_upkeep;
        Villages.PERIMETER_UPKEEP_STRING = (new Change(Villages.PERIMETER_UPKEEP)).getChangeString();

        try {
            Villages.class.getDeclaredField("FREE_PERIMETER").set(Villages.class, free_perimeter);
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
            GuardPlan.class.getDeclaredField("minMoneyDrained").setLong(GuardPlan.class, min_drain);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("min_drain_not_set"));
            ex.printStackTrace();
        }

        try {
            GuardPlan.class.getDeclaredField("maxDrainModifier").setFloat(GuardPlan.class, max_drain_modifier);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.warning(messages.getString("max_drain_modifier_not_set"));
            ex.printStackTrace();
        }

        try {
            GuardPlan.class.getDeclaredField("drainCumulateFigure").setFloat(GuardPlan.class, drain_modifier_increment);
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
            FREE_TILES = String.valueOf(Villages.class.getDeclaredField("FREE_TILES").getLong(Villages.class));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String FREE_PERIMETER = "?";
        try {
            FREE_PERIMETER = String.valueOf(Villages.class.getDeclaredField("FREE_PERIMETER").getLong(Villages.class));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String minMoneyDrained = "?";
        try {
            minMoneyDrained = String.valueOf(GuardPlan.class.getDeclaredField("minMoneyDrained").getLong(Villages.class));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String maxDrainModifier = "?";
        try {
            maxDrainModifier = String.valueOf(GuardPlan.class.getDeclaredField("maxDrainModifier").getFloat(GuardPlan.class));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        String drainCumulateFigure = "?";
        try {
            drainCumulateFigure = String.valueOf(GuardPlan.class.getDeclaredField("drainCumulateFigure").getFloat(GuardPlan.class));
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
            guardPlan.getDeclaredMethod("getCostForGuards").setBody("return (long)$1 * com.wurmonline.server.villages.Villages.GUARD_UPKEEP;");
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
            drainMoney.setBody(
                    "{long moneyToDrain = this.getMoneyDrained();\n" +
                    "this.drainGuardPlan(this.moneyLeft - moneyToDrain);\n" +
                    "this.drainModifier = Math.min(com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"maxDrainModifier\").getFloat(com.wurmonline.server.villages.GuardPlan.class), com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"drainCumulateFigure\").getFloat(com.wurmonline.server.villages.GuardPlan.class) + this.drainModifier);\n" +
                    "this.saveDrainMod();\n" +
                    "return moneyToDrain;\n" +
                    "}");

            CtMethod getVillageId = new CtMethod(CtPrimitiveType.intType, "getVillageId", null, guardPlan);
            getVillageId.setBody("{return this.villageId;}");
            getVillageId.setModifiers(Modifier.PUBLIC);
            guardPlan.addMethod(getVillageId);
            CtField output = new CtField(CtClass.booleanType, "output", guardPlan);
            output.setModifiers(Modifier.setPublic(Modifier.STATIC));
            guardPlan.addField(output, CtField.Initializer.constant(false));

            CtMethod getTimeLeft = guardPlan.getDeclaredMethod("getTimeLeft");
            getTimeLeft.insertAfter("if ($_ != 0L) {" +
                    "if ($_ == 29030400000L) {" +
                    "    return 29030400000L;" +
                    "} else {" +                    
                    "    return (long)((double)this.moneyLeft / this.calculateUpkeep(true) * 500000.0D);" +
                    "}}");

            CtMethod getMonthlyCost = guardPlan.getDeclaredMethod("getMonthlyCost");
            getMonthlyCost.setBody("if(!com.wurmonline.server.Servers.localServer.isUpkeep()) {\n" +
                    "    return 0L;\n" +
                    "} else {\n" +
                    "    try {\n" +
                    "        com.wurmonline.server.villages.Village sv = this.getVillage();\n" +
                    "        long tiles = (long)sv.getNumTiles() - com.wurmonline.server.villages.Villages.FREE_TILES;" +
                    "        long cost = (tiles > 0L ? tiles : 0L) * com.wurmonline.server.villages.Villages.TILE_UPKEEP;\n" +
                    "        long perimeter = (long)sv.getPerimeterNonFreeTiles() - com.wurmonline.server.villages.Villages.FREE_PERIMETER;" +
                    "        cost += (perimeter > 0L ? perimeter : 0L) * com.wurmonline.server.villages.Villages.PERIMETER_UPKEEP;\n" +
                    "        cost += getCostForGuards(this.hiredGuardNumber);\n" +
                    "        if(sv.isCapital()) {\n" +
                    "            cost = (long)((float)cost * 0.5F);\n" +
                    "        }\n" +
                    "\n" +
                    "        if(sv.hasToomanyCitizens()) {\n" +
                    "            cost *= 2L;\n" +
                    "        }\n" +
                    "\n" +
                    "        return Math.max(com.wurmonline.server.villages.Villages.MINIMUM_UPKEEP, cost);\n" +
                    "    } catch (com.wurmonline.server.villages.NoSuchVillageException var4) {\n" +
                    "        logger.log(java.util.logging.Level.WARNING, \"Guardplan for village \" + this.villageId + \": Village not found. Deleting.\", var4);\n" +
                    "        this.delete();\n" +
                    "        return 10000L;\n" +
                    "    }\n" +
                    "}");

            CtMethod pollUpkeep = guardPlan.getDeclaredMethod("pollUpkeep");
            pollUpkeep.setBody("{try {" +
                    "            if(this.getVillage().isPermanent) {" +
                    "                return false;" +
                    "            }" +
                    "        } catch (com.wurmonline.server.villages.NoSuchVillageException var11) {" +
                    "            ;" +
                    "        }" +
                    "double upkeepD = this.calculateUpkeep(true);" +
                    "if (upkeepD < 0.0D) {" +
                    "    logger.severe(\"Why is upkeep less than 0.0?\");" +
                    "}" +
                    "if (upkeepD < 1.0D) {" +
                    "    this.upkeepBuffer += upkeepD;" +
                    "    upkeepD = 0.0D;" +
                    "}" +
                    "while (this.upkeepBuffer >= 1.0D) {" +
                    "    this.upkeepBuffer -= 1.0D;" +
                    "    upkeepD += 1.0D;" +
                    "}" +
                    "if (this.output) {" +
                    "    System.out.println(\"Village upkeep - \" + this.getVillage().getName() + \" paid \" + Double.toString(upkeepD) + \" this turn.  Upkeep buffer is now \" + Double.toString(this.upkeepBuffer));" +
                    "}" +
                    "if (upkeepD == 0.0D) {" +
                    "    return false;" +
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
                    "                    this.getVillage().broadCastAlert(\"The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.\", (byte)2);" +
                    "                    this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
                    "                } catch (com.wurmonline.server.villages.NoSuchVillageException var9) {" +
                    "                    logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var9);" +
                    "                }" +
                    "            } else if(var12 < 86400000L) {" +
                    "                if(System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
                    "                    this.lastSentWarning = System.currentTimeMillis();" +
                    "" +
                    "                    try {" +
                    "                        this.getVillage().broadCastAlert(\"The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.\", (byte)2);" +
                    "                        this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
                    "                    } catch (com.wurmonline.server.villages.NoSuchVillageException var8) {" +
                    "                        logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var8);" +
                    "                    }" +
                    "                }" +
                    "            } else if(var12 < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
                    "                this.lastSentWarning = System.currentTimeMillis();" +
                    "" +
                    "                try {" +
                    "                    this.getVillage().broadCastAlert(\"The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.\", (byte)4);" +
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
