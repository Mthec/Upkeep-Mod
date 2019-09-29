package GuardPlanMethodsTests;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.villages.*;
import javassist.*;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Before;
import org.mockito.internal.util.reflection.FieldSetter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

abstract class GuardPlanMethodsTest {
    static ClassReflector VillagesClass;
    static Class<?> KingsShop;
    MyGuardPlan gPlan;
    Village gVillage;
    private int villageId = 0;
    private static boolean firstRun = true;
    private ServerEntry localServer = new ServerEntry();

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
        Field upkeepCounter = GuardPlan.class.getDeclaredField("upkeepCounter");
        upkeepCounter.setAccessible(true);
        upkeepCounter.setInt(gPlan, 0);
        ReflectionUtil.setPrivateField(gPlan, GuardPlan.class.getDeclaredField("upkeepBuffer"), 0.0D);
        ReflectionUtil.setPrivateField(gPlan, GuardPlan.class.getDeclaredField("type"), 0);
        Field lastSentWarning = GuardPlan.class.getDeclaredField("lastSentWarning");
        lastSentWarning.setAccessible(true);
        lastSentWarning.setLong(gPlan, 0L);


        gVillage = mock(Village.class);
        Map<Long, Citizen> citizens = new HashMap<>();
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("citizens"), citizens);
        Villages.class.getDeclaredMethod("addVillage", Village.class).invoke(null, gVillage);
        Field isPermanent = Village.class.getDeclaredField("isPermanent");
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(isPermanent, isPermanent.getModifiers() & ~Modifier.FINAL);
        isPermanent.setBoolean(gVillage, false);

        UpkeepCosts.free_tiles = 0L;
        Villages.TILE_UPKEEP = 0L;
        UpkeepCosts.free_perimeter = 0L;
        Villages.PERIMETER_UPKEEP = 0L;
        Villages.class.getDeclaredField("MINIMUM_UPKEEP").setLong(null, 0L);
        Villages.GUARD_UPKEEP = 0L;

        localServer = new ServerEntry();
        ReflectionUtil.setPrivateField(null, Servers.class.getDeclaredField("localServer"), localServer);
        setUpkeep(true);
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("PVPSERVER"), false);
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("challengeServer"), false);
        KingsShop.getDeclaredField("money").setLong(null, 0L);
    }

    private void createVillages(ClassPool pool) throws Exception {
        CtClass villages = pool.get("com.wurmonline.server.villages.Villages");
        villages.defrost();

        villages.addMethod(CtMethod.make("public static void addVillage(com.wurmonline.server.villages.Village newVillage) {villages.put((Object)Integer.valueOf(newVillage.getId()), (java.lang.Object)newVillage);}", villages));
//         Needs to be constructed at least once to be usable?
        Class<?> VillagesClazz = villages.toClass();
        VillagesClass = new ClassReflector(VillagesClazz);
    }

    private void createOther(ClassPool pool) throws Exception {
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

    protected void setUpkeep(boolean isUpkeep) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("upkeep"), isUpkeep);
    }
}