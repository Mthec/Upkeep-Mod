package GuardPlanMethodsTests;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.villages.*;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Before;
import org.mockito.internal.util.reflection.FieldSetter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class GuardPlanMethodsTest {
    static ClassReflector VillagesClass;
    MyGuardPlan gPlan;
    Village gVillage;
    private Shop kingsShop;
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
        gPlan.moneyLeft = 1000;
        ReflectionUtil.setPrivateField(gPlan, GuardPlan.class.getDeclaredField("type"), 0);
        Field lastSentWarning = GuardPlan.class.getDeclaredField("lastSentWarning");
        lastSentWarning.setAccessible(true);
        lastSentWarning.setLong(gPlan, 0L);


        gVillage = mock(Village.class);
        Map<Long, Citizen> citizens = new HashMap<>();
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("citizens"), citizens);
        Villages.class.getDeclaredMethod("addVillage", Village.class).invoke(null, gVillage);
        FieldSetter.setField(gVillage, Village.class.getDeclaredField("isPermanent"), false);

        Villages.TILE_COST = 0L;
        Villages.TILE_UPKEEP = 0L;
        Villages.PERIMETER_COST = 0L;
        Villages.PERIMETER_UPKEEP = 0L;
        Villages.GUARD_UPKEEP = 0L;
        Villages.MINIMUM_UPKEEP = 0L;
        UpkeepCosts.epic_guard_upkeep_scaling = false;
        VillageFoundationQuestion.MINIMUM_LEFT_UPKEEP = 30000L;
        VillageFoundationQuestion.NAME_CHANGE_COST = 50000L;
        UpkeepCosts.free_tiles = 0;
        UpkeepCosts.free_perimeter = 0;
        UpkeepCosts.free_guards = 0;
        UpkeepCosts.min_drain = 7500;
        UpkeepCosts.max_drain_modifier = 5.0F;
        UpkeepCosts.drain_modifier_increment = 0.5F;

        localServer = new ServerEntry();
        ReflectionUtil.setPrivateField(null, Servers.class.getDeclaredField("localServer"), localServer);
        setUpkeep(true);
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("PVPSERVER"), false);
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("challengeServer"), false);
        Economy.getEconomy().getKingsShop().setMoney(0L);
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
//        CtClass Economy = pool.makeClass("com.wurmonline.server.economy.Economy");
//        new CtNewConstructor();
//        Economy.addConstructor(CtNewConstructor.make("public Economy(){}", Economy));
//        Economy.addField(CtField.make("public static com.wurmonline.server.economy.Economy economy = new com.wurmonline.server.economy.Economy();", Economy));
//        Economy.addField(CtField.make("public static com.wurmonline.server.economy.Shop shop = new com.wurmonline.server.economy.Shop();", Economy));
//        Economy.addMethod(CtMethod.make("public static com.wurmonline.server.economy.Economy getEconomy() {return economy;}", Economy));
//        Economy.addMethod(CtMethod.make("public static com.wurmonline.server.economy.Shop getKingsShop() {return shop;}", Economy));
//        CtClass Shop = pool.makeClass("com.wurmonline.server.economy.Shop");
//        Shop.addConstructor(CtNewConstructor.make("public Shop(){}", Shop));
//        Shop.addField(CtField.make("public static long money;", Shop));
//        Shop.addMethod(CtMethod.make("public long getMoney() {return this.money;}", Shop));
//        Shop.addMethod(CtMethod.make("public void setMoney(long money) {this.money = money; return;}", Shop));
//
//        Economy.toClass();
//        KingsShop = Shop.toClass();

        kingsShop = mock(Shop.class);
        Economy economy = mock(Economy.class);
        ReflectionUtil.setPrivateField(null, Economy.class.getDeclaredField("economy"), economy);
        when(economy.getKingsShop()).thenReturn(kingsShop);
    }

    protected void setUpkeep(boolean isUpkeep) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setPrivateField(localServer, ServerEntry.class.getDeclaredField("upkeep"), isUpkeep);
    }
}