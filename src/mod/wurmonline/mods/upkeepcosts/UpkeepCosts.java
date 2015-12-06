package mod.wurmonline.mods.upkeepcosts;

import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.villages.Villages;
import javassist.*;
import javassist.bytecode.*;
import org.gotti.wurmunlimited.modloader.classhooks.CodeReplacer;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.LocalNameLookup;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;
import sun.invoke.util.BytecodeDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;
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
    ResourceBundle messages = ResourceBundle.getBundle("mod.wurmonline.mods.upkeepcosts.UpkeepCosts");;

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
            try {
                VillageFoundationQuestion.class.getField("MINIMUM_LEFT_UPKEEP").set(VillageFoundationQuestion.class, into_upkeep);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                ex.printStackTrace();
            }
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
            properties.store(stream, "");
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
        String into = "3 silver";
        try {
            into = new Change((Long)VillageFoundationQuestion.class.getField("MINIMUM_LEFT_UPKEEP").get(VillageFoundationQuestion.class)).getChangeString();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        logger.info(String.format("Upkeep costs are as follows: Tile %s, %s - Perimeter %s, %s - Guards %s, %s - Minimum %s - Into Upkeep %s",
                Villages.TILE_COST_STRING,
                Villages.TILE_UPKEEP_STRING,
                Villages.PERIMETER_COST_STRING,
                Villages.PERIMETER_UPKEEP_STRING,
                Villages.GUARD_COST_STRING,
                Villages.GUARD_UPKEEP_STRING,
                Villages.MINIMUM_UPKEEP_STRING,
                into));
    }

    @Override
    public void preInit() {
        // Note for Future - VillageFoundationQuestion.getFoundingCharge shows settlement form discount.
        // i.e. 10s for form, money goes into upkeep.
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass question = pool.getCtClass("com.wurmonline.server.questions.VillageFoundationQuestion");
            CtField leftUpkeep = question.getField("MINIMUM_LEFT_UPKEEP");
            leftUpkeep.setModifiers(Modifier.setPublic(Modifier.STATIC));

            CtMethod getFoundingCharge = question.getDeclaredMethod("getFoundingCharge");


            //LocalVariableAttribute va = (LocalVariableAttribute) getFoundingCharge.getMethodInfo().getAttribute("LocalAttributeTable");



            getFoundingCharge.setBody("return com.wurmonline.server.Servers.localServer.isFreeDeeds()?0L:this.getFoundingCost() + this.MINIMUM_LEFT_UPKEEP - (this.deed.getTemplateId() == 862?0L:100000L);");

            CtMethod parseQuestion5 = question.getDeclaredMethod("parseVillageFoundationQuestion5");
            parseQuestion5.insertAt(26, true, "long left = this.deed.getTemplateId() == 862?this.MINIMUM_LEFT_UPKEEP:0L;");
            CodeAttribute codeAttribute = parseQuestion5.getMethodInfo().getCodeAttribute();
            Bytecode bytecode = new Bytecode(codeAttribute.getConstPool());
            bytecode.addLdc2w(30000);
            byte[] search = bytecode.get();
            bytecode = new Bytecode(codeAttribute.getConstPool());
            bytecode.addGetstatic(question, "MINIMUM_LEFT_UPKEEP", Descriptor.of(CtClass.longType));

            byte[] replace = bytecode.get();
            new CodeReplacer(codeAttribute).replaceCode(search, replace);
            //parseQuestion5.insertAt(54, true, "com.wurmonline.server.villages.Villages.getVillage(this.villageName).plan.updateGuardPlan(0, this.MINIMUM_LEFT_UPKEEP, this.selectedGuards);");
            parseQuestion5.getMethodInfo().rebuildStackMap(pool);

            question.writeFile();
        } catch (NotFoundException | CannotCompileException | IOException | BadBytecode ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
