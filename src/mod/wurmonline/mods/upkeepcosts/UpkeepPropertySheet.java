package mod.wurmonline.mods.upkeepcosts;

import com.wurmonline.server.MiscConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mod.wurmonline.serverlauncher.LocaleHelper;
import org.controlsfx.control.PropertySheet;

import java.util.*;

public class UpkeepPropertySheet extends VBox implements MiscConstants {
    ObservableList<PropertySheet.Item> list;
    private Set<Enum> changedProperties = new HashSet<>();
    private ResourceBundle messages = LocaleHelper.getBundle("UpkeepCosts");

    public UpkeepPropertySheet(UpkeepCostsUI upkeep) {
        list = FXCollections.observableArrayList();
        list.add(new UpkeepItem(UpkeepPropertyType.TILE_COST, "", messages.getString("tile_cost"), messages.getString("tile_cost_description"), true, upkeep.tile_cost));
        list.add(new UpkeepItem(UpkeepPropertyType.TILE_UPKEEP, "", messages.getString("tile_upkeep"), messages.getString("tile_upkeep_description"), true, upkeep.tile_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.PERIMETER_COST, "", messages.getString("perimeter_cost"), messages.getString("perimeter_cost_description"), true, upkeep.perimeter_cost));
        list.add(new UpkeepItem(UpkeepPropertyType.PERIMETER_UPKEEP, "", messages.getString("perimeter_upkeep"), messages.getString("perimeter_upkeep_description"), true, upkeep.perimeter_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.NORMAL_GUARD_COST, "", messages.getString("normal_guard_cost"), messages.getString("normal_guard_cost_description"), true, upkeep.normal_guard_cost));
        list.add(new UpkeepItem(UpkeepPropertyType.NORMAL_GUARD_UPKEEP, "", messages.getString("normal_guard_upkeep"), messages.getString("normal_guard_upkeep_description"), true, upkeep.normal_guard_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.EPIC_GUARD_COST, "", messages.getString("epic_guard_cost"), messages.getString("epic_guard_cost_description"), true, upkeep.epic_guard_cost));
        list.add(new UpkeepItem(UpkeepPropertyType.EPIC_GUARD_UPKEEP, "", messages.getString("epic_guard_upkeep"), messages.getString("epic_guard_upkeep_description"), true, upkeep.epic_guard_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.MINIMUM_UPKEEP, "", messages.getString("minimum_upkeep"), messages.getString("minimum_upkeep_description"), true, upkeep.minimum_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.INTO_UPKEEP, "", messages.getString("into_upkeep"), messages.getString("into_upkeep_description"), true, upkeep.into_upkeep));
        list.add(new UpkeepItem(UpkeepPropertyType.NAME_CHANGE, "", messages.getString("name_change"), messages.getString("name_change_description"), true, upkeep.name_change));

        PropertySheet propertySheet = new PropertySheet(list);
        VBox.setVgrow(propertySheet, Priority.ALWAYS);
        this.getChildren().add(new Label(messages.getString("irons_note")));
        this.getChildren().add(propertySheet);
    }

    public boolean haveChanges () {
        return changedProperties.size() > 0;
    }

    public void clearChanges () { changedProperties.clear(); }

    public void setAllToChanged() {
        for (PropertySheet.Item item : list) {
            changedProperties.add(((UpkeepItem) item).getPropertyType());
        }
    }

    class UpkeepItem implements PropertySheet.Item {
        private UpkeepPropertyType type;
        private String category;
        private String name;
        private String description;
        private boolean editable = true;
        private Long value;

        UpkeepItem(UpkeepPropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Long aValue) {
            type = aType;
            category = aCategory;
            name = aName;
            description = aDescription;
            editable = aEditable;
            value = aValue;
        }

        public UpkeepPropertyType getPropertyType() {
            return type;
        }

        public Class<?> getType() {
            return value.getClass();
        }

        public String getCategory() {
            return category;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isEditable() {
            return editable;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object aValue) {
            if(!value.equals(aValue)) {
                changedProperties.add(type);
            }
            value = (Long)aValue;
        }
    }

    enum UpkeepPropertyType {
        TILE_COST,
        TILE_UPKEEP,
        PERIMETER_COST,
        PERIMETER_UPKEEP,
        NORMAL_GUARD_COST,
        NORMAL_GUARD_UPKEEP,
        EPIC_GUARD_COST,
        EPIC_GUARD_UPKEEP,
        MINIMUM_UPKEEP,
        NAME_CHANGE,
        INTO_UPKEEP
    }
}
