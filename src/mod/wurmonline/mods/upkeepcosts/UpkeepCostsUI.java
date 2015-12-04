package mod.wurmonline.mods.upkeepcosts;

import com.ibm.icu.text.MessageFormat;
import com.wurmonline.server.villages.Villages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import mod.wurmonline.serverlauncher.LocaleHelper;
import mod.wurmonline.serverlauncher.gui.ServerGuiController;
import org.gotti.wurmunlimited.modloader.interfaces.WurmUIMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Level;

public class UpkeepCostsUI extends UpkeepCosts implements WurmUIMod {
    ServerGuiController controller;
    UpkeepPropertySheet upkeepPropertySheet;
    boolean blockLateConfigure = false;

    public UpkeepCostsUI () {
        messages = LocaleHelper.getBundle("UpkeepCosts");
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

    void logValues () {
        logger.info(MessageFormat.format(messages.getString("all_values"),
                Villages.TILE_COST_STRING,
                Villages.TILE_UPKEEP_STRING,
                Villages.PERIMETER_COST_STRING,
                Villages.PERIMETER_UPKEEP_STRING,
                Villages.GUARD_COST_STRING,
                Villages.GUARD_UPKEEP_STRING,
                Villages.MINIMUM_UPKEEP_STRING));
    }

    @Override
    void lateConfigure () {
        if (!blockLateConfigure) {
            super.lateConfigure();
        }
    }

    @FXML
    ScrollPane container;

    @FXML
    void saveUpkeep () {
        if (upkeepPropertySheet.haveChanges()) {
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
                    field.set(this, upkeepPropertySheet.list.get(UpkeepPropertySheet.UpkeepPropertyType.valueOf(field.getName().toUpperCase()).ordinal()).getValue());
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

            if (created) {
                upkeepPropertySheet = new UpkeepPropertySheet(this);
                upkeepPropertySheet.setAllToChanged();
                saveUpkeep();
                container.setContent(upkeepPropertySheet);
                return;
            }

            FileInputStream stream = new FileInputStream(file.toString());
            Properties properties = new Properties();
            properties.load(stream);
            configure(properties);
            upkeepPropertySheet = new UpkeepPropertySheet(this);
            container.setContent(upkeepPropertySheet);
        } catch (IOException ex) {
            logger.warning(messages.getString("load_properties_error"));
            ex.printStackTrace();
        }
    }
}
