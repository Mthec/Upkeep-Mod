//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// Modified from version by Code Club AB.
//

package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.GuardPlanMethods;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;

import java.util.Properties;

public final class GuardManagementQuestion extends Question implements TimeConstants, MonetaryConstants {
    public GuardManagementQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 9, aTarget);
    }

    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseGuardRentalQuestion(this);
    }

    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        Village village = this.getResponder().getCitizenVillage();
        if (village != null) {
            GuardPlan plan = village.plan;
            if (plan != null) {                
                if (village.isCitizen(this.getResponder())) {
                    buf.append("text{text=\"The size of " + village.getName() + " is " + village.getDiameterX() + " by " + village.getDiameterY() + ".\"}");
                    buf.append("text{text=\"The perimeter is " + (5 + village.getPerimeterSize()) + " and it has " + plan.getNumHiredGuards() + " guards hired.\"}");
                }

                buf.append("text{text=\"\"}");
                if (village.isPermanent) {
                    buf.append("text{text='This village is permanent, and should never run out of money or be drained.'}");
                } else {
                    Change c = Economy.getEconomy().getChangeFor(plan.moneyLeft);
                    buf.append("text{text='The settlement has " + c.getChangeString() + " left in its coffers.'}");
                    Change upkeep = Economy.getEconomy().getChangeFor(plan.getMonthlyCost());
                    buf.append("text{text='Upkeep per month is " + upkeep.getChangeString() + ".'}");
                    long monthlyCost = plan.getMonthlyCost();
                    float cost = (float)plan.moneyLeft / (float)monthlyCost;
                    long grace = GuardPlanMethods.graceTimeRemaining(village);
                    if (monthlyCost == 0) {
                        buf.append("text{text=\"This means that the upkeep should last indefinitely.\"}");
                    } else if (UpkeepCosts.upkeep_grace_period > 0 && grace > 0) {
                        GuardPlanMethods.addGraceTimeRemaining(buf, grace);
                    } else {
                        buf.append("text{text=\"This means that the upkeep should last for about " + (cost * 28.0F) + " days.\"}");
                    }
                    if (Servers.localServer.PVPSERVER || Servers.localServer.id == 3) {
                        buf.append("text{text=\"A drain would cost " + new Change(plan.getMoneyDrained()).getChangeShortString() + ".\"};");
                        long minimumDrain = UpkeepCosts.min_drain;
                        if (plan.moneyLeft < minimumDrain * 5) {
                            buf.append("text{type='bold';text='Since minimum drain is " + new Change(minimumDrain).getChangeShortString() + " it may be drained to disband in less than 5 days.'}");
                        }
                    }

                    buf.append("text{text=\"\"}");
                }

                if (Servers.localServer.isChallengeOrEpicServer()) {
                    buf.append("text{text=\"The only guard type is heavy guards. The cost for hiring them is " + Villages.GUARD_COST_STRING + " and running upkeep cost increases the more guards you have in a sort of ladder system. The first guards are cheaper than the last.\"};");
                    buf.append("text{text=\"Make sure to review the cost for upkeep once you are done.\"};");
                } else {
                    buf.append("text{text=\"The only guard type is heavy guards. The cost for hiring them is " + Villages.GUARD_COST_STRING + " and running upkeep is " + Villages.GUARD_UPKEEP_STRING + " per month.\"};");
                    buf.append("text{text=\"\"};");
                }

                buf.append("text{text=\"The cost for hiring the guards is a one-time summoning fee that is not returned in case you decide to lower the amount of guards.\"};");
                buf.append("text{text=\"\"}");
                if (Servers.localServer.PVPSERVER) {
                    buf.append("label{text='Note that you will need at least 1 guard to enforce the role rules on deed!'}");
                    buf.append("text{text=\"\"}");
                }

                int freeGuards = 0;

                freeGuards = Math.max(0, UpkeepCosts.free_guards - plan.getNumHiredGuards());

                buf.append("text{text=\"How many guards do you wish to have? You currently have " + plan.getNumHiredGuards() + ", can hire " + freeGuards + " more for free and may hire up to " + GuardPlan.getMaxGuards(village) + ".\"};input{text=\"" + plan.getNumHiredGuards() + "\";id=\"hired\"}; ");
                buf.append("text{text=\"\"};");

                buf.append("harray{label{text=\"Take money from my bank if there is not enough in the coffers\"};checkbox{id=\"use_bank\";selected=\"false\";text=\" \"}}");
                buf.append("text{text=\"\"}");
            }

            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
        }

    }
}
