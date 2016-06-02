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
            "this.drainModifier = Math.min(com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"maxDrainModifier\").getFloat(com.wurmonline.server.villages.GuardPlan.class), com.wurmonline.server.villages.GuardPlan.class.getDeclaredField(\"drainCumulateFigure\").getFloat(com.wurmonline.server.villages.GuardPlan.class) + this.drainModifier);\n" +
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

    public static String badGetMonthlyCost = "if(!com.wurmonline.server.Servers.localServer.isUpkeep()) {\n" +
            "    return 0L;\n" +
            "} else {\n" +
            "    try {\n" +
            "        com.wurmonline.server.villages.Village sv = this.getVillage();\n" +
            "        long tiles = (long)sv.getNumTiles() - com.wurmonline.server.villages.Villages.FREE_TILES;" +
            "        long cost = tiles > 0L ? tiles : 0L * com.wurmonline.server.villages.Villages.TILE_UPKEEP;\n" +
            "        long perimeter = (long)sv.getPerimeterNonFreeTiles() - com.wurmonline.server.villages.Villages.FREE_PERIMETER;" +
            "        cost += perimeter > 0L ? perimeter : 0L * com.wurmonline.server.villages.Villages.PERIMETER_UPKEEP;\n" +
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
}
