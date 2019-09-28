//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.wurmonline.server.villages;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.*;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GuardPlan implements CreatureTemplateIds, TimeConstants, MiscConstants, MonetaryConstants {
    public static final int GUARD_PLAN_NONE = 0;
    public static final int GUARD_PLAN_LIGHT = 1;
    public static final int GUARD_PLAN_MEDIUM = 2;
    public static final int GUARD_PLAN_HEAVY = 3;
    final LinkedList<Creature> freeGuards = new LinkedList();
    private static final Logger logger = Logger.getLogger(GuardPlan.class.getName());
    public int type = 0;
    final int villageId;
    long lastChangedPlan;
    public long moneyLeft;
    private int siegeCount = 0;
    private int waveCounter = 0;
    private long lastSentWarning = 0L;
    private static final long polltime = 500000L;
    long lastDrained = 0L;
    float drainModifier = 0.0F;
    private static float maxDrainModifier = 5.0F;
    private static float drainCumulateFigure = 0.5F;
    private int upkeepCounter = 0;
    int hiredGuardNumber = 0;
    private static final int maxGuards;
    private static long minMoneyDrained = 7500L;

    // Custom
    public double upkeepBuffer = 0.0D;
    public int getVillageId() {
        return villageId;
    }

    GuardPlan(int aType, int aVillageId) {
        this.type = aType;
        this.villageId = aVillageId;
        this.create();
    }

    GuardPlan(int aVillageId) {
        this.villageId = aVillageId;
        this.load();
    }

    final Village getVillage() throws NoSuchVillageException {
        return Villages.getVillage(this.villageId);
    }

    public final String getName() {
        if (this.type == 3) {
            return "Heavy";
        } else if (this.type == 1) {
            return "Light";
        } else {
            return this.type == 2 ? "Medium" : "None";
        }
    }

    public final long getTimeLeft() {
        try {
            if (this.getVillage().isPermanent || !Servers.localServer.isUpkeep()) {
                return 29030400000L;
            }
        } catch (NoSuchVillageException var2) {
            logger.log(Level.WARNING, this.villageId + ", " + var2.getMessage(), var2);
        }

        return (long)((double)this.moneyLeft / Math.max(1.0D, this.calculateUpkeep(false)) * 500000.0D);
    }

    public double calculateUpkeep(boolean calculateFraction) {
        long monthlyCost = this.getMonthlyCost();
        double upkeep = (double)monthlyCost * 2.0667989417989417E-4D;
        return upkeep;
    }

    public final long getMoneyLeft() {
        return this.moneyLeft;
    }

    public static final long getCostForGuards(int numGuards) {
        return Servers.localServer.isChallengeOrEpicServer() ? (long)(numGuards * 10000 + (numGuards - 1) * numGuards / 2 * 100 * 50) : (long)numGuards * Villages.GUARD_UPKEEP;
    }

    public long getMonthlyCost() {
        if (!Servers.localServer.isUpkeep()) {
            return 0L;
        } else {
            try {
                Village vill = this.getVillage();
                long cost = (long)vill.getNumTiles() * Villages.TILE_UPKEEP;
                cost += (long)vill.getPerimeterNonFreeTiles() * Villages.PERIMETER_UPKEEP;
                cost += getCostForGuards(this.hiredGuardNumber);
                if (vill.isCapital()) {
                    cost = (long)((float)cost * 0.5F);
                }

                if (vill.hasToomanyCitizens()) {
                    cost *= 2L;
                }

                return Math.max(Villages.MINIMUM_UPKEEP, cost);
            } catch (NoSuchVillageException var4) {
                logger.log(Level.WARNING, "Guardplan for village " + this.villageId + ": Village not found. Deleting.", var4);
                this.delete();
                return 10000L;
            }
        }
    }

    public final boolean mayRaiseUpkeep() {
        return System.currentTimeMillis() - this.lastChangedPlan > 604800000L;
    }

    public final boolean mayLowerUpkeep() {
        return true;
    }

    public final long calculateUpkeepTimeforType(int upkeeptype) {
        int origType = this.type;
        this.type = upkeeptype;
        long timeleft = this.getTimeLeft();
        this.type = origType;
        return timeleft;
    }

    public final long calculateMonthlyUpkeepTimeforType(int upkeeptype) {
        int origType = this.type;
        this.type = upkeeptype;
        long cost = this.getMonthlyCost();
        this.type = origType;
        return cost;
    }

    protected long getDisbandMoneyLeft() {
        return this.moneyLeft;
    }

    private void pollGuards() {
        Village village;
        byte templateId;
        int x;
        Creature toReturn;
        Creature newc;
        if (this.type != 0) {
            try {
                village = this.getVillage();
                int _maxGuards = this.getConvertedGuardNumber(village);
                Guard[] guards = village.getGuards();
                if (guards.length < _maxGuards) {
                    try {
                        Item villToken = village.getToken();
                        templateId = 0;
                        if (Server.rand.nextInt(2) == 0) {
                            templateId = 1;
                        }

                        templateId = 32;
                        if (Kingdoms.getKingdomTemplateFor(village.kingdom) == 3) {
                            templateId = 33;
                        }

                        for(x = 0; x < Math.min(this.siegeCount + 1, _maxGuards - guards.length); ++x) {
                            try {
                                if (this.freeGuards.isEmpty()) {
                                    toReturn = Creature.doNew(templateId, villToken.getPosX(), villToken.getPosY(), (float)Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", templateId, village.kingdom);
                                    village.createGuard(toReturn, System.currentTimeMillis());
                                } else {
                                    toReturn = (Creature)this.freeGuards.removeFirst();
                                    if (toReturn.getTemplate().getTemplateId() != templateId) {
                                        this.removeReturnedGuard(toReturn.getWurmId());
                                        toReturn.destroy();
                                        newc = Creature.doNew(templateId, villToken.getPosX(), villToken.getPosY(), (float)Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", templateId, village.kingdom);
                                        village.createGuard(newc, System.currentTimeMillis());
                                    } else {
                                        village.createGuard(toReturn, System.currentTimeMillis());
                                        this.removeReturnedGuard(toReturn.getWurmId());
                                        this.putGuardInWorld(toReturn);
                                    }
                                }
                            } catch (Exception var11) {
                                logger.log(Level.WARNING, var11.getMessage(), var11);
                            }
                        }
                    } catch (NoSuchItemException var14) {
                        logger.log(Level.WARNING, "Village " + village.getName() + " has no token.");
                    }

                    if (this.siegeCount > 0) {
                        this.siegeCount += 3;
                    }
                }

                village.checkForEnemies();
            } catch (NoSuchVillageException var15) {
                logger.log(Level.WARNING, "No village for guardplan with villageid " + this.villageId, var15);
            }
        } else {
            try {
                village = this.getVillage();
                Guard[] guards = village.getGuards();
                if (guards.length < this.hiredGuardNumber && (this.hiredGuardNumber <= 10 || guards.length <= 10 || this.siegeCount == 0)) {
                    try {
                        Item villToken = village.getToken();
                        if (Feature.TOWER_CHAINING.isEnabled() && !villToken.isChained()) {
                            ++this.waveCounter;
                            if (this.waveCounter % 3 != 0) {
                                return;
                            }
                        }

                        byte sex = 0;
                        if (Server.rand.nextInt(2) == 0) {
                            sex = 1;
                        }

                        templateId = 32;
                        if (village.kingdom == 3) {
                            templateId = 33;
                        }

                        int minguards = Math.max(1, this.hiredGuardNumber / 10);

                        for(x = 0; x < Math.min(this.siegeCount + minguards, this.hiredGuardNumber - guards.length); ++x) {
                            try {
                                if (this.freeGuards.isEmpty()) {
                                    toReturn = Creature.doNew(templateId, villToken.getPosX(), villToken.getPosY(), (float)Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                                    village.createGuard(toReturn, System.currentTimeMillis());
                                } else {
                                    toReturn = (Creature)this.freeGuards.removeFirst();
                                    if (toReturn.getTemplate().getTemplateId() != templateId) {
                                        this.removeReturnedGuard(toReturn.getWurmId());
                                        toReturn.destroy();
                                        newc = Creature.doNew(templateId, villToken.getPosX(), villToken.getPosY(), (float)Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                                        village.createGuard(newc, System.currentTimeMillis());
                                    } else {
                                        village.createGuard(toReturn, System.currentTimeMillis());
                                        this.removeReturnedGuard(toReturn.getWurmId());
                                        this.putGuardInWorld(toReturn);
                                    }
                                }
                            } catch (Exception var10) {
                                logger.log(Level.WARNING, var10.getMessage(), var10);
                            }
                        }
                    } catch (NoSuchItemException var12) {
                        logger.log(Level.WARNING, "Village " + village.getName() + " has no token.");
                    }

                    if (this.siegeCount > 0) {
                        this.siegeCount += 3;
                    }
                }

                village.checkForEnemies();
            } catch (NoSuchVillageException var13) {
                logger.log(Level.WARNING, "No village for guardplan with villageid " + this.villageId, var13);
            }
        }

    }

    public void startSiege() {
        this.siegeCount = 1;
    }

    public boolean isUnderSiege() {
        return this.siegeCount > 0;
    }

    public int getSiegeCount() {
        return this.siegeCount;
    }

    private void putGuardInWorld(Creature guard) {
        try {
            Item token = this.getVillage().getToken();
            guard.setPositionX(token.getPosX());
            guard.setPositionY(token.getPosY());

            try {
                guard.setLayer(token.isOnSurface() ? 0 : -1, false);
                guard.setPositionZ(Zones.calculateHeight(guard.getPosX(), guard.getPosY(), token.isOnSurface()));
                guard.respawn();
                Zone zone = Zones.getZone(guard.getTileX(), guard.getTileY(), guard.isOnSurface());
                zone.addCreature(guard.getWurmId());
                guard.savePosition(zone.getId());
            } catch (NoSuchZoneException var4) {
                logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + var4.getMessage(), var4);
            } catch (NoSuchCreatureException var5) {
                logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + var5.getMessage(), var5);
                this.getVillage().deleteGuard(guard, false);
            } catch (NoSuchPlayerException var6) {
                logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + var6.getMessage(), var6);
            } catch (Exception var7) {
                logger.log(Level.WARNING, "Failed to return village guard: " + var7.getMessage(), var7);
            }
        } catch (NoSuchItemException var8) {
            logger.log(Level.WARNING, var8.getMessage(), var8);
        } catch (NoSuchVillageException var9) {
            logger.log(Level.WARNING, var9.getMessage(), var9);
        }

    }

    public final void returnGuard(Creature guard) {
        if (!this.freeGuards.contains(guard)) {
            this.freeGuards.add(guard);
            this.addReturnedGuard(guard.getWurmId());
        }

    }

    private boolean pollUpkeep() {
        try {
            if (this.getVillage().isPermanent) {
                return false;
            }
        } catch (NoSuchVillageException var11) {
        }

        if (!Servers.localServer.isUpkeep()) {
            return false;
        } else {
            long upkeep = (long)this.calculateUpkeep(true);
            if (this.moneyLeft - upkeep <= 0L) {
                try {
                    logger.log(Level.INFO, this.getVillage().getName() + " disbanding. Money left=" + this.moneyLeft + ", upkeep=" + upkeep);
                } catch (NoSuchVillageException var6) {
                    logger.log(Level.INFO, var6.getMessage(), var6);
                }

                return true;
            } else {
                if (upkeep >= 100L) {
                    try {
                        logger.log(Level.INFO, this.getVillage().getName() + " upkeep=" + upkeep);
                    } catch (NoSuchVillageException var10) {
                        logger.log(Level.INFO, var10.getMessage(), var10);
                    }
                }

                this.updateGuardPlan(this.type, this.moneyLeft - Math.max(1L, upkeep), this.hiredGuardNumber);
                ++this.upkeepCounter;
                if (this.upkeepCounter == 2) {
                    this.upkeepCounter = 0;
                    Shop shop = Economy.getEconomy().getKingsShop();
                    if (shop != null) {
                        if (upkeep <= 1L) {
                            shop.setMoney(shop.getMoney() + Math.max(1L, upkeep));
                        } else {
                            shop.setMoney(shop.getMoney() + upkeep);
                        }
                    } else {
                        logger.log(Level.WARNING, "No shop when " + this.villageId + " paying upkeep.");
                    }
                }

                long tl = this.getTimeLeft();
                if (tl < 3600000L) {
                    try {
                        this.getVillage().broadCastAlert("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.", (byte)2);
                        this.getVillage().broadCastAlert("Any traders who are citizens of " + this.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var9) {
                        logger.log(Level.WARNING, "No Village? " + this.villageId, var9);
                    }
                } else if (tl < 86400000L) {
                    if (System.currentTimeMillis() - this.lastSentWarning > 3600000L) {
                        this.lastSentWarning = System.currentTimeMillis();

                        try {
                            this.getVillage().broadCastAlert("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.", (byte)2);
                            this.getVillage().broadCastAlert("Any traders who are citizens of " + this.getVillage().getName() + " will disband without refund.");
                        } catch (NoSuchVillageException var8) {
                            logger.log(Level.WARNING, "No Village? " + this.villageId, var8);
                        }
                    }
                } else if (tl < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 3600000L) {
                    this.lastSentWarning = System.currentTimeMillis();

                    try {
                        this.getVillage().broadCastAlert("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.", (byte)4);
                        this.getVillage().broadCastAlert("Any traders who are citizens of " + this.getVillage().getName() + " will disband without refund.");
                    } catch (NoSuchVillageException var7) {
                        logger.log(Level.WARNING, "No Village? " + this.villageId, var7);
                    }
                }

                return false;
            }
        }
    }

    public final void destroyGuard(Creature guard) {
        this.freeGuards.remove(guard);
        this.removeReturnedGuard(guard.getWurmId());
    }

    final boolean poll() {
        this.pollGuards();
        if (this.siegeCount > 0) {
            --this.siegeCount;
            this.siegeCount = Math.min(this.siegeCount, 9);

            try {
                if (!this.getVillage().isAlerted()) {
                    this.siegeCount = Math.max(0, this.siegeCount - 1);
                }
            } catch (NoSuchVillageException var2) {
                logger.log(Level.WARNING, var2.getMessage());
            }
        }

        if (this.drainModifier > 0.0F && System.currentTimeMillis() - this.lastDrained > 172800000L) {
            this.drainModifier = 0.0F;
            this.saveDrainMod();
        }

        return this.pollUpkeep();
    }

    public final long getLastDrained() {
        return this.lastDrained;
    }

    public static final int getMaxGuards(Village village) {
        return getMaxGuards(village.getDiameterX(), village.getDiameterY());
    }

    public static final int getMaxGuards(int diameterX, int diameterY) {
        return Math.min(maxGuards, Math.max(3, diameterX * diameterY / 49));
    }

    public final int getNumHiredGuards() {
        return this.hiredGuardNumber;
    }

    public final int getConvertedGuardNumber(Village village) {
        int max = getMaxGuards(village);
        if (this.type == 1) {
            max = Math.max(1, max / 4);
        }

        if (this.type == 2) {
            Math.max(1, max /= 2);
        }

        return max;
    }

    public final void changePlan(int newPlan, int newNumberOfGuards) {
        this.lastChangedPlan = System.currentTimeMillis();
        int changeInGuards = newNumberOfGuards - this.getNumHiredGuards();
        this.updateGuardPlan(newPlan, this.moneyLeft, newNumberOfGuards);
        if (changeInGuards < 0) {
            try {
                Village village = this.getVillage();
                int deleted = 0;
                changeInGuards = Math.abs(changeInGuards);
                int x;
                if (this.freeGuards.size() > 0) {
                    Creature[] crets = (Creature[])this.freeGuards.toArray(new Creature[this.freeGuards.size()]);

                    for(x = 0; x < Math.min(crets.length, changeInGuards); ++x) {
                        ++deleted;
                        this.removeReturnedGuard(crets[x].getWurmId());
                        crets[x].destroy();
                    }
                }

                if (deleted < changeInGuards) {
                    Guard[] guards = village.getGuards();

                    for(x = 0; x < Math.min(guards.length, changeInGuards - deleted); ++x) {
                        if (guards[x].creature.isSpiritGuard()) {
                            village.deleteGuard(guards[x].creature, true);
                        }
                    }
                }
            } catch (NoSuchVillageException var8) {
                logger.log(Level.WARNING, "Village lacking for plan " + this.villageId, var8);
            }
        }

    }

    public final void addMoney(long moneyAdded) {
        if (moneyAdded > 0L) {
            this.updateGuardPlan(this.type, this.moneyLeft + moneyAdded, this.hiredGuardNumber);
        }

    }

    public final long getTimeToNextDrain() {
        try {
            if (this.getVillage().isPermanent) {
                return 86400000L;
            }
        } catch (NoSuchVillageException var2) {
            logger.log(Level.WARNING, this.villageId + ", " + var2.getMessage(), var2);
            return 86400000L;
        }

        return this.lastDrained + 86400000L - System.currentTimeMillis();
    }

    public final long getMoneyDrained() {
        try {
            if (this.getVillage().isPermanent) {
                return 0L;
            }
        } catch (NoSuchVillageException var2) {
            logger.log(Level.WARNING, this.villageId + ", " + var2.getMessage(), var2);
            return 0L;
        }

        return (long)Math.min((float)this.moneyLeft, (1.0F + this.drainModifier) * Math.max(7500.0F, (float)this.getMonthlyCost() * 0.15F));
    }

    public long drainMoney() {
        long moneyToDrain = this.getMoneyDrained();
        this.drainGuardPlan(this.moneyLeft - moneyToDrain);
        this.drainModifier += 0.5F;
        this.saveDrainMod();
        return moneyToDrain;
    }

    public final void fixGuards() {
        try {
            Guard[] gs = this.getVillage().getGuards();

            for(int x = 0; x < gs.length; ++x) {
                if (gs[x].creature.isDead()) {
                    this.getVillage().deleteGuard(gs[x].creature, false);
                    this.returnGuard(gs[x].creature);
                    logger.log(Level.INFO, "Destroyed dead guard for " + this.getVillage().getName());
                }
            }
        } catch (NoSuchVillageException var3) {
            logger.log(Level.WARNING, "Village lacking for plan " + this.villageId, var3);
        }

    }

    public final float getProsperityModifier() {
        return this.getMoneyLeft() > 1000000L ? 1.05F : 1.0F;
    }

    public void updateGuardPlan(long aMoneyLeft) {
        this.updateGuardPlan(this.type, aMoneyLeft, this.hiredGuardNumber);
    }

    abstract void create();

    abstract void load();

    public abstract void updateGuardPlan(int var1, long var2, int var4);

    abstract void delete();

    abstract void addReturnedGuard(long var1);

    abstract void removeReturnedGuard(long var1);

    abstract void saveDrainMod();

    abstract void deleteReturnedGuards();

    public abstract void addPayment(String var1, long var2, long var4);

    abstract void drainGuardPlan(long var1);

    static {
        maxGuards = Servers.localServer.isChallengeOrEpicServer() ? 20 : 50;
    }
}
