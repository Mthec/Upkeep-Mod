package GuardPlanMethodsTests;

import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.GuardPlanMethods;
import com.wurmonline.server.villages.MyGuardPlan;
import mod.wurmonline.mods.upkeepcosts.UpkeepCosts;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DrainMoney extends GuardPlanMethodsTest {
    private static Object[] args = new Object[] {};

    @Test
    public void testMaxDrain() throws Throwable {
        float maxDrainModifier = 9.0f;
        UpkeepCosts.drain_modifier_increment = 10.0f;
        UpkeepCosts.max_drain_modifier = maxDrainModifier;
        InvocationHandler handler = GuardPlanMethods::drainMoney;
        Method method = mock(Method.class);

        handler.invoke(gPlan, method, args);
        verify(method, never()).invoke(gPlan, method, args);
        assertEquals(maxDrainModifier, ReflectionUtil.getPrivateField(gPlan, GuardPlan.class.getDeclaredField("drainModifier")), 0.0001f);
    }

    @Test
    public void testDrainGuardPlanCalled() throws Throwable {
        long moneyLeft = gPlan.moneyLeft;
        long moneyDrained = gPlan.getMoneyDrained();
        InvocationHandler handler = GuardPlanMethods::drainMoney;
        Method method = mock(Method.class);

        handler.invoke(gPlan, method, args);
        verify(method, never()).invoke(gPlan, method, args);
        assertEquals(moneyLeft - moneyDrained, MyGuardPlan.class.getDeclaredField("guardPlanDrained").getLong(gPlan));
    }

    @Test
    public void testSaveDrainModCalled() throws Throwable {
        InvocationHandler handler = GuardPlanMethods::drainMoney;
        Method method = mock(Method.class);

        handler.invoke(gPlan, method, args);
        verify(method, never()).invoke(gPlan, method, args);
        assertTrue(MyGuardPlan.class.getDeclaredField("savedDrainMod").getBoolean(gPlan));
    }

    @Test
    public void testDrainAmount() throws Throwable {
        InvocationHandler handler = GuardPlanMethods::drainMoney;
        Method method = mock(Method.class);

        assertEquals(gPlan.getMoneyDrained(), handler.invoke(gPlan, method, args));
        verify(method, never()).invoke(gPlan, method, args);
    }

    @Test
    public void testDrainIncrements() throws Throwable {
        long drainCost = gPlan.getMoneyDrained();
        UpkeepCosts.drain_modifier_increment = 0.5f;
        UpkeepCosts.max_drain_modifier = 5.0f;
        InvocationHandler handler = GuardPlanMethods::drainMoney;
        Method method = mock(Method.class);

        assertEquals(drainCost, handler.invoke(gPlan, method, args));
        long newDrainCost = (long)(drainCost * 1.5);
        assertEquals(newDrainCost, handler.invoke(gPlan, method, args));
        newDrainCost = (long)(drainCost * 2.0);
        assertEquals(newDrainCost, handler.invoke(gPlan, method, args));

        verify(method, never()).invoke(gPlan, method, args);
    }
}
