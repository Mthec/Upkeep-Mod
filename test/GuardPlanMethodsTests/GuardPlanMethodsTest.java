package GuardPlanMethodsTests;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.villages.*;
import javassist.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Before;
import org.mockito.internal.util.reflection.FieldSetter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

abstract class GuardPlanMethodsTest {
    static ClassReflector VillagesClass;
    static ClassReflector GuardPlanClass = new ClassReflector(GuardPlan.class);
    static Class<?> LocalServer;
    static Class<?> KingsShop;
    MyGuardPlan gPlan;
    Village gVillage;
    protected int villageId = 0;
    private static boolean firstRun = true;

    @Before
    public void setUp() throws Exception {
        if (firstRun) {
            ClassPool pool = ClassPool.getDefault();
            createVillages(pool);
            createOther(pool);
            firstRun = false;
        }
        Servers.localServer = mock(ServerEntry.class);
        gPlan = new MyGuardPlan(villageId);
        // Reset values
        Field upkeepCounter = GuardPlanClass.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        upkeepCounter.setInt(gPlan, 0);
        upkeepCounter.setAccessible(false);
        ReflectionUtil.setPrivateField(gPlan, GuardPlanClass.getDeclaredField("upkeepBuffer"), 0.0D);
        ReflectionUtil.setPrivateField(gPlan, GuardPlanClass.getDeclaredField("output"), false);
        ReflectionUtil.setPrivateField(gPlan, GuardPlanClass.getDeclaredField("type"), 0);
        Field lastSentWarning = GuardPlanClass.getDeclaredField("lastSentWarning");
        lastSentWarning.setAccessible(true);
        lastSentWarning.setLong(gPlan, 0L);
        lastSentWarning.setAccessible(false);


        gVillage = mock(Village.class);
        Map<Long, Citizen> citizens = new HashMap<>();
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("citizens"), citizens);
        Villages.class.getDeclaredMethod("addVillage", Village.class).invoke(null, gVillage);
        Field isPermanent = Village.class.getDeclaredField("isPermanent");
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(isPermanent, isPermanent.getModifiers() & ~Modifier.FINAL);
        isPermanent.setBoolean(gVillage, false);

        Villages.class.getDeclaredField("FREE_TILES").setLong(null, 0L);
        Villages.class.getDeclaredField("TILE_UPKEEP").setLong(null, 0L);
        Villages.class.getDeclaredField("FREE_PERIMETER").setLong(null, 0L);
        Villages.class.getDeclaredField("PERIMETER_UPKEEP").setLong(null, 0L);
        Villages.class.getDeclaredField("MINIMUM_UPKEEP").setLong(null, 0L);
        Villages.class.getDeclaredField("GUARD_UPKEEP").setLong(null, 0L);

        LocalServer.getDeclaredField("isUpkeep").setBoolean(null, true);
        KingsShop.getDeclaredField("money").setLong(null, 0L);
    }

    private void createVillages(ClassPool pool) throws Exception {
        CtClass villages = pool.get("com.wurmonline.server.villages.Villages");
        villages.defrost();

        villages.addField(CtField.make("public static long FREE_TILES = 0L;", villages));
        villages.addField(CtField.make("public static long FREE_PERIMETER = 0L;", villages));
        villages.addField(CtField.make("public static int FREE_GUARDS = 0;", villages));
        villages.addMethod(CtMethod.make("public static void addVillage(com.wurmonline.server.villages.Village newVillage) {villages.put((Object)Integer.valueOf(newVillage.getId()), (java.lang.Object)newVillage);}", villages));
//         Needs to be constructed at least once to be usable?
        Class<?> VillagesClazz = villages.toClass();
        VillagesClass = new ClassReflector(VillagesClazz);
    }

    private void createOther(ClassPool pool) throws Exception {
        CtClass Servers = pool.getCtClass("com.wurmonline.server.Servers");
        Servers.defrost();
        CtClass localServer = pool.makeClass("com.wurmonline.server.ServerEntry");
        localServer.defrost();
        new CtNewConstructor();
        localServer.addConstructor(CtNewConstructor.make("public Server(){}", localServer));
        //Servers.addField(CtField.make("public static com.wurmonline.server.ServerEntry localServer = new com.wurmonline.server.ServerEntry();", Servers));
        localServer.addField(CtField.make("public static boolean isUpkeep = true;", localServer));
        localServer.addField(CtField.make("public boolean PVPSERVER = false;", localServer));
        localServer.addMethod(CtMethod.make("public boolean isUpkeep() {return isUpkeep;}", localServer));
        localServer.addMethod(CtMethod.make("public boolean isChallengeOrEpicServer() {return false;}", localServer));

        Servers.toClass();
        LocalServer = localServer.toClass();

        CtClass Economy = pool.makeClass("com.wurmonline.server.economy.Economy");
        new CtNewConstructor();
        Economy.addConstructor(CtNewConstructor.make("public Economy(){}", Economy));
        Economy.addField(CtField.make("public static com.wurmonline.server.economy.Economy economy = new com.wurmonline.server.economy.Economy();", Economy));
        Economy.addField(CtField.make("public static com.wurmonline.server.economy.Shop shop = new com.wurmonline.server.economy.Shop();", Economy));
        Economy.addMethod(CtMethod.make("public static com.wurmonline.server.economy.Economy getEconomy() {return economy;}", Economy));
        Economy.addMethod(CtMethod.make("public static com.wurmonline.server.economy.Shop getKingsShop() {return shop;}", Economy));
        CtClass Shop = pool.makeClass("com.wurmonline.server.economy.Shop");
        Shop.addConstructor(CtNewConstructor.make("public Shop(){}", Shop));
        Shop.addField(CtField.make("public static long money;", Shop));
        Shop.addMethod(CtMethod.make("public long getMoney() {return this.money;}", Shop));
        Shop.addMethod(CtMethod.make("public void setMoney(long money) {this.money = money; return;}", Shop));

        Economy.toClass();
        KingsShop = Shop.toClass();
    }
}