package GuardPlanMethodsTests;

import com.wurmonline.server.villages.GuardPlanMethods;
import com.wurmonline.server.villages.Villages;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class getCostForGuards extends GuardPlanMethodsTest {
    @Test
    public void testGuardCosts() throws Throwable {
        // Probably don't need a test for this, but going to do it just as a precaution.
        InvocationHandler handler = GuardPlanMethods::getCostForGuards;
        Method method = mock(Method.class);
        long guardUpkeep = 100L;
        Villages.class.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            assertEquals(guardUpkeep * i, handler.invoke(null, method, new Object[] { i }));
        }
        guardUpkeep *= 12;
        Villages.class.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            assertEquals(guardUpkeep * i, handler.invoke(null, method, new Object[] { i }));
        }
    }

    @Test
    public void testGuardPurchaseCost() throws Throwable {
        // Probably don't need a test for this, but going to do it just as a precaution.
        InvocationHandler handler = GuardPlanMethods::getCostForGuards;
        Method method = mock(Method.class);
        long guardUpkeep = 100L;
        Villages.class.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            assertEquals(guardUpkeep * i, handler.invoke(null, method, new Object[] { i }));
        }
        guardUpkeep *= 12;
        Villages.class.getDeclaredField("GUARD_UPKEEP").setLong(null, guardUpkeep);
        for (int i = 0; i <= 10; i++) {
            assertEquals(guardUpkeep * i, handler.invoke(null, method, new Object[] { i }));
        }
    }
}
