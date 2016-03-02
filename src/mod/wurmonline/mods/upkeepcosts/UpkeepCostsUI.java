package mod.wurmonline.mods.upkeepcosts;

import com.ibm.icu.text.MessageFormat;
import com.wurmonline.server.villages.GuardPlan;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import mod.wurmonline.serverlauncher.LocaleHelper;
import mod.wurmonline.serverlauncher.ServerController;
import mod.wurmonline.serverlauncher.gui.ServerGuiController;
import org.gotti.wurmunlimited.modloader.interfaces.WurmArgsMod;
import org.gotti.wurmunlimited.modloader.interfaces.WurmUIMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

public class UpkeepCostsUI extends UpkeepCosts implements WurmUIMod, WurmArgsMod {
    ServerGuiController controller;
    UpkeepPropertySheet upkeepPropertySheet;
    boolean blockLateConfigure = false;

    public UpkeepCostsUI () {
        this.messages = LocaleHelper.getBundle("UpkeepCosts");
    }

    @Override
    public Region getRegion(ServerGuiController guiController) {
        controller = guiController;
        blockLateConfigure = true;
        try {
            FXMLLoader fx = new FXMLLoader(UpkeepCostsUI.class.getResource("UpkeepCosts.fxml"), LocaleHelper.getBundle("UIWindow"));
            fx.setClassLoader(this.getClass().getClassLoader());
            fx.setControllerFactory(param -> this);
            return fx.load();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        Label label = new Label(messages.getString("fxml_missing"));
        Pane pane = new Pane();
        pane.getChildren().add(label);
        return pane;
    }

    @Override
    public String getName() {
        return messages.getString("mod_name");
    }

    void negative (String property) {
        logger.warning(MessageFormat.format(messages.getString("negative"), property));
    }

    void invalid (String property) {
        logger.warning(MessageFormat.format(messages.getString("invalid"), property));
    }

    @Override
    void lateConfigure () {
        if (!blockLateConfigure) {
            super.lateConfigure();
        }
        try {
            GuardPlan.class.getDeclaredField("output").setBoolean(GuardPlan.class, output);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    @FXML
    ScrollPane container;

    @FXML
    @Override
    void saveUpkeep () {
        if (upkeepPropertySheet.haveChanges()) {
            for (Field field : this.getClass().getFields()) {
                if (!(field.getType().isAssignableFrom(Long.class))) {
                    continue;
                }
                try {
                    field.set(this, upkeepPropertySheet.list.get(UpkeepPropertySheet.UpkeepPropertyType.valueOf(field.getName().toUpperCase()).ordinal()).getValue());
                } catch (IllegalAccessException ex) {
                    logger.warning(messages.getString("error"));
                    ex.printStackTrace();
                }
            }
            super.saveUpkeep();
            upkeepPropertySheet.clearChanges();
            if (controller.serverIsRunning()) {
                onServerStarted();
            }
        }
    }

    ButtonType saveCheck() {
        if (upkeepPropertySheet != null && upkeepPropertySheet.haveChanges()) {
            ButtonType result = controller.showYesNoCancel(messages.getString("changes_title"), messages.getString("changes_header"), messages.getString("changes_message")).get();
            if (result == ButtonType.YES) {
                saveUpkeep();
            }
            return result;
        }
        return new ButtonType("", ButtonBar.ButtonData.NO);
    }

    @FXML
    void initialize () {
        ButtonType check = saveCheck();
        if (check == ButtonType.CANCEL) {
            return;
        }

        try {
            File file = getFile();
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                created = file.createNewFile();
            }

            FileInputStream stream;
            Properties properties = new Properties();

            if (created) {
                stream = new FileInputStream(Paths.get("mods", "upkeepcosts.properties").toString());
                properties.load(stream);
                configure(properties);
                upkeepPropertySheet = new UpkeepPropertySheet(this);
                upkeepPropertySheet.setAllToChanged();
                saveUpkeep();
                container.setContent(upkeepPropertySheet);
            }
            else {
                stream = new FileInputStream(file.toString());
                properties.load(stream);
                configure(properties);
                upkeepPropertySheet = new UpkeepPropertySheet(this);
                container.setContent(upkeepPropertySheet);
            }
        } catch (IOException ex) {
            logger.warning(messages.getString("load_properties_error"));
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getArgs() {
        Set<String> set = new HashSet<>();
        set.add("upkeep_output");
        return set;
    }

    @Override
    public void parseArgs(ServerController controller) {
        String value = controller.arguments.getOptionValue("upkeep_output");
        output = value != null && value.equals("true");
    }
}
