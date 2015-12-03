package mod.wurmonline.mods.upkeepcosts;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.villages.Villages;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Logger;

public class UpkeepCosts implements WurmMod, Configurable, ServerStartedListener {
    private static final Logger logger = Logger.getLogger(UpkeepCosts.class.getName());
    Long tile_upkeep;
    Long tile_cost;
    Long perimeter_cost;
    Long perimeter_upkeep;
    Long normal_guard_cost;
    Long normal_guard_upkeep;
    Long epic_guard_cost;
    Long epic_guard_upkeep;
    Long minimum_upkeep;

    @Override
    public void configure(Properties properties) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getName().equals("logger")) {
                continue;
            }
            try {
                field.set(this, Long.valueOf(properties.getProperty(field.getName())));
                if ((Long)field.get(this) < 0) {
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
        ServerEntry local = Servers.localServer;
        if (!local.isUpkeep()) {
            logger.info("Upkeep is not enabled on this server, upkeep costs will have no effect.");
        }
        if (!local.isFreeDeeds()) {
            logger.info("Deeds are free on this server, costs will have no effect.");
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

        logger.info(String.format("Upkeep costs are as follows: Tile %s, %s - Perimeter %s, %s - Guards %s, %s - Minimum %s",
                Villages.TILE_COST_STRING,
                Villages.TILE_UPKEEP_STRING,
                Villages.PERIMETER_COST_STRING,
                Villages.PERIMETER_UPKEEP_STRING,
                Villages.GUARD_COST_STRING,
                Villages.GUARD_UPKEEP_STRING,
                Villages.MINIMUM_UPKEEP_STRING));
    }

    void negative (String property) {
        logger.warning(String.format("%s cannot be negative.  Value will not be changed.", property));
    }
    
    void invalid (String property) {
        logger.warning(String.format("Invalid value for %s.  Value will not be changed.", property));
    }
}
