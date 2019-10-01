package com.wurmonline.server.economy;

public class MyShop extends Shop {
    public MyShop(long aWurmid, long aMoney) {
        super(aWurmid, aMoney);
        money = aMoney;
    }

    @Override
    void create() {

    }

    @Override
    boolean traderMoneyExists() {
        return false;
    }

    @Override
    public void setMoney(long l) {
        money = l;
    }

    @Override
    public void delete() {

    }

    @Override
    public void setPriceModifier(float v) {

    }

    @Override
    public void setFollowGlobalPrice(boolean b) {

    }

    @Override
    public void setUseLocalPrice(boolean b) {

    }

    @Override
    public void setLastPolled(long l) {

    }

    @Override
    public void setTax(float v) {

    }

    @Override
    public void addMoneyEarned(long l) {

    }

    @Override
    public void addMoneySpent(long l) {

    }

    @Override
    public void resetEarnings() {

    }

    @Override
    public void addTax(long l) {

    }

    @Override
    public void setOwner(long l) {

    }

    @Override
    public void setMerchantData(int i, long l) {

    }
}
