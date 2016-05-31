import javassist.*;
import mod.wurmonline.mods.upkeepcosts.GuardPlanStrings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GuardPlanStringsTests {
    static Class GuardPlan;

    @Before
    public void createGuardPlan() throws Exception {
        ClassPool pool = ClassPool.getDefault();

        //CtField field = new CtField(CtClass.longType, "test", cc);
        //field.setModifiers(Modifier.STATIC);
        //cc.addField(field, "1L");

        CtClass village = pool.makeClass("test.Village");
        new CtNewConstructor();
        CtConstructor ct = CtNewConstructor.make("public Village(){}", village);
        village.addConstructor(ct);
        CtField isPermanent = new CtField(CtClass.booleanType, "isPermanent", village);
        isPermanent.setModifiers(Modifier.PUBLIC);
        village.addField(isPermanent, "false");
        Object v = village.toClass().newInstance();

        CtClass cc = pool.makeClass("com.wurmonline.server.villages.GuardPlan");

        CtField village_instance = new CtField(village, "village", cc);
        cc.addField(village_instance, "new test.Village()");
        CtField logger = CtField.make("private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(com.wurmonline.server.villages.GuardPlan.class.getName());", cc);
        cc.addField(logger);
        CtField villageId = CtField.make("long villageId = 0L;", cc);
        cc.addField(villageId);
        CtField moneyLeft = CtField.make("long moneyLeft = 10000L;", cc);
        cc.addField(moneyLeft);
        CtField drainModifier = CtField.make("float drainModifier = 0.0f;", cc);
        cc.addField(drainModifier);
        CtField minMoneyDrained = CtField.make("public static long minMoneyDrained = 30L;", cc);
        cc.addField(minMoneyDrained);

        CtMethod getVillage = CtMethod.make("test.Village getVillage() {return this.village;}", cc);
        cc.addMethod(getVillage);

        CtMethod getMonthlyCost = CtMethod.make("public long getMonthlyCost() {return 1L;}", cc);
        cc.addMethod(getMonthlyCost);

        CtMethod method = CtMethod.make("public long getMoneyDrained() {return;}", cc);
        method.setBody(GuardPlanStrings.getMoneyDrained);
        cc.addMethod(method);

        GuardPlan = cc.toClass();
    }

    @Test
    public void test() throws Exception {
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(null, 50);
        Assert.assertEquals(50L, GuardPlan.getDeclaredMethod("getMoneyDrained").invoke(GuardPlan.newInstance()));
    }
}