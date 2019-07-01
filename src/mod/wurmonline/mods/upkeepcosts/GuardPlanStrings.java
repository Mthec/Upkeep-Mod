package mod.wurmonline.mods.upkeepcosts;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.questions.GuardManagementQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuardPlanStrings {
    private static final Logger logger = Logger.getLogger(GuardPlanStrings.class.getName());

    public static String getMoneyDrained = "{try {\n" +
            "    if(this.getVillage().isPermanent) {\n" +
            "        return 0L;\n" +
            "    }\n" +
            "} catch (com.wurmonline.server.villages.NoSuchVillageException var2) {\n" +
            "    logger.log(java.util.logging.Level.WARNING, this.villageId + \", \" + var2.getMessage(), var2);\n" +
            "    return 0L;\n" +
            "}\n" +
            "return (long)Math.min((float)this.moneyLeft, (1.0F + this.drainModifier) * Math.max((float)com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"minMoneyDrained\").getLong(this), (float)this.getMonthlyCost() * 0.15F));\n" +
            "}";

    public static String drainMoney = "{long moneyToDrain = this.getMoneyDrained();\n" +
            "this.drainGuardPlan(this.moneyLeft - moneyToDrain);\n" +
            "this.drainModifier = Math.min(com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"maxDrainModifier\").getFloat(this), com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"drainCumulateFigure\").getFloat(this) + this.drainModifier);\n" +
            "this.saveDrainMod();\n" +
            "return moneyToDrain;\n" +
            "}";

    public static String getMonthlyCost = "if(!com.wurmonline.server.Servers.localServer.isUpkeep()) {\n" +
            "    return 0L;\n" +
            "} else {\n" +
            "    try {\n" +
            "        com.wurmonline.server.villages.Village sv = this.getVillage();\n" +
            "        long tiles = (long)sv.getNumTiles() - com.wurmonline.server.villages.Villages.FREE_TILES;" +
            "        long cost = tiles > 0L ? tiles * com.wurmonline.server.villages.Villages.TILE_UPKEEP : 0L;\n" +
            "        long perimeter = (long)sv.getPerimeterNonFreeTiles() - com.wurmonline.server.villages.Villages.FREE_PERIMETER;" +
            "        cost += perimeter > 0L ? perimeter * com.wurmonline.server.villages.Villages.PERIMETER_UPKEEP : 0L;\n" +
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
            "}";

    public static String getTimeLeft = "if ($_ == 29030400000L) {" +
            "    return 29030400000L;" +
            "} else {" +
            "    return (long)((double)this.moneyLeft / this.calculateUpkeep(false) * 500000.0D);" +
            "}";

    public static Object getCostForGuards(Object o, Method method, Object[] args) {
        int nonFreeGuards = (int)args[0];
        try {
            nonFreeGuards = Math.max(0, nonFreeGuards - Villages.class.getDeclaredField("FREE_GUARDS").getInt(null));
            if (!Villages.class.getDeclaredField("EPIC_UPKEEP_SCALING").getBoolean(null)) {
                return nonFreeGuards * Villages.GUARD_UPKEEP;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Servers.localServer.isChallengeOrEpicServer() ? (nonFreeGuards * Villages.GUARD_UPKEEP + (nonFreeGuards - 1) * nonFreeGuards / 2 * 100 * 50) : nonFreeGuards * Villages.GUARD_UPKEEP;
    }

    public static String pollUpkeep = "{try {" +
            "    if(this.getVillage().isPermanent) {" +
            "        return false;" +
            "    }" +
            "} catch (com.wurmonline.server.villages.NoSuchVillageException var11) {" +
            "    ;" +
            "}" +
            "if (!com.wurmonline.server.Servers.localServer.isUpkeep()) {" +
            "    return false;" +
            "}" +
            "double upkeepD = this.calculateUpkeep(true);" +
            "this.upkeepBuffer += upkeepD % 1;" +
            "upkeepD -= upkeepD % 1;" +
            "while (this.upkeepBuffer >= 1.0D) {" +
            "    this.upkeepBuffer -= 1.0D;" +
            "    upkeepD += 1.0D;" +
            "}" +
            "if (this.output) {" +
            "    System.out.println(\"Village upkeep - \" + this.getVillage().getName() + \" paid \" + Double.toString(upkeepD) + \" this turn.  Upkeep buffer is now \" + Double.toString(this.upkeepBuffer));" +
            "}" +
            "long upkeep = (long)upkeepD;" +
            "if(this.moneyLeft - upkeep <= 0L) {" +
            "    try {" +
            "        logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" disbanding. Money left=\" + this.moneyLeft + \", upkeep=\" + upkeep);" +
            "    } catch (com.wurmonline.server.villages.NoSuchVillageException var6) {" +
            "        logger.log(java.util.logging.Level.INFO, var6.getMessage(), var6);" +
            "    }" +
            "    return true;" +
            "} else {" +
            "    if(upkeep >= 100L) {" +
            "        try {" +
            "            logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" upkeep=\" + upkeep);" +
            "        } catch (com.wurmonline.server.villages.NoSuchVillageException var10) {" +
            "            logger.log(java.util.logging.Level.INFO, var10.getMessage(), var10);" +
            "        }" +
            "    }" +
            "    this.updateGuardPlan(this.type, this.moneyLeft - upkeep, this.hiredGuardNumber);" +
            "    ++this.upkeepCounter;" +
            "    if(this.upkeepCounter == 2) {" +
            "        this.upkeepCounter = 0;" +
            "        com.wurmonline.server.economy.Shop tl = com.wurmonline.server.economy.Economy.getEconomy().getKingsShop();" +
            "        if(tl != null) {" +
            "            tl.setMoney(tl.getMoney() + upkeep);" +
            "        } else {" +
            "           logger.log(java.util.logging.Level.WARNING, \"No shop when \" + this.villageId + \" paying upkeep.\");" +
            "        }" +
            "    }" +
            "    long var12 = this.getTimeLeft();" +
            "    if(var12 < 3600000L) {" +
            "        try {" +
            "            this.getVillage().broadCastAlert(\"The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.\", (byte)2);" +
            "            if (com.wurmonline.server.villages.Villages.FREE_TILES > 0 || com.wurmonline.server.villages.Villages.FREE_PERIMETER > 0)" +
            "                this.getVillage().broadCastAlert(\"Or you may resize to remove any non-free tiles.  You can have up to \" + com.wurmonline.server.villages.Villages.FREE_TILES + \" free tiles and \" + com.wurmonline.server.villages.Villages.FREE_PERIMETER + \" free perimeter tiles.\", (byte)2);" +
            "            this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "        } catch (com.wurmonline.server.villages.NoSuchVillageException var9) {" +
            "            logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var9);" +
            "        }" +
            "    } else if(var12 < 86400000L) {" +
            "        if(System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
            "            this.lastSentWarning = System.currentTimeMillis();" +
            "            try {" +
            "                this.getVillage().broadCastAlert(\"The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.\", (byte)2);" +
            "                if (com.wurmonline.server.villages.Villages.FREE_TILES > 0 || com.wurmonline.server.villages.Villages.FREE_PERIMETER > 0)" +
            "                    this.getVillage().broadCastAlert(\"Or you may resize to remove any non-free tiles.  You can have up to \" + com.wurmonline.server.villages.Villages.FREE_TILES + \" free tiles and \" + com.wurmonline.server.villages.Villages.FREE_PERIMETER + \" free perimeter tiles.\", (byte)2);" +
            "                this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "            } catch (com.wurmonline.server.villages.NoSuchVillageException var8) {" +
            "                logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var8);" +
            "            }" +
            "        }" +
            "   } else if(var12 < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
            "        this.lastSentWarning = System.currentTimeMillis();" +
            "        try {" +
            "            this.getVillage().broadCastAlert(\"The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.\", (byte)4);" +
            "            if (com.wurmonline.server.villages.Villages.FREE_TILES > 0 || com.wurmonline.server.villages.Villages.FREE_PERIMETER > 0)" +
            "                this.getVillage().broadCastAlert(\"You may resize to remove any non-free tiles.  You can have up to \" + com.wurmonline.server.villages.Villages.FREE_TILES + \" free tiles and \" + com.wurmonline.server.villages.Villages.FREE_PERIMETER + \" free perimeter tiles.\", (byte)4);" +
            "            this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "        } catch (com.wurmonline.server.villages.NoSuchVillageException var7) {" +
            "            logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var7);" +
            "        }" +
            "    }" +
            "    return false;" +
            "}}";

    static Object parseGuardRentalQuestion(Object o, Method method, Object[] args) {
        GuardManagementQuestion question = (GuardManagementQuestion)args[0];
        Properties props;
        try {
            props = ReflectionUtil.getPrivateField(question, Question.class.getDeclaredField("answer"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }

        Creature responder = question.getResponder();
        String key = "12345678910";
        String val = null;
        Village village = responder.citizenVillage;
        long money = responder.getMoney();
        if (money > 0L) {
            long valueWithdrawn;
            try {
                valueWithdrawn = ReflectionUtil.callPrivateMethod(null, QuestionParser.class.getDeclaredMethod("getValueWithdrawn", Question.class), question);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
            if (valueWithdrawn > 0L) {
                try {
                    if (village.plan != null) {
                        if (responder.chargeMoney(valueWithdrawn)) {
                            village.plan.addMoney(valueWithdrawn);
                            village.plan.addPayment(responder.getName(), responder.getWurmId(), valueWithdrawn);
                            Change newch = Economy.getEconomy().getChangeFor(valueWithdrawn);
                            responder.getCommunicator().sendNormalServerMessage("You pay " + newch.getChangeString() + " to the upkeep fund of " + village.getName() + ".");
                            logger.log(Level.INFO, responder.getName() + " added " + valueWithdrawn + " irons to " + village.getName() + " upkeep.");
                        } else {
                            responder.getCommunicator().sendNormalServerMessage("You don't have that much money.");
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("This village does not have an upkeep plan.");
                    }
                } catch (IOException var17) {
                    logger.log(Level.WARNING, "Failed to withdraw money from " + responder.getName() + ":" + var17.getMessage(), var17);
                    responder.getCommunicator().sendNormalServerMessage("The transaction failed. Please contact the game masters using the <i>/dev</i> command.");
                }
            } else {
                responder.getCommunicator().sendNormalServerMessage("No money withdrawn.");
            }
        }

        if (responder.mayManageGuards()) {
            GuardPlan plan = responder.getCitizenVillage().plan;
            if (plan != null) {
                boolean changed = false;
                key = "hired";
                val = (String)props.get(key);
                int nums = plan.getNumHiredGuards();
                int oldnums = nums;
                if (val == null) {
                    responder.getCommunicator().sendNormalServerMessage("Failed to parse the value " + val + ". Please enter a number if you wish to change the number of guards.");
                    return null;
                }

                try {
                    nums = Integer.parseInt(val);
                } catch (NumberFormatException var16) {
                    responder.getCommunicator().sendNormalServerMessage("Failed to parse the value " + val + ". Please enter a number if you wish to change the number of guards.");
                    return null;
                }

                if (nums != plan.getNumHiredGuards()) {
                    int freeGuards = 0;

                    try {
                        freeGuards = Villages.class.getDeclaredField("FREE_GUARDS").getInt(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    boolean aboveMax = nums > GuardPlan.getMaxGuards(responder.getCitizenVillage());
                    nums = Math.min(nums, GuardPlan.getMaxGuards(responder.getCitizenVillage()));
                    int diff = Math.max(0, nums - freeGuards) - Math.max(0, plan.getNumHiredGuards() - freeGuards);
                    boolean takeFromBank = Boolean.parseBoolean(props.getProperty("use_bank"));
                    if (diff > 0) {
                        long moneyOver = plan.moneyLeft - plan.calculateMonthlyUpkeepTimeforType(0);
                        long costForHire = Villages.GUARD_COST * diff;
                        if (moneyOver > costForHire) {
                            changed = true;
                            plan.changePlan(0, nums);
                            plan.updateGuardPlan(0, plan.moneyLeft - costForHire, nums);
                        } else if (takeFromBank && responder.getMoney() > costForHire) {
                            try {
                                responder.setMoney(responder.getMoney() - costForHire);
                                changed = true;
                                plan.changePlan(0, nums);
                                plan.updateGuardPlan(0, plan.moneyLeft, nums);
                            } catch (IOException e) {
                                e.printStackTrace();
                                responder.getCommunicator().sendAlertServerMessage("An error occurred when taking payment.  Please report.");
                                return null;
                            }
                        } else {
                            // TODO - Not really true, as the cost comes after the check?
                            responder.getCommunicator().sendNormalServerMessage("There was not enough upkeep to increase the number of guards. Please make sure that there is at least one month of upkeep left after you hire the guards.");
                        }
                    } else if (diff < 0) {
                        changed = true;
                        plan.changePlan(0, nums);
                    }

                    if (aboveMax) {
                        responder.getCommunicator().sendNormalServerMessage("You tried to increase the amount of guards above the max of " + GuardPlan.getMaxGuards(responder.getCitizenVillage()) + " which was denied.");
                    }
                }

                if (changed && oldnums < nums) {
                    responder.getCommunicator().sendNormalServerMessage("You change the upkeep plan. New guards will arrive soon.");
                } else if (changed) {
                    responder.getCommunicator().sendNormalServerMessage("You change the upkeep plan.");
                } else {
                    responder.getCommunicator().sendNormalServerMessage("No change was made.");
                }
            }
        } else {
            logger.log(Level.WARNING, responder.getName() + " tried to manage guards without the right.");
        }


        return null;
    }
}
