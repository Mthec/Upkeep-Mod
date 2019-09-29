package com.wurmonline.server.villages;

import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MyGuardPlan extends GuardPlan {
    public long guardPlanDrained = 0L;
    public boolean savedDrainMod = false;
    public int updateGuardPlan1 = -1;
    public long updateGuardPlan2 = -1;
    public int updateGuardPlan3 = -1;
    public Long monthlyCost = null;

    public MyGuardPlan(int aVillageId) {
        super(aVillageId);
    }

    @Override
    void create() {

    }

    @Override
    void load() {

    }

    @Override
    public void updateGuardPlan(int i, long l, int i1) {
        updateGuardPlan1 = i;
        updateGuardPlan2 = l;
        updateGuardPlan3 = i1;
    }

    @Override
    void delete() {

    }

    @Override
    void addReturnedGuard(long l) {

    }

    @Override
    void removeReturnedGuard(long l) {

    }

    @Override
    void saveDrainMod() {
        savedDrainMod = true;
    }

    @Override
    void deleteReturnedGuards() {

    }

    @Override
    public void addPayment(String s, long l, long l1) {

    }

    @Override
    void drainGuardPlan(long l) {
        guardPlanDrained = l;
    }

    @Override
    public long getMoneyDrained() {
        return (long)GuardPlanMethods.getMoneyDrained(this, null, null);
    }

    @Override
    public long getMonthlyCost() {
        if (monthlyCost != null)
            return monthlyCost;
        else
            return (long)GuardPlanMethods.getMonthlyCost(this, null, null);
    }

    @Override
    public long drainMoney() {
        return (long)GuardPlanMethods.drainMoney(this, null, null);
    }

    @Override
    public long getTimeLeft() {
        try {
            Method method = mock(Method.class);
            when(method.invoke(any(), any())).thenAnswer((Answer<Long>)invocationOnMock -> MyGuardPlan.super.getTimeLeft());
            return (long)GuardPlanMethods.getTimeLeft(this, method, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean pollUpkeep() throws NoSuchFieldException, IllegalAccessException {
        return (boolean)GuardPlanMethods.pollUpkeep(this, null, null);
    }
}
