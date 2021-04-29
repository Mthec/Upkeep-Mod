package com.wurmonline.server.villages;

import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
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
                long tiles = (long)vill.getNumTiles() - (UpkeepCosts.free_tiles_upkeep ? 0 : UpkeepCosts.free_tiles);
                long cost = tiles > 0L ? tiles * Villages.TILE_UPKEEP : 0L;
                long perimeter = (long)vill.getPerimeterNonFreeTiles() - (UpkeepCosts.free_perimeter_upkeep ? 0 : UpkeepCosts.free_perimeter);
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
        if (guardPlan.calculateUpkeep(false) == 0 || (long)method.invoke(o, args) == 29030400000L)
            return 29030400000L;
        
        return (long)((guardPlan.moneyLeft - (double)ReflectionUtil.getPrivateField(guardPlan, GuardPlan.class.getDeclaredField("upkeepBuffer"))) / guardPlan.calculateUpkeep(false) * 500000.0D);
    }

    public static Object getCostForGuards(Object o, Method method, Object[] args) {
        int nonFreeGuards = (int)args[0];
        nonFreeGuards = Math.max(0, nonFreeGuards - (UpkeepCosts.free_guards_upkeep ? 0 : UpkeepCosts.free_guards));
        if (!UpkeepCosts.epic_guard_upkeep_scaling) {
            return nonFreeGuards * Villages.GUARD_UPKEEP;
        }
        return Servers.localServer.isChallengeOrEpicServer() ? (nonFreeGuards * Villages.GUARD_UPKEEP + (nonFreeGuards - 1L) * nonFreeGuards / 2 * 100 * 50) : nonFreeGuards * Villages.GUARD_UPKEEP;
    }

    public static Object pollUpkeep(Object o, Method method, Object[] args) throws NoSuchFieldException, IllegalAccessException {
        GuardPlan guardPlan = (GuardPlan)o;
        try {
            Village village = guardPlan.getVillage();

            if (village.isPermanent)
                return false;

            if (UpkeepCosts.upkeep_grace_period > 0) {
                long timeLeft = graceTimeRemaining(village);
                if (timeLeft > 0 && timeLeft <= TimeConstants.DAY_MILLIS) {
                    Field lastSentWarningField = GuardPlan.class.getDeclaredField("lastSentWarning");
                    long lastSentWarning = ReflectionUtil.getPrivateField(guardPlan, lastSentWarningField);
                    long now = System.currentTimeMillis();

                    if (now - lastSentWarning >= TimeConstants.HOUR_MILLIS) {
                        ReflectionUtil.setPrivateField(guardPlan, lastSentWarningField, now);
                        village.broadCastNormal("Your village upkeep grace period will run out soon.");
                    }
                }

                if (timeLeft >= 0) {
                    return false;
                }
            }
        } catch (NoSuchVillageException ignored) {
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
                        removeNonFreeTilesMessage(guardPlan.getVillage(), (byte)2);
                        guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var9) {
                        logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var9);
                    }
                } else if (tl < 86400000L) {
                    if (System.currentTimeMillis() - lastSentWarning > 3600000L) {
                        ReflectionUtil.setPrivateField(guardPlan, lastSentWarningField, System.currentTimeMillis());

                        try {
                            guardPlan.getVillage().broadCastAlert("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.", (byte)2);
                            removeNonFreeTilesMessage(guardPlan.getVillage(), (byte)2);
                            guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                        } catch (NoSuchVillageException var8) {
                            logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var8);
                        }
                    }
                } else if (tl < 604800000L && System.currentTimeMillis() - lastSentWarning > 3600000L) {
                    ReflectionUtil.setPrivateField(guardPlan, lastSentWarningField, System.currentTimeMillis());

                    try {
                        guardPlan.getVillage().broadCastAlert("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.", (byte)4);
                        removeNonFreeTilesMessage(guardPlan.getVillage(), (byte)4);
                        guardPlan.getVillage().broadCastAlert("Any traders who are citizens of " + guardPlan.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var7) {
                        logger.log(Level.WARNING, "No Village? " + guardPlan.villageId, var7);
                    }
                }

                return false;
            }
        }
    }

    public static long graceTimeRemaining(Village village) {
        int grace = UpkeepCosts.upkeep_grace_period;
        if (grace > 0) {
            return (village.creationDate + (grace * TimeConstants.DAY_MILLIS)) - System.currentTimeMillis();
        }
        return -1;
    }

    public static void addGraceTimeRemaining(StringBuilder sb, long grace) {
        long remainingDays = grace / TimeConstants.DAY_MILLIS;
        sb.append("text{text=\"This village is still in it's grace period, it will start paying upkeep in ");
        if (remainingDays > 0) {
            sb.append(remainingDays == 1 ? "1 day" : remainingDays + " days");
        } else {
            long remainingHours = grace / TimeConstants.HOUR_MILLIS;
            if (remainingHours == 0)
                sb.append("less than an hour");
            else
                sb.append(remainingHours == 1 ? "1 hour" : remainingHours + " hours");
        }
        sb.append(".\"}");
    }
    
    private static void removeNonFreeTilesMessage(Village village, byte messageType) {
        StringBuilder sb = new StringBuilder("Or you may resize to remove any non-free tiles.  You can have up to ");
        boolean freeTiles = false;
        if (!UpkeepCosts.free_tiles_upkeep && UpkeepCosts.free_tiles > 0) {
            freeTiles = true;
            sb.append(UpkeepCosts.free_tiles).append(" free tiles");
        }

        boolean freePerimeter = false;
        if (!UpkeepCosts.free_perimeter_upkeep && UpkeepCosts.free_perimeter > 0) {
            if (freeTiles)
                sb.append(" and ");

            freePerimeter = true;
            sb.append(UpkeepCosts.free_perimeter).append(" free perimeter tiles");
        }

        if (freeTiles || freePerimeter)
            village.broadCastAlert(sb.append(".").toString(), messageType);
    }
}
