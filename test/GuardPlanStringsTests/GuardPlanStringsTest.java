package GuardPlanStringsTests;

import javassist.*;
import org.junit.Before;

public class GuardPlanStringsTest {
    static Class GuardPlan;

    @Before
    public void createGuardPlan(String method) throws Exception {
        ClassPool pool = ClassPool.getDefault();

        CtClass village = pool.makeClass("test.Village");
        new CtNewConstructor();
        CtConstructor ct = CtNewConstructor.make("public Village(){}", village);
        village.addConstructor(ct);
        CtField isPermanent = new CtField(CtClass.booleanType, "isPermanent", village);
        isPermanent.setModifiers(Modifier.PUBLIC);
        village.addField(isPermanent, "false");
        // Needs to be constructed at least once to be usable?
        village.toClass();

        CtClass guardPlan = pool.makeClass("com.wurmonline.server.villages.GuardPlan");

        CtField village_instance = CtField.make("test.Village village = new test.Village();", guardPlan);
        guardPlan.addField(village_instance);
        CtField logger = CtField.make("private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(com.wurmonline.server.villages.GuardPlan.class.getName());", guardPlan);
        guardPlan.addField(logger);
        CtField villageId = CtField.make("long villageId = 0L;", guardPlan);
        guardPlan.addField(villageId);
        CtField moneyLeft = CtField.make("long moneyLeft = 10000L;", guardPlan);
        guardPlan.addField(moneyLeft);
        CtField drainModifier = CtField.make("float drainModifier = 0.0f;", guardPlan);
        guardPlan.addField(drainModifier);
        CtField minMoneyDrained = CtField.make("public static long minMoneyDrained = 30L;", guardPlan);
        guardPlan.addField(minMoneyDrained);

        CtMethod getVillage = CtMethod.make("test.Village getVillage() {return this.village;}", guardPlan);
        guardPlan.addMethod(getVillage);
        CtMethod getMonthlyCost = CtMethod.make("public long getMonthlyCost() {return 1L;}", guardPlan);
        guardPlan.addMethod(getMonthlyCost);

        CtMethod methodToAdd = CtMethod.make("public long getMoneyDrained() {return;}", guardPlan);
        methodToAdd.setBody(method);
        guardPlan.addMethod(methodToAdd);

        GuardPlan = guardPlan.toClass();
    }
}