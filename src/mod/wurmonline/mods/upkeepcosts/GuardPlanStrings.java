package mod.wurmonline.mods.upkeepcosts;

public class GuardPlanStrings {
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

    public static String getCostForGuards = "return (long)$1 * com.wurmonline.server.villages.Villages.GUARD_UPKEEP;";

    public static String pollUpkeep = "{try {" +
            "            if(this.getVillage().isPermanent) {" +
            "                return false;" +
            "            }" +
            "        } catch (com.wurmonline.server.villages.NoSuchVillageException var11) {" +
            "            ;" +
            "        }" +
            "double upkeepD = this.calculateUpkeep(true);" +
            "if (upkeepD < 0.0D) {" +
            "    logger.severe(\"Why is upkeep less than 0.0?\");" +
            "}" +
            "if (upkeepD < 1.0D) {" +
            "    this.upkeepBuffer += upkeepD;" +
            "    upkeepD = 0.0D;" +
            "}" +
            "while (this.upkeepBuffer >= 1.0D) {" +
            "    this.upkeepBuffer -= 1.0D;" +
            "    upkeepD += 1.0D;" +
            "}" +
            "if (this.output) {" +
            "    System.out.println(\"Village upkeep - \" + this.getVillage().getName() + \" paid \" + Double.toString(upkeepD) + \" this turn.  Upkeep buffer is now \" + Double.toString(this.upkeepBuffer));" +
            "}" +
            "if (upkeepD == 0.0D) {" +
            "    return false;" +
            "}" +
            "        long upkeep = (long)upkeepD;" +
            "        if(this.moneyLeft - upkeep <= 0L) {" +
            "            try {" +
            "                logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" disbanding. Money left=\" + this.moneyLeft + \", upkeep=\" + upkeep);" +
            "            } catch (com.wurmonline.server.villages.NoSuchVillageException var6) {" +
            "                logger.log(java.util.logging.Level.INFO, var6.getMessage(), var6);" +
            "            }" +
            "" +
            "            return true;" +
            "        } else {" +
            "            if(upkeep >= 100L) {" +
            "                try {" +
            "                    logger.log(java.util.logging.Level.INFO, this.getVillage().getName() + \" upkeep=\" + upkeep);" +
            "                } catch (com.wurmonline.server.villages.NoSuchVillageException var10) {" +
            "                    logger.log(java.util.logging.Level.INFO, var10.getMessage(), var10);" +
            "                }" +
            "            }" +
            "" +
            "            this.updateGuardPlan(this.type, this.moneyLeft - Math.max(1L, upkeep), this.hiredGuardNumber);" +
            "            ++this.upkeepCounter;" +
            "            if(this.upkeepCounter == 2) {" +
            "                this.upkeepCounter = 0;" +
            "                com.wurmonline.server.economy.Shop tl = com.wurmonline.server.economy.Economy.getEconomy().getKingsShop();" +
            "                if(tl != null) {" +
            "                    if(upkeep <= 1L) {" +
            "                        tl.setMoney(tl.getMoney() + Math.max(1L, upkeep));" +
            "                    } else {" +
            "                        tl.setMoney(tl.getMoney() + upkeep);" +
            "                    }" +
            "                } else {" +
            "                    logger.log(java.util.logging.Level.WARNING, \"No shop when \" + this.villageId + \" paying upkeep.\");" +
            "                }" +
            "            }" +
            "" +
            "            long var12 = this.getTimeLeft();" +
            "            if(var12 < 3600000L) {" +
            "                try {" +
            "                    this.getVillage().broadCastAlert(\"The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.\", (byte)2);" +
            "                    this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "                } catch (com.wurmonline.server.villages.NoSuchVillageException var9) {" +
            "                    logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var9);" +
            "                }" +
            "            } else if(var12 < 86400000L) {" +
            "                if(System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
            "                    this.lastSentWarning = System.currentTimeMillis();" +
            "" +
            "                    try {" +
            "                        this.getVillage().broadCastAlert(\"The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.\", (byte)2);" +
            "                        this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "                    } catch (com.wurmonline.server.villages.NoSuchVillageException var8) {" +
            "                        logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var8);" +
            "                    }" +
            "                }" +
            "            } else if(var12 < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 3600000L) {" +
            "                this.lastSentWarning = System.currentTimeMillis();" +
            "" +
            "                try {" +
            "                    this.getVillage().broadCastAlert(\"The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.\", (byte)4);" +
            "                    this.getVillage().broadCastAlert(\"Any traders who are citizens of \" + this.getVillage().getName() + \" will disband without refund.\");" +
            "                } catch (com.wurmonline.server.villages.NoSuchVillageException var7) {" +
            "                    logger.log(java.util.logging.Level.WARNING, \"No Village? \" + this.villageId, var7);" +
            "                }" +
            "            }" +
            "            return false;" +
            "        }}";
}
