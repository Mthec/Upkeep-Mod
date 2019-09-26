package com.wurmonline.server.villages;

import com.wurmonline.server.creatures.Creature;

import java.io.IOException;

public class MyCitizen extends Citizen {
    public MyCitizen() throws IOException {
        super(0, "", new MyVillageRole(), 0, 0);
    }

    @Override
    public void setRole(VillageRole villageRole) throws IOException {

    }

    @Override
    void setVoteDate(long l) throws IOException {

    }

    @Override
    void setVotedFor(long l) throws IOException {

    }

    @Override
    void create(Creature creature, int i) throws IOException {

    }
}