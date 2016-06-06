package GuardPlanStringsTests;

import javassist.*;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

abstract class GuardPlanStringsTest {
    static Class<?> Village;
    static Class<?> Villages;
    static Class<?> GuardPlan;
    static Class<?> LocalServer;
    static Map<String, String> methodsToTest = new HashMap<>();
    static Map<String, String> insertAftersToTest = new HashMap<>();
    Object gPlan;
    Object gVillage;

    @Before
    public void setUp() throws Exception {
        if (GuardPlan == null) {
            ClassPool pool = ClassPool.getDefault();
            createVillage(pool);
            createVillages(pool);
            createOther(pool);
            createGuardPlan(pool);
        }
        // Reset values
        gPlan = GuardPlan.newInstance();
        GuardPlan.getDeclaredField("moneyLeft").setLong(gPlan, 10000L);
        GuardPlan.getDeclaredField("monthlyCost").setLong(gPlan, 1000L);
        GuardPlan.getDeclaredField("minMoneyDrained").setLong(gPlan, 300L);
        GuardPlan.getDeclaredField("drainModifier").setFloat(gPlan, 0.0f);
        GuardPlan.getDeclaredField("maxDrainModifier").setFloat(gPlan, 5.0f);
        GuardPlan.getDeclaredField("guardPlanDrained").setLong(gPlan, 0L);
        GuardPlan.getDeclaredField("savedDrainMod").setBoolean(gPlan, false);
        GuardPlan.getDeclaredField("drainCumulateFigure").setFloat(gPlan, 0.0f);

        gVillage = GuardPlan.getDeclaredField("village").get(gPlan);
        Village.getDeclaredField("isPermanent").setBoolean(gVillage, false);
        Village.getDeclaredField("numTiles").setLong(gVillage, 110L);
        Village.getDeclaredField("perimeterNonFreeTiles").setLong(gVillage, 0L);
        Village.getDeclaredField("tooManyCitizens").setBoolean(gVillage, false);
        Village.getDeclaredField("isCapital").setBoolean(gVillage, false);

        Villages.getDeclaredField("FREE_TILES").setLong(null, 0L);
        Villages.getDeclaredField("TILE_UPKEEP").setLong(null, 0L);
        Villages.getDeclaredField("FREE_PERIMETER").setLong(null, 0L);
        Villages.getDeclaredField("PERIMETER_UPKEEP").setLong(null, 0L);
        Villages.getDeclaredField("MINIMUM_UPKEEP").setLong(null, 0L);
        Villages.getDeclaredField("GUARD_UPKEEP").setLong(null, 0L);

        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, true);
    }

    private void createVillage(ClassPool pool) throws Exception {
        CtClass village = pool.makeClass("com.wurmonline.server.villages.Village");
        new CtNewConstructor();
        CtConstructor ct = CtNewConstructor.make("public Village(){}", village);
        village.addConstructor(ct);
        village.addField(CtField.make("public boolean isPermanent = false;", village));
        village.addField(CtField.make("public long numTiles;", village));
        village.addField(CtField.make("public long perimeterNonFreeTiles;", village));
        village.addField(CtField.make("public boolean tooManyCitizens;", village));
        village.addField(CtField.make("public boolean isCapital;", village));

        village.addMethod(CtMethod.make("public long getNumTiles() {return this.numTiles;}", village));
        village.addMethod(CtMethod.make("public long getPerimeterNonFreeTiles() {return this.perimeterNonFreeTiles;}", village));
        village.addMethod(CtMethod.make("public boolean hasToomanyCitizens() {return this.tooManyCitizens;}", village));
        village.addMethod(CtMethod.make("public boolean isCapital() {return this.isCapital;}", village));
        // Needs to be constructed at least once to be usable?
        Village = village.toClass();
    }

    private void createVillages(ClassPool pool) throws Exception {
        CtClass villages = pool.makeClass("com.wurmonline.server.villages.Villages");

        villages.addField(CtField.make("public static long FREE_TILES = 0L;", villages));
        villages.addField(CtField.make("public static long TILE_UPKEEP = 0L;", villages));
        villages.addField(CtField.make("public static long FREE_PERIMETER = 0L;", villages));
        villages.addField(CtField.make("public static long PERIMETER_UPKEEP = 0L;", villages));
        villages.addField(CtField.make("public static long MINIMUM_UPKEEP = 0L;", villages));
        villages.addField(CtField.make("public static long GUARD_UPKEEP = 0L;", villages));
        // Needs to be constructed at least once to be usable?
        Villages = villages.toClass();
    }

    private void createGuardPlan(ClassPool pool) throws Exception {
        CtClass guardPlan = pool.makeClass("com.wurmonline.server.villages.GuardPlan");

        CtField village_instance = CtField.make("public com.wurmonline.server.villages.Village village = new com.wurmonline.server.villages.Village();", guardPlan);
        guardPlan.addField(village_instance);
        CtField logger = CtField.make("private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(com.wurmonline.server.villages.GuardPlan.class.getName());", guardPlan);
        guardPlan.addField(logger);
        CtField villageId = CtField.make("long villageId = 0L;", guardPlan);
        guardPlan.addField(villageId);
        CtField moneyLeft = CtField.make("public long moneyLeft;", guardPlan);
        guardPlan.addField(moneyLeft);
        CtField drainModifier = CtField.make("public float drainModifier;", guardPlan);
        guardPlan.addField(drainModifier);
        guardPlan.addField(CtField.make("public float maxDrainModifier;", guardPlan));
        CtField minMoneyDrained = CtField.make("public static long minMoneyDrained;", guardPlan);
        guardPlan.addField(minMoneyDrained);
        CtField monthlyCost = CtField.make("public long monthlyCost;", guardPlan);
        guardPlan.addField(monthlyCost);
        guardPlan.addField(CtField.make("public int hiredGuardNumber;", guardPlan));
        guardPlan.addField(CtField.make("public long costForGuards;", guardPlan));
        guardPlan.addField(CtField.make("public float drainCumulateFigure;", guardPlan));

        CtMethod getVillage = CtMethod.make("com.wurmonline.server.villages.Village getVillage() {return this.village;}", guardPlan);
        guardPlan.addMethod(getVillage);
        CtMethod getMonthlyCost = CtMethod.make("public long getMonthlyCost() {return this.monthlyCost;}", guardPlan);
        guardPlan.addMethod(getMonthlyCost);
        CtMethod getCostForGuards = CtMethod.make("long getCostForGuards(int guards) {return guards * com.wurmonline.server.villages.Villages.GUARD_UPKEEP;}", guardPlan);
        guardPlan.addMethod(getCostForGuards);
        CtMethod delete = CtMethod.make("void delete() {return;}", guardPlan);
        guardPlan.addMethod(delete);

        // From DbGuardPlan
        guardPlan.addField(CtField.make("public long guardPlanDrained;", guardPlan));
        guardPlan.addMethod(CtMethod.make("void drainGuardPlan(long moneyLeft) {this.guardPlanDrained = moneyLeft;return;}", guardPlan));
        guardPlan.addField(CtField.make("public boolean savedDrainMod;", guardPlan));
        guardPlan.addMethod(CtMethod.make("void saveDrainMod() {this.savedDrainMod = true;return;}", guardPlan));

        methodsToTest.forEach((def, body) -> {
                try {
                    CtMethod methodToAdd;
                    try {
                        methodToAdd = guardPlan.getDeclaredMethod(def.split(" ")[2].split("\\(")[0]);
                    } catch (NotFoundException ex) {
                        methodToAdd = CtMethod.make(def + " {return;}", guardPlan);
                        guardPlan.addMethod(methodToAdd);
                    }

                    methodToAdd.setBody(body);
                } catch (CannotCompileException ex) {
                    ex.printStackTrace();
                }
        });

        insertAftersToTest.forEach((name, insert) -> {
            try {
                guardPlan.getDeclaredMethod(name).insertAfter(insert);
            } catch (CannotCompileException | NotFoundException ex) {
                ex.printStackTrace();
            }
        });

        GuardPlan = guardPlan.toClass();
    }

    public void createOther(ClassPool pool) throws Exception {
        CtClass Servers = pool.makeClass("com.wurmonline.server.Servers");
        CtClass localServer = pool.makeClass("test.Server");
        new CtNewConstructor();
        CtConstructor ct = CtNewConstructor.make("public Server(){}", localServer);
        localServer.addConstructor(ct);
        Servers.addField(CtField.make("public static test.Server localServer = new test.Server();", Servers));
        localServer.addField(CtField.make("public static boolean isUpkeep = true;", localServer));
        localServer.addMethod(CtMethod.make("public boolean isUpkeep() {return isUpkeep;}", localServer));

        Servers.toClass();
        LocalServer = localServer.toClass();
    }
}