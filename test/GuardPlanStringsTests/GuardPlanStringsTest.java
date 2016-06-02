package GuardPlanStringsTests;

import javassist.*;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

class GuardPlanStringsTest {
    static Class<?> GuardPlan;
    static Map<String, String> methodsToTest = new HashMap<>();
    Object gPlan;

    @Before
    public void setUp() throws Exception {
        if (GuardPlan == null) {
            createGuardPlan();
        }
        // Reset values
        gPlan = GuardPlan.newInstance();
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 10000L);
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, 1000L);
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, 300L);
        GuardPlan.getDeclaredField("drainModifier").setFloat(gPlan, 0.0f);
        gPlan.getClass().getDeclaredMethod("setIsPermanent", boolean.class).invoke(gPlan, false);
    }

    private void createGuardPlan() throws Exception {
        ClassPool pool = ClassPool.getDefault();

        CtClass village = pool.makeClass("test.Village");
        new CtNewConstructor();
        CtConstructor ct = CtNewConstructor.make("public Village(){}", village);
        village.addConstructor(ct);
        CtField isPermanent = CtField.make("public boolean isPermanent = false;", village);
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
        CtField moneyLeft = CtField.make("public long moneyLeft;", guardPlan);
        guardPlan.addField(moneyLeft);
        CtField drainModifier = CtField.make("public float drainModifier;", guardPlan);
        guardPlan.addField(drainModifier);
        CtField minMoneyDrained = CtField.make("public static long minMoneyDrained;", guardPlan);
        guardPlan.addField(minMoneyDrained);
        CtField monthlyCost = CtField.make("public long monthlyCost;", guardPlan);
        guardPlan.addField(monthlyCost);
        // Extra testing method
        guardPlan.addMethod(CtMethod.make("public void setIsPermanent(boolean value) {this.village.isPermanent = value; return;}", guardPlan));

        CtMethod getVillage = CtMethod.make("test.Village getVillage() {return this.village;}", guardPlan);
        guardPlan.addMethod(getVillage);
        CtMethod getMonthlyCost = CtMethod.make("public long getMonthlyCost() {return this.monthlyCost;}", guardPlan);
        guardPlan.addMethod(getMonthlyCost);

        methodsToTest.forEach((def, body) -> {
                try {
                    CtMethod methodToAdd = CtMethod.make(def + "() {return;}", guardPlan);
                    methodToAdd.setBody(body);
                    guardPlan.addMethod(methodToAdd);
                } catch (CannotCompileException ex) {
                    ex.printStackTrace();
                }
        });

        GuardPlan = guardPlan.toClass();
    }
}