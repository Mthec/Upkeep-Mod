package com.wurmonline.server.villages;

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
    public long getMonthlyCost() {
        if (monthlyCost != null)
            return monthlyCost;
        else
            return super.getMonthlyCost();
    }
}
