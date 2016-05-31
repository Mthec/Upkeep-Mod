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
}
