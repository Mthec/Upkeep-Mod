package com.wurmonline.server.villages;

import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuardPlanMethods {
    private static final Logger logger = Logger.getLogger(GuardPlanMethods.class.getName());

    public static Object getMoneyDrained(Object o, Method method, Object[] args) {
        GuardPlan guardPlan = (GuardPlan)o;
        try {
            if (guardPlan.getVillage().isPermanent) {
                return 0L;
            }
        } catch (NoSuchVillageException var2) {
            logger.log(Level.WARNING, guardPlan.villageId + ", " + var2.getMessage(), var2);
            return 0L;
        }

        return (long)Math.min((float)guardPlan.moneyLeft, (1.0F + guardPlan.drainModifier) * Math.max(UpkeepCosts.min_drain, (float)guardPlan.getMonthlyCost() * 0.15F));
    }

    public static Object drainMoney(Object o, Method method, Object[] args) {
        GuardPlan guardPlan = (GuardPlan)o;
        long moneyToDrain = guardPlan.getMoneyDrained();
        guardPlan.drainGuardPlan(guardPlan.moneyLeft - moneyToDrain);
        guardPlan.drainModifier = Math.min(UpkeepCosts.max_drain_modifier, UpkeepCosts.drain_modifier_increment + guardPlan.drainModifier);
        guardPlan.saveDrainMod();
        return moneyToDrain;
    }

    public static Object getMonthlyCost(Object o, Method method, Object[] args) {
        if (!Servers.localServer.isUpkeep()) {
            return 0L;
        } else {
            GuardPlan guardPlan = (GuardPlan)o;
            try {
                Village vill = guardPlan.getVillage();
                long tiles = (long)vill.getNumTiles() - UpkeepCosts.free_tiles;
                long cost = tiles > 0L ? tiles * Villages.TILE_UPKEEP : 0L;
                long perimeter = (long)vill.getPerimeterNonFreeTiles() - UpkeepCosts.free_perimeter;
                cost += perimeter > 0L ? perimeter * com.wurmonline.server.villages.Villages.PERIMETER_UPKEEP : 0L;
                cost += GuardPlan.getCostForGuards(guardPlan.hiredGuardNumber);
                if (vill.isCapital()) {
                    cost = (long)((float)cost * 0.5F);
                }

                if (vill.hasToomanyCitizens()) {
                    cost *= 2L;
                }

                return Math.max(Villages.MINIMUM_UPKEEP, cost);
            } catch (NoSuchVillageException var4) {
                logger.log(Level.WARNING, "Guardplan for village " + guardPlan.villageId + ": Village not found. Deleting.", var4);
                guardPlan.delete();
                return 10000L;
            }
        }
    }
    
    public static Object getTimeLeft(Object o, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        GuardPlan guardPlan = (GuardPlan)o;
        long toReturn = (long)method.invoke(o, args);
        if (toReturn == 29030400000L)
            return toReturn;
        
        return (long)((guardPlan.moneyLeft - (double)ReflectionUtil.getPrivateField(guardPlan, GuardPlan.class.getDeclaredField("upkeepBuffer"))) / guardPlan.calculateUpkeep(false) * 500000.0D);
    }

    public static Object getCostForGuards(Object o, Method method, Object[] args) {
        int nonFreeGuards = (int)args[0];
        nonFreeGuards = Math.max(0, nonFreeGuards - UpkeepCosts.free_guards);
        if (!UpkeepCosts.epic_guard_upkeep_scaling) {
            return nonFreeGuards * Villages.GUARD_UPKEEP;
        }
        return Servers.localServer.isChallengeOrEpicServer() ? (nonFreeGuards * Villages.GUARD_UPKEEP + (nonFreeGuards - 1) * nonFreeGuards / 2 * 100 * 50) : nonFreeGuards * Villages.GUARD_UPKEEP;
    }

    public static Object pollUpkeep(Object o, Method method, Object[] args) throws NoSuchFieldException, IllegalAccessException {
        GuardPlan guardPlan = (GuardPlan)o;
        try {
            if (guardPlan.getVillage().isPermanent) {
                return false;
            }
        } catch (NoSuchVillageException var11) {
        }

        if (!Servers.localServer.isUpkeep()) {
            return false;
        } else {
            double upkeepD = guardPlan.calculateUpkeep(true);
            Field upkeepBufferField = GuardPlan.class.getDeclaredField("upkeepBuffer");
            double upkeepBuffer = upkeepBufferField.getDouble(guardPlan);
            upkeepBuffer += upkeepD % 1;
            upkeepD -= upkeepD % 1;
            while (upkeepBuffer >= 1.0D) {
                upkeepBuffer -= 1.0D;
                upkeepD += 1.0D;
            }
            upkeepBufferField.setDouble(guardPlan, upkeepBuffer);
            if (UpkeepCosts.output) {
                try {
                    System.out.println("Village upkeep - " + guardPlan.getVillage().getName() + " paid " + upkeepD + " this turn.  Upkeep buffer is now " + upkeepBuffer);
                } catch (NoSuchVillageException e) {
                    logger.warning("guardPlan does not have village.");
                    e.printStackTrace();
                }
            }

            long upkeep = (long)upkeepD;
            if (upkeep != 0 && guardPlan.moneyLeft - upkeep <= 0L) {
                try {
                    logger.log(Level.INFO, guardPlan.getVillage().getName() + " disbanding. Money left=" + guardPlan.moneyLeft + ", upkeep=" + upkeep);
                } catch (NoSuchVillageException var6) {
                    logger.log(Level.INFO, var6.getMessage(), var6);
                }

                return true;
            } else {
                if (upkeep >= 100L) {
                    try {
                        logger.log(Level.INFO, guardPlan.getVillage().getName() + " upkeep=" + upkeep);
                    } catch (NoSuchVillageException var10) {
                        logger.log(Level.INFO, var10.getMessage(), var10);
                    }
                }

                guardPlan.updateGuardPlan(guardPlan.type, guardPlan.moneyLeft - upkeep, guardPlan.hiredGuardNumber);
                Field upkeepCounterField = GuardPlan.class.getDeclaredField("upkeepCounter");
                upkeepCounterField.setAccessible(true);

                if (upkeepCounterField.getInt(guardPlan) == 1) {
                    upkeepCounterField.setInt(guardPlan, 0);
                    Shop shop = Economy.getEconomy().getKingsShop();
                    if (shop != null) {
                        shop.setMoney(shop.getMoney() + upkeep);
                    } else {
                        logger.log(Level.WARNING, "No shop when " + guardPlan.villageId + " paying upkeep.");
                    }
                } else
                    upkeepCounterField.setInt(guardPlan, 1);

                long tl = guardPlan.getTimeLeft();
                Field lastSentWarningField = GuardPlan.class.getDeclaredField("lastSentWarning");
                long lastSentWarning = ReflectionUtil.getPrivateField(guardPlan, lastSentWarningField);
                if (tl < 3600000L) {
                    try {
                        guardPlan.getVillage().broadCastAlert("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.", (byte)2);
                        if (UpkeepCosts.free_tiles > 0 || UpkeepCosts.free_perimeter > 0)
                            guardPlan.getVillage().broadCastAlert("Or you may resize to remove any non-free tiles.  You can have up to " + UpkeepCosts.free_tiles + " free tiles and " + UpkeepCosts.free_perimeter + " free perimeter tiles.", (byte)2);
                        guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var9) {
                        logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var9);
                    }
                } else if (tl < 86400000L) {
                    if (System.currentTimeMillis() - lastSentWarning > 3600000L) {
                        ReflectionUtil.setPrivateField(guardPlan, lastSentWarningField, System.currentTimeMillis());

                        try {
                            guardPlan.getVillage().broadCastAlert("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.", (byte)2);
                            if (UpkeepCosts.free_tiles > 0 || UpkeepCosts.free_perimeter > 0)
                                guardPlan.getVillage().broadCastAlert("Or you may resize to remove any non-free tiles.  You can have up to " + UpkeepCosts.free_tiles + " free tiles and " + UpkeepCosts.free_perimeter + " free perimeter tiles.", (byte)2);
                            guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                        } catch (NoSuchVillageException var8) {
                            logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var8);
                        }
                    }
                } else if (tl < 604800000L && System.currentTimeMillis() - lastSentWarning > 3600000L) {
                    ReflectionUtil.setPrivateField(guardPlan, lastSentWarningField, System.currentTimeMillis());

                    try {
                        guardPlan.getVillage().broadCastAlert("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.", (byte)4);
                        if (UpkeepCosts.free_tiles > 0 || UpkeepCosts.free_perimeter > 0)
                            guardPlan.getVillage().broadCastAlert("You may resize to remove any non-free tiles.  You can have up to " + UpkeepCosts.free_tiles + " free tiles and " + UpkeepCosts.free_perimeter + " free perimeter tiles.", (byte)4);
                        guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var7) {
                        logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var7);
                    }
                }

                return false;
            }
        }
    }
}
