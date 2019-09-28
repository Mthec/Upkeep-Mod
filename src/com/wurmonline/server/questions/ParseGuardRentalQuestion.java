package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParseGuardRentalQuestion {
    private static final Logger logger = Logger.getLogger(ParseGuardRentalQuestion.class.getName());

    public static Object parseGuardRentalQuestion(Object o, Method method, Object[] args) {
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

                    freeGuards = UpkeepCosts.free_guards;

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
