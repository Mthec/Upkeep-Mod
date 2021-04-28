//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// Modified from version by Code Club AB.
//

package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.*;

import java.text.NumberFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageUpkeep extends Question implements VillageStatus, TimeConstants, MonetaryConstants {
    private static final Logger logger = Logger.getLogger(VillageUpkeep.class.getName());
    private static final NumberFormat nf = NumberFormat.getInstance();

    public VillageUpkeep(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 120, aTarget);
        nf.setMaximumFractionDigits(6);
    }

    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseVillageUpkeepQuestion(this);
    }

    public void sendQuestion() {
        try {
            Village nsv;
            if(this.target == -10L) {
                nsv = this.getResponder().getCitizenVillage();
                if(nsv == null) {
                    throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
                }
            } else {
                Item buf = Items.getItem(this.target);
                int plan = buf.getData2();
                nsv = Villages.getVillage(plan);
            }

            StringBuilder buf1 = new StringBuilder();
            buf1.append(this.getBmlHeader());
            buf1.append("header{text=\"" + nsv.getName() + "\"}");
            GuardPlan plan1 = nsv.plan;
            if(nsv.isPermanent) {
                buf1.append("text{text=\'This village is permanent, and should never run out of money or be drained.\'}");
            } else if(!Servers.localServer.isUpkeep()) {
                buf1.append("text{text=\'There are no upkeep costs for settlements here.\'}");
            } else if(plan1 != null) {
                if(nsv.isCitizen(this.getResponder()) || this.getResponder().getPower() >= 2) {
                    Change money = Economy.getEconomy().getChangeFor(plan1.moneyLeft);
                    buf1.append("text{text=\'The settlement has " + money.getChangeString() + " left in its coffers.\'}");
                    Change upkeep = Economy.getEconomy().getChangeFor(plan1.getMonthlyCost());
                    buf1.append("text{text=\'Upkeep per month is " + upkeep.getChangeString() + ".\'}");
                    long monthlyCost = plan1.getMonthlyCost();
                    float cost = (float)plan1.moneyLeft / (float)monthlyCost;
                    if (monthlyCost == 0)
                        buf1.append("text{text=\"This means that the upkeep should last indefinitely.\"}");
                    else
                        buf1.append("text{text=\"This means that the upkeep should last for about " + (int)(cost * 28.0F) + " days.\"}");
                    if(Servers.localServer.PVPSERVER) {
                        buf1.append("text{text=\"A drain would cost " + Economy.getEconomy().getChangeFor(plan1.getMoneyDrained()).getChangeString() + ".\"};");
                        if(plan1.moneyLeft < plan1.getMoneyDrained() * 5) {
                            try {
                                buf1.append("text{type=\'bold\';text=\'Since minimum drain is " + Economy.getEconomy().getChangeFor(plan1.getClass().getField("minMoneyDrained").getLong(plan1.getClass())).getChangeString() + " it may be drained to disband in less than 5 days.\'}");
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(nsv.isMayor(this.getResponder()) && Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep() && nsv.getCreationDate() < System.currentTimeMillis() + 2419200000L) {
                        buf1.append("text{text=\"\"}");
                        buf1.append("text{type=\'bold\';text=\'Free deeding is enabled and your settlement is less than 30 days old. You will not receive a refund if you choose to disband before your village is 30 days old.\'}");
                    }
                }
            } else {
                buf1.append("text{text=\"No plan found!\"}");
            }

            buf1.append("text{text=\"\"}");
            long money1 = this.getResponder().getMoney();
            if(money1 > 0L && (!nsv.isPermanent || this.getResponder().getPower() >= 2) && Servers.localServer.isUpkeep()) {
                buf1.append("text{text=\"If you wish to contribute to the upkeep costs of this settlement, fill in the amount below:\"}");
                Change mayor1 = Economy.getEconomy().getChangeFor(money1);
                buf1.append("text{text=\"You may pay up to " + mayor1.getChangeString() + ".\"}");
                buf1.append("text{text=\"The money will be added to the settlement upkeep fund.\"}");
                buf1.append("text{type=\"italic\";text=\"If the settlement has more than one month worth of upkeep, there will be no decay on houses, fences, and bulk and food storage bins will not be subject to a 5% loss every 30 days. If there is less than a week, decay will be very fast and bulk and food storage bins will lose 5% of their contents every 30 days.\"};text{text=\"\"}");
                long gold = mayor1.getGoldCoins();
                long silver = mayor1.getSilverCoins();
                long copper = mayor1.getCopperCoins();
                long iron = mayor1.getIronCoins();
                if(gold > 0L) {
                    buf1.append("harray{input{maxchars=\"10\";id=\"gold\";text=\"0\"};label{text=\"(" + gold + ") Gold coins\"}}");
                }

                if(silver > 0L || gold > 0L) {
                    buf1.append("harray{input{maxchars=\"10\";id=\"silver\";text=\"0\"};label{text=\"(" + silver + ") Silver coins\"}}");
                }

                if(copper > 0L || silver > 0L || gold > 0L) {
                    buf1.append("harray{input{maxchars=\"10\";id=\"copper\";text=\"0\"};label{text=\"(" + copper + ") Copper coins\"}}");
                }

                if(iron > 0L || copper > 0L || silver > 0L || gold > 0L) {
                    buf1.append("harray{input{maxchars=\"10\";id=\"iron\";text=\"0\"};label{text=\"(" + iron + ") Iron coins\"}}");
                }
            } else if(Servers.localServer.isUpkeep() && money1 == 0L) {
                buf1.append("text{text=\"You may contribute to the upkeep costs of this settlement if you have money in the bank.\"}");
            }

            buf1.append("text{text=\"\"}");
            Citizen mayor2 = nsv.getMayor();
            if(mayor2 != null) {
                buf1.append("text{type=\"italic\";text=\"" + mayor2.getName() + ", " + mayor2.getRole().getName() + ", " + nsv.getName() + "\"};text{text=\"\"}");
            } else {
                buf1.append("text{type=\"italic\";text=\"The Citizens, " + nsv.getName() + "\"};text{text=\"\"}");
            }

            buf1.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf1.toString(), 200, 200, 200, this.title);
        } catch (NoSuchItemException var15) {
            logger.log(Level.WARNING, this.getResponder().getName() + " tried to get info for null token with id " + this.target, var15);
            this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
        } catch (NoSuchVillageException var16) {
            logger.log(Level.WARNING, this.getResponder().getName() + " tried to get info for null settlement for token with id " + this.target);
            this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
        }

    }
}
