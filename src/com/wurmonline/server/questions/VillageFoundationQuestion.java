//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// Modified from version by Code Club AB.
//

package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.*;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.villages.*;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.util.StringUtilities;

import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageFoundationQuestion extends Question implements VillageStatus, ItemTypes, MonetaryConstants, CounterTypes {
    private static final Logger logger = Logger.getLogger(VillageFoundationQuestion.class.getName());
    public int tokenx;
    public int tokeny;
    public boolean expanding = false;
    private String error = "";
    private int sequence = 0;
    public int selectedWest = 5;
    public int selectedEast = 5;
    public int selectedNorth = 5;
    public int selectedSouth = 5;
    private int diameterX;
    private int diameterY;
    private int maxGuards;
    public int selectedGuards;
    public byte dir;
    private final boolean hasCompass;
    private int tiles;
    public int initialPerimeter;
    private int perimeterDiameterX;
    private int perimeterDiameterY;
    private int perimeterTiles;
    public String motto;
    private static final int maxFactor = 4;
    public String villageName;
    private boolean permanent;
    public boolean democracy;
    public byte spawnKingdom;
    private Item deed;
    public static long MINIMUM_LEFT_UPKEEP = 30000L;
    private static long DEED_VALUE = 100000L;
    public static long NAME_CHANGE_COST = 50000L;
    public boolean surfaced;
    private long totalUpkeep;
    private boolean changingName;

    public VillageFoundationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 7, aTarget);
        this.diameterX = this.selectedWest + this.selectedEast + 1;
        this.diameterY = this.selectedNorth + this.selectedSouth + 1;
        this.maxGuards = GuardPlan.getMaxGuards(this.diameterX, this.diameterY);
        this.selectedGuards = 1;
        this.dir = 0;
        this.tiles = this.diameterX * this.diameterY;
        this.initialPerimeter = 0;
        this.perimeterDiameterX = this.diameterX + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterDiameterY = this.diameterY + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterTiles = this.perimeterDiameterX * this.perimeterDiameterY - (this.diameterX + 5 + 5) * (this.diameterY + 5 + 5);
        this.motto = "A settlement like no other.";
        this.villageName = "";
        this.permanent = false;
        this.democracy = false;
        this.spawnKingdom = 0;
        this.deed = null;
        this.totalUpkeep = 0L;
        this.changingName = false;
        this.tokenx = aResponder.getTileX();
        this.tokeny = aResponder.getTileY();
        this.surfaced = true;
        this.hasCompass = aResponder.getBestCompass() != null;
    }

    public VillageFoundationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Village aVillage, boolean aExpanding) {
        super(aResponder, aTitle, aQuestion, 7, aTarget);
        this.diameterX = this.selectedWest + this.selectedEast + 1;
        this.diameterY = this.selectedNorth + this.selectedSouth + 1;
        this.maxGuards = GuardPlan.getMaxGuards(this.diameterX, this.diameterY);
        this.selectedGuards = 1;
        this.dir = 0;
        this.tiles = this.diameterX * this.diameterY;
        this.initialPerimeter = 0;
        this.perimeterDiameterX = this.diameterX + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterDiameterY = this.diameterY + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterTiles = this.perimeterDiameterX * this.perimeterDiameterY - (this.diameterX + 5 + 5) * (this.diameterY + 5 + 5);
        this.motto = "A settlement like no other.";
        this.villageName = "";
        this.permanent = false;
        this.democracy = false;
        this.spawnKingdom = 0;
        this.deed = null;
        this.totalUpkeep = 0L;
        this.changingName = false;
        this.tokenx = aVillage.getTokenX();
        this.tokeny = aVillage.getTokenY();
        this.surfaced = aVillage.isOnSurface();
        this.initialPerimeter = aVillage.getPerimeterSize();
        this.democracy = aVillage.isDemocracy();
        this.spawnKingdom = aVillage.kingdom;
        this.motto = aVillage.getMotto();
        this.villageName = aVillage.getName();
        this.selectedWest = this.tokenx - aVillage.getStartX();
        this.selectedEast = aVillage.getEndX() - this.tokenx;
        this.selectedNorth = this.tokeny - aVillage.getStartY();
        this.selectedSouth = aVillage.getEndY() - this.tokeny;
        this.selectedGuards = aVillage.plan.getNumHiredGuards();
        this.expanding = aExpanding;
        this.hasCompass = aResponder.getBestCompass() != null;
    }

    private int getFreeTiles() {
        try {
            return (int)Villages.class.getDeclaredField("FREE_TILES").getLong(Villages.class);
        } catch(NoSuchFieldException | IllegalAccessException ex) {
            logger.warning("Free tiles not available, reason follows:");
            ex.printStackTrace();
            return 0;
        }
    }

    private int getFreePerimeter() {
        try {
            return (int)Villages.class.getDeclaredField("FREE_PERIMETER").getLong(Villages.class);
        } catch(NoSuchFieldException | IllegalAccessException ex) {
            logger.warning("Free perimeter not available, reason follows:");
            ex.printStackTrace();
            return 0;
        }
    }

    private int getChargeableTiles() {
        int tiles = this.tiles - getFreeTiles();
        if (tiles < 0)
            return 0;
        return tiles;
    }

    private int getChargeablePerimeter() {
        int perimeter = this.perimeterTiles - getFreePerimeter();
        if (perimeter < 0)
            return 0;
        return perimeter;
    }

    private int getExpansionDiff(long free, int newSize, int oldSize) {
        int diff;
        // If allowed free tiles is not exhausted.
        if (oldSize < free) {
            diff = newSize - (int)free;
            if (diff < 0)
                diff = 0;
        } else {
            diff = newSize - oldSize;
        }
        return diff;
    }

    public int getTileX() {
        return this.tokenx;
    }

    public int getTileY() {
        return this.tokeny;
    }

    public boolean isSurfaced() {
        return this.surfaced;
    }

    public void answer(Properties answers) {
        this.setAnswer(answers);
        if(this.answersFail()) {
            this.removeSettlementMarkers();
            this.removePerimeterMarkers();
        }
    }

    private boolean answersFail() {
        if(!this.checkDeedItem()) {
            return false;
        } else if(!this.checkToken()) {
            return false;
        } else {
            String val = this.getAnswer().getProperty("cancel");
            if(val != null && val.equals("true")) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to fill in the form right now.");
                return true;
            } else {
                if(this.expanding) {
                    try {
                        Village nsv = Villages.getVillage(this.deed.getData2());
                        if(nsv.plan != null && nsv.plan.isUnderSiege()) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("You can\'t do this now since the settlement is under siege.");
                            return true;
                        }
                    } catch (NoSuchVillageException var3) {
                        return true;
                    }
                }

                if(this.sequence == 0) {
                    this.createQuestion1();
                    return false;
                } else if(this.sequence == 1) {
                    this.parseVillageFoundationQuestion1();
                    return false;
                } else if(!this.checkBlockingCreatures()) {
                    return true;
                } else if(!this.checkStructuresInArea()) {
                    return true;
                } else if(!this.checkBlockingItems()) {
                    return true;
                } else {
                    this.setSize();
                    if(!this.checkTile()) {
                        return true;
                    } else if(this.sequence == 2) {
                        this.parseVillageFoundationQuestion2();
                        return false;
                    } else if(this.sequence != 6 && !this.checkSize()) {
                        return true;
                    } else {
                        if(this.sequence == 3) {
                            this.parseVillageFoundationQuestion3();
                        }

                        if(this.sequence == 4) {
                            this.parseVillageFoundationQuestion4();
                        }

                        if(this.sequence == 5) {
                            this.parseVillageFoundationQuestion5();
                        }

                        return false;
                    }
                }
            }
        }
    }

    private boolean checkTile() {
        VolaTile tile = Zones.getOrCreateTile(this.tokenx, this.tokeny, this.surfaced);
        if(tile != null) {
            int tx = tile.tilex;
            int ty = tile.tiley;
            int tt = Server.surfaceMesh.getTile(tx, ty);
            if(Tiles.decodeType(tt) == Tile.TILE_LAVA.id || Tiles.isMineDoor(Tiles.decodeType(tt))) {
                this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found a settlement here.");
                return false;
            }

            for(int x = -1; x <= 1; ++x) {
                for(int y = -1; y <= 1; ++y) {
                    int t = Server.surfaceMesh.getTile(tx + x, ty + y);
                    if(Tiles.decodeHeight(t) < 0) {
                        this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found a settlement here. Too close to water.");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void createIntro() {
        VillageFoundationQuestion vf = new VillageFoundationQuestion(this.getResponder(), "Settlement Application Form", "Welcome to the Settlement Application Form", this.target);
        if(vf != null) {
            this.copyValues(vf);
            vf.sequence = 0;
            vf.sendIntro();
        }

    }

    private void createQuestion1() {
        VillageFoundationQuestion vf = new VillageFoundationQuestion(this.getResponder(), "Settlement Size", "Stage One - The size of your settlement", this.target);
        if(vf != null) {
            this.copyValues(vf);
            vf.sequence = 1;
            vf.sendQuestion();
        }

    }

    private void createQuestion2() {
        VillageFoundationQuestion vfq = new VillageFoundationQuestion(this.getResponder(), "The Deed Perimeter", "Congratulations! You have chosen a deed that will be " + this.diameterX + " tiles by " + this.diameterY + " tiles.", this.target);
        if(vfq != null) {
            this.copyValues(vfq);
            vfq.sequence = 2;
            vfq.sendQuestion2();
        }

    }

    private void createQuestion3() {
        VillageFoundationQuestion vfq = new VillageFoundationQuestion(this.getResponder(), "Naming Your Deed", "Your survey was a success!", this.target);
        if(vfq != null) {
            this.copyValues(vfq);
            vfq.sequence = 3;
            vfq.sendQuestion3();
        }

    }

    private void createQuestion4() {
        VillageFoundationQuestion vfq = new VillageFoundationQuestion(this.getResponder(), "Guards", "Congratulations! You have reserved the name \'" + this.villageName + "\'", this.target);
        if(vfq != null) {
            this.copyValues(vfq);
            vfq.sequence = 4;
            vfq.sendQuestion4();
        }

    }

    private void createQuestion5() {
        VillageFoundationQuestion vfq = new VillageFoundationQuestion(this.getResponder(), "Check your settings", "Check your settings and Found your settlement!", this.target);
        if(vfq != null) {
            this.copyValues(vfq);
            vfq.sequence = 5;
            vfq.sendQuestion5();
        }

    }

    private void createQuestion6() {
        if(!this.expanding) {
            SoundPlayer.playSong("sound.music.song.foundsettlement", this.getResponder());
            VillageFoundationQuestion vfq = new VillageFoundationQuestion(this.getResponder(), "CONGRATULATIONS!", "CONGRATULATIONS!", this.target);
            if(vfq != null) {
                this.copyValues(vfq);
                vfq.sequence = 6;
                vfq.sendQuestion6();
            }
        }

    }

    boolean parseVillageFoundationQuestion1() {
        String key = "back";
        String val = this.getAnswer().getProperty(key);
        if(val != null && val.equals("true")) {
            this.createIntro();
            return false;
        } else {
            this.error = "";
            this.selectedWest = 5;
            this.selectedEast = 5;
            this.selectedNorth = 5;
            this.selectedSouth = 5;
            key = "sizeW";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                try {
                    this.selectedWest = Integer.parseInt(val);
                    if(this.selectedWest < 5) {
                        this.error = "The minimum size is 5. ";
                        this.selectedWest = 5;
                    }
                } catch (NumberFormatException var15) {
                    this.error = this.error + "* Failed to parse the desired size of " + val + " to a valid number. ";
                }
            }

            key = "sizeE";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                try {
                    this.selectedEast = Integer.parseInt(val);
                    if(this.selectedEast < 5) {
                        this.error = "The minimum size is 5. ";
                        this.selectedEast = 5;
                    }
                } catch (NumberFormatException var14) {
                    this.error = this.error + "* Failed to parse the desired size of " + val + " to a valid number. ";
                }
            }

            key = "sizeN";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                try {
                    this.selectedNorth = Integer.parseInt(val);
                    if(this.selectedNorth < 5) {
                        this.error = "The minimum size is 5. ";
                        this.selectedNorth = 5;
                    }
                } catch (NumberFormatException var13) {
                    this.error = this.error + "Failed to parse the desired size of " + val + " to a valid number. ";
                }
            }

            key = "sizeS";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                try {
                    this.selectedSouth = Integer.parseInt(val);
                    if(this.selectedSouth < 5) {
                        this.error = "The minimum size is 5. ";
                        this.selectedSouth = 5;
                    }
                } catch (NumberFormatException var12) {
                    this.error = this.error + "Failed to parse the desired size of " + val + " to a valid number. ";
                }
            }

            this.diameterX = this.selectedWest + this.selectedEast + 1;
            this.diameterY = this.selectedNorth + this.selectedSouth + 1;
            if((float)this.diameterX / (float)this.diameterY > 4.0F || (float)this.diameterY / (float)this.diameterX > 4.0F) {
                this.error = this.error + "The deed would be too stretched. One edge is not allowed to be more than 4 times the length of the other.";
            }

            if(this.error.length() < 1) {
                int xa = Zones.safeTileX(this.tokenx - this.selectedWest);
                int xe = Zones.safeTileX(this.tokenx + this.selectedEast);
                int ya = Zones.safeTileY(this.tokeny - this.selectedNorth);
                int ye = Zones.safeTileY(this.tokeny + this.selectedSouth);

                for(int x = xa; x <= xe; ++x) {
                    for(int y = ya; y <= ye; ++y) {
                        boolean create = false;
                        if(x == xa) {
                            if(y == ya || y == ye || y % 5 == 0) {
                                create = true;
                            }
                        } else if(x == xe) {
                            if(y == ya || y == ye || y % 5 == 0) {
                                create = true;
                            }
                        } else if((y == ya || y == ye) && x % 5 == 0) {
                            create = true;
                        }

                        if(create) {
                            try {
                                Item ex = ItemFactory.createItem(671, 80.0F, this.getResponder().getName());
                                ex.setPosXYZ((float)((x << 2) + 2), (float)((y << 2) + 2), Zones.calculateHeight((float)((x << 2) + 2), (float)((y << 2) + 2), true) + 5.0F);
                                Zones.getZone(x, y, true).addItem(ex);
                            } catch (Exception var11) {
                                logger.log(Level.INFO, var11.getMessage());
                            }
                        }
                    }
                }
            }

            this.setSize();
            if(!this.checkBlockingKingdoms()) {
                this.error = this.error + "You would be founding too close to enemy kingdom influence.";
            }

            if(this.error.length() > 0) {
                this.createQuestion1();
                return false;
            } else {
                this.createQuestion2();
                return true;
            }
        }
    }

    private void removeSettlementMarkers() {
        int xa = Zones.safeTileX(this.tokenx - this.selectedWest);
        int xe = Zones.safeTileX(this.tokenx + this.selectedEast);
        int ya = Zones.safeTileY(this.tokeny - this.selectedNorth);
        int ye = Zones.safeTileY(this.tokeny + this.selectedSouth);
        boolean notFound = false;

        for(int x = xa; x <= xe && !notFound; ++x) {
            for(int y = ya; y <= ye; ++y) {
                boolean remove = false;
                if(x == xa) {
                    if(y == ya || y == ye || y % 5 == 0) {
                        remove = true;
                    }
                } else if(x == xe) {
                    if(y == ya || y == ye || y % 5 == 0) {
                        remove = true;
                    }
                } else if((y == ya || y == ye) && x % 5 == 0) {
                    remove = true;
                }

                if(remove) {
                    try {
                        Zone nsz = Zones.getZone(x, y, true);
                        VolaTile vtile = nsz.getTileOrNull(x, y);
                        if(vtile != null) {
                            Item[] items = vtile.getItems();

                            for (Item item : items) {
                                if (item.getTemplateId() == 671) {
                                    item.removeAndEmpty();
                                    notFound = false;
                                    break;
                                }

                                notFound = true;
                            }

                            if(notFound) {
                                break;
                            }
                        }
                    } catch (NoSuchZoneException var13) {
                        logger.log(Level.INFO, var13.getMessage());
                        notFound = true;
                        break;
                    }
                }
            }
        }

    }

    private void removePerimeterMarkers() {
        int xa = Zones.safeTileX(this.tokenx - this.selectedWest - 5 - this.initialPerimeter);
        int xe = Zones.safeTileX(this.tokenx + this.selectedEast + 5 + this.initialPerimeter);
        int ya = Zones.safeTileY(this.tokeny - this.selectedNorth - 5 - this.initialPerimeter);
        int ye = Zones.safeTileY(this.tokeny + this.selectedSouth + 5 + this.initialPerimeter);
        boolean notFound = false;

        for(int x = xa; x <= xe && !notFound; ++x) {
            for(int y = ya; y <= ye; ++y) {
                boolean remove = false;
                if(x == xa) {
                    if(y == ya || y == ye || y % 10 == 0) {
                        remove = true;
                    }
                } else if(x == xe) {
                    if(y == ya || y == ye || y % 10 == 0) {
                        remove = true;
                    }
                } else if((y == ya || y == ye) && x % 10 == 0) {
                    remove = true;
                }

                if(remove) {
                    try {
                        Zone nsz = Zones.getZone(x, y, this.surfaced);
                        VolaTile tile = nsz.getTileOrNull(x, y);
                        if(tile != null) {
                            Item[] items = tile.getItems();

                            for (Item item : items) {
                                if (item.getTemplateId() == 673) {
                                    notFound = false;
                                    item.removeAndEmpty();
                                    break;
                                }

                                notFound = true;
                            }

                            if(notFound) {
                                break;
                            }
                        }
                    } catch (NoSuchZoneException var13) {
                        logger.log(Level.INFO, var13.getMessage());
                        notFound = true;
                        break;
                    }
                }
            }
        }

    }

    boolean parseVillageFoundationQuestion2() {
        String key = "back";
        String val = this.getAnswer().getProperty(key);
        if(val != null && val.equals("true")) {
            this.removeSettlementMarkers();
            this.removePerimeterMarkers();
            this.createQuestion1();
            return false;
        } else {
            this.error = "";
            key = "perimeter";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                try {
                    this.removePerimeterMarkers();
                    this.initialPerimeter = Math.max(0, Integer.parseInt(val));
                    int nsf = Zones.safeTileX(this.tokenx - this.selectedWest - 5 - this.initialPerimeter);
                    int xe = Zones.safeTileX(this.tokenx + this.selectedEast + 5 + this.initialPerimeter);
                    int ya = Zones.safeTileY(this.tokeny - this.selectedNorth - 5 - this.initialPerimeter);
                    int ye = Zones.safeTileY(this.tokeny + this.selectedSouth + 5 + this.initialPerimeter);

                    for(int x = nsf; x <= xe; ++x) {
                        for(int y = ya; y <= ye; ++y) {
                            boolean create = false;
                            if(x == nsf) {
                                if(y == ya || y == ye || y % 10 == 0) {
                                    create = true;
                                }
                            } else if(x == xe) {
                                if(y == ya || y == ye || y % 10 == 0) {
                                    create = true;
                                }
                            } else if((y == ya || y == ye) && x % 10 == 0) {
                                create = true;
                            }

                            if(create) {
                                try {
                                    Item ex = ItemFactory.createItem(673, 80.0F, this.getResponder().getName());
                                    ex.setPosXYZ((float)((x << 2) + 2), (float)((y << 2) + 2), Zones.calculateHeight((float)((x << 2) + 2), (float)((y << 2) + 2), true) + 5.0F);
                                    Zones.getZone(x, y, true).addItem(ex);
                                } catch (Exception var11) {
                                    logger.log(Level.INFO, var11.getMessage());
                                }
                            }
                        }
                    }
                } catch (NumberFormatException var12) {
                    this.error = "Failed to parse the desired perimeter of " + val + " to a valid number.";
                    return false;
                }
            }

            this.setSize();
            if(!this.checkSize()) {
                this.createQuestion2();
                return false;
            } else if(this.error.length() > 0) {
                this.createQuestion2();
                return false;
            } else {
                this.createQuestion3();
                return true;
            }
        }
    }

    boolean parseVillageFoundationQuestion3() {
        String key = "back";
        String val = this.getAnswer().getProperty(key);
        if(val != null && val.equals("true")) {
            this.createQuestion2();
            this.removePerimeterMarkers();
            return false;
        } else {
            this.error = "";
            this.villageName = this.getAnswer().getProperty("vname");
            this.villageName = this.villageName.replaceAll("\"", "");
            this.villageName = this.villageName.trim();
            if(this.villageName.length() > 3) {
                this.villageName = StringUtilities.raiseFirstLetter(this.villageName);
                StringTokenizer nsv = new StringTokenizer(this.villageName);

                String newName;
                for(newName = nsv.nextToken(); nsv.hasMoreTokens(); newName = newName + " " + StringUtilities.raiseFirstLetter(nsv.nextToken())) {
                    ;
                }

                this.villageName = newName;
            }

            if(this.expanding) {
                try {
                    Village nsv1 = Villages.getVillage(this.deed.getData2());
                    if(nsv1.mayChangeName()) {
                        if(this.villageName.length() >= 41) {
                            this.villageName = this.villageName.substring(0, 39);
                            this.error = this.error + " * The name of the settlement would be \'\'" + this.villageName + "\'\'. Please select a shorter name.";
                        } else if(this.villageName.length() < 3) {
                            this.error = this.error + " * The name of the settlement would be \'\'" + this.villageName + "\'\'. Please select a name with at least 3 letters.";
                        } else if(QuestionParser.containsIllegalVillageCharacters(this.villageName)) {
                            this.error = this.error + " * The name \'\'" + this.villageName + "\'\' contains illegal characters. Please select another name.";
                        } else if(this.villageName.equals("Wurm")) {
                            this.error = this.error + " * The name \'\'" + this.villageName + "\'\' is illegal. Please select another name.";
                        } else if(!Villages.isNameOk(this.villageName, nsv1.id)) {
                            this.error = this.error + " * The name \'\'" + this.villageName + "\'\' is already taken. Please select another name.";
                        } else if(!this.villageName.equals(nsv1.getName())) {
                            this.changingName = true;
                        }
                    }
                } catch (NoSuchVillageException var5) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("The settlement no longer exists and the operation failed.");
                    return false;
                }
            }

            this.motto = this.getAnswer().getProperty("motto");
            this.motto = this.motto.replaceAll("\"", "");
            if(this.motto.length() >= 101) {
                this.motto = this.motto.substring(0, 101);
                this.error = this.error + " * The motto of the settlement would be \'\'" + this.motto + "\'\'. Please select a shorter devise.";
            } else if(QuestionParser.containsIllegalCharacters(this.motto)) {
                this.error = this.error + " * The motto contains illegal characters. Please select another motto.";
                this.motto = "We use improper characters";
            } else if(!this.expanding) {
                if(this.villageName.length() >= 41) {
                    this.villageName = this.villageName.substring(0, 39);
                    this.error = this.error + " * The name of the settlement would be \'\'" + this.villageName + "\'\'. Please select a shorter name.";
                } else if(this.villageName.length() < 3) {
                    this.error = this.error + " * The name of the settlement would be \'\'" + this.villageName + "\'\'. Please select a name with at least 3 letters.";
                } else if(QuestionParser.containsIllegalVillageCharacters(this.villageName)) {
                    this.error = this.error + " * The name \'\'" + this.villageName + "\'\' contains illegal characters. Please select another name.";
                } else if(this.villageName.equals("Wurm")) {
                    this.error = this.error + " * The name \'\'" + this.villageName + "\'\' is illegal. Please select another name.";
                } else if(!Villages.isNameOk(this.villageName)) {
                    this.error = this.error + " * The name \'\'" + this.villageName + "\'\' is already taken. Please select another name.";
                }
            }

            this.democracy = false;
            key = "democracy";
            val = this.getAnswer().getProperty(key);
            if(val != null) {
                this.democracy = val.equals("true");
            }

            this.permanent = false;
            this.spawnKingdom = 0;
            if(this.getResponder().getPower() >= 3) {
                key = "permanent";
                val = this.getAnswer().getProperty(key);
                if(val != null) {
                    this.permanent = val.equals("true");
                }

                val = this.getAnswer().getProperty("kingdomid");
                if(val != null) {
                    this.spawnKingdom = Byte.parseByte(val);
                }
            }

            if(this.error.length() > 0) {
                this.createQuestion3();
                return false;
            } else {
                this.createQuestion4();
                return true;
            }
        }
    }

    boolean parseVillageFoundationQuestion4() {
        String key = "back";
        String val = this.getAnswer().getProperty(key);
        if(val != null && val.equals("true")) {
            this.createQuestion3();
            return false;
        } else {
            this.error = "";
            key = "guards";
            val = this.getAnswer().getProperty(key);
            if(val != null && val.length() > 0) {
                try {
                    this.selectedGuards = Math.max(0, Math.min(this.maxGuards, Integer.parseInt(val)));
                } catch (NumberFormatException var4) {
                    this.error = "Failed to parse the desired guards of " + val + " to a valid number.";
                    return false;
                }
            } else {
                this.error = this.error + "* The number of required guards MUST be specified. ";
            }

            if(this.error.length() > 0) {
                this.createQuestion4();
                return false;
            } else {
                this.createQuestion5();
                return true;
            }
        }
    }

    private long getResizeCostDiff() {
        if(Servers.localServer.isFreeDeeds()) {
            return 0L;
        } else {
            try {
                Village oldvill = Villages.getVillage(this.deed.getData2());
                long moneyNeeded = 0L;
                long moneyToRefund = 0L;

                int diffDeed = getExpansionDiff(this.getFreeTiles(), this.tiles, oldvill.getNumTiles());
                int diffPerim = getExpansionDiff(this.getFreePerimeter(), this.perimeterTiles, oldvill.getPerimeterNonFreeTiles());
                long costDeedDiff = diffDeed * Villages.TILE_COST;
                long costPerimDiff = diffPerim * Villages.PERIMETER_COST;
                long costTotalDiff = costDeedDiff + costPerimDiff;
                if(costTotalDiff > moneyToRefund) {
                    moneyNeeded += costTotalDiff;
                }

                if(this.changingName) {
                    moneyNeeded += NAME_CHANGE_COST;
                }

                int diffGuard = this.selectedGuards - oldvill.plan.getNumHiredGuards();
                if(diffGuard > 0) {
                    moneyNeeded += (long)diffGuard * Villages.GUARD_COST;
                }

                moneyNeeded -= 0L;
                return moneyNeeded;
            } catch (NoSuchVillageException var15) {
                return 0L;
            }
        }
    }

    static long getExpandMoneyNeededFromBank(long moneyNeeded, Village village) {
        long moneyAvailInPlan = village.getAvailablePlanMoney();
        return moneyNeeded > moneyAvailInPlan?moneyNeeded - moneyAvailInPlan:0L;
    }

    private long getFoundingCost() {
        long moneyNeeded = this.getChargeableTiles() * Villages.TILE_COST;
        moneyNeeded += this.getChargeablePerimeter() * Villages.PERIMETER_COST;
        moneyNeeded += (long)this.selectedGuards * Villages.GUARD_COST;
        return moneyNeeded;
    }

    private long getFoundingCharge() {
        return Servers.localServer.isFreeDeeds()?0L:this.getFoundingCost() + MINIMUM_LEFT_UPKEEP - (this.deed.getTemplateId() == 862?0L:DEED_VALUE);
    }

    boolean parseVillageFoundationQuestion5() {
        String key = "back";
        String val = this.getAnswer().getProperty(key);
        if(val != null && val.equals("true")) {
            this.createQuestion4();
            return false;
        } else {
            long moneyNeeded = this.getFoundingCost();
            Village oldvill = null;
            if(this.expanding) {
                try {
                    oldvill = Villages.getVillage(this.deed.getData2());
                } catch (NoSuchVillageException var19) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("The settlement no longer exists. The settlement form was cancelled.");
                    return false;
                }
            }

            if(this.expanding) {
                moneyNeeded = this.getResizeCostDiff();
            }

            try {
                if(!this.expanding) {
                    long iox = this.getFoundingCharge();
                    boolean charge = iox > 0L;
                    long left = this.deed.getTemplateId() == 862?MINIMUM_LEFT_UPKEEP:0L;
                    if(iox < 0L) {
                        left = DEED_VALUE - this.getFoundingCost();
                    }

                    if(charge && !((Player)this.getResponder()).chargeMoney(iox)) {
                        Change funds1 = new Change(iox);
                        this.getResponder().getCommunicator().sendAlertServerMessage("You do not have the required " + funds1.getChangeString() + " available in your bank account.");
                        this.removeSettlementMarkers();
                        this.removePerimeterMarkers();
                        return false;
                    }

                    try {
                        Village funds = Villages.createVillage(Zones.safeTileX(this.tokenx - this.selectedWest), Zones.safeTileX(this.tokenx + this.selectedEast), Zones.safeTileY(this.tokeny - this.selectedNorth), Zones.safeTileY(this.tokeny + this.selectedSouth), this.tokenx, this.tokeny, this.villageName, this.getResponder(), this.target, this.surfaced, this.democracy, this.motto, this.permanent, this.spawnKingdom, this.initialPerimeter);
                        logger.log(Level.INFO, this.getResponder().getName() + " founded " + this.villageName + " for " + iox + " irons.");
                        Server.getInstance().broadCastSafe(WurmCalendar.getTime(), false);
                        Server.getInstance().broadCastSafe("The settlement of " + this.villageName + " has just been founded by " + this.getResponder().getName() + ".");
                        this.getResponder().getCommunicator().sendSafeServerMessage("The settlement of " + this.villageName + " has been founded according to your specifications!");
                        this.deed.setDescription(this.villageName);
                        this.deed.setName("Settlement deed");
                        funds.setIsHighwayAllowed(funds.hasHighway());
                        if(this.getResponder().getPower() < 5 && this.deed.getTemplateId() != 862) {
                            Shop shop = Economy.getEconomy().getKingsShop();
                            shop.setMoney(shop.getMoney() - (long)((int)((float)this.deed.getValue() * 0.4F)));
                        }

                        if(left > 0L) {
                            funds.plan.updateGuardPlan(0, left, this.selectedGuards);
                        } else {
                            funds.plan.updateGuardPlan(0, MINIMUM_LEFT_UPKEEP, this.selectedGuards);
                        }

                        this.createQuestion6();
                    } catch (FailedException var13) {
                        this.getResponder().getCommunicator().sendSafeServerMessage(var13.getMessage());
                        return false;
                    } catch (NoSuchPlayerException var14) {
                        logger.log(Level.WARNING, "Failed to create settlement: " + var14.getMessage(), var14);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate a resource needed for that request. Please contact administration.");
                        return false;
                    } catch (NoSuchCreatureException var15) {
                        logger.log(Level.WARNING, "Failed to create settlement: " + var15.getMessage(), var15);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate a resource needed for that request. Please contact administration.");
                        return false;
                    } catch (IOException var16) {
                        logger.log(Level.WARNING, "Failed to create settlement:" + var16.getMessage(), var16);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate a resource needed for that request. Please contact administration.");
                        return false;
                    } catch (NoSuchRoleException var17) {
                        logger.log(Level.WARNING, "Failed to create settlement:" + var17.getMessage(), var17);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate a role needed for that request. Please contact administration.");
                        return false;
                    } catch (NoSuchItemException var18) {
                        logger.log(Level.WARNING, "Failed to create settlement:" + var18.getMessage(), var18);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to located the deed. The operation was aborted.");
                        return false;
                    }
                } else {
                    if(oldvill == null) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The settlement no longer exists.");
                        return false;
                    }

                    boolean iox1 = true;
                    if(!this.permanent && moneyNeeded > 0L) {
                        long builder1 = getExpandMoneyNeededFromBank(moneyNeeded, oldvill);
                        if(builder1 > 0L) {
                            if(Servers.localServer.testServer) {
                                this.getResponder().getCommunicator().sendNormalServerMessage("We need " + moneyNeeded + ". " + builder1 + " must be taken from the bank.");
                            }

                            if(!((Player)this.getResponder()).chargeMoney(builder1)) {
                                this.error = "You try to change the settlement size, but your bank account could not be charged. The action was aborted.";
                                this.removeSettlementMarkers();
                                this.removePerimeterMarkers();
                                return false;
                            }

                            if(Servers.localServer.testServer) {
                                this.getResponder().getCommunicator().sendNormalServerMessage("We also take " + oldvill.getAvailablePlanMoney() + " from upkeep.");
                            }

                            oldvill.plan.updateGuardPlan(oldvill.plan.moneyLeft - oldvill.getAvailablePlanMoney());
                        } else {
                            if(Servers.localServer.testServer) {
                                this.getResponder().getCommunicator().sendNormalServerMessage("We charge " + moneyNeeded + " from the plan which has " + oldvill.plan.moneyLeft);
                            }

                            oldvill.plan.updateGuardPlan(oldvill.plan.moneyLeft - moneyNeeded);
                        }
                    } else if(moneyNeeded < 0L) {
                        LoginServerWebConnection builder = new LoginServerWebConnection();
                        if(!builder.addMoney(this.getResponder().getWurmId(), this.getResponder().getName(), Math.abs(moneyNeeded), "Resize " + oldvill.getName())) {
                            iox1 = false;
                            logger.log(Level.INFO, "Changing values did not yield money for " + oldvill.getName() + " to " + this.getResponder().getName() + ": " + Math.abs(moneyNeeded) + "?");
                            this.getResponder().getCommunicator().sendAlertServerMessage("You try to change the settlement size, but you could not be reimbursed because we could not reach your bank account. The action was aborted.");
                        }
                    }

                    if(iox1) {
                        StringBuilder builder2 = new StringBuilder();
                        if(oldvill.getPerimeterSize() != this.initialPerimeter) {
                            builder2.append("Perimeter ").append(this.initialPerimeter).append(" ");
                            oldvill.setPerimeter(this.initialPerimeter);
                        }

                        if(oldvill.plan.getNumHiredGuards() != this.selectedGuards) {
                            builder2.append("Guards ").append(this.selectedGuards).append(" ");
                            oldvill.plan.changePlan(0, this.selectedGuards);
                        }

                        if(oldvill.getStartX() != this.tokenx - this.selectedWest || oldvill.getStartY() != this.tokeny - this.selectedNorth || oldvill.getEndX() != this.tokenx + this.selectedEast || oldvill.getEndY() != this.tokeny + this.selectedSouth) {
                            builder2.append("From token: ").append(this.selectedWest).append(" West to ").append(this.selectedEast).append(" East and ");
                            builder2.append(this.selectedNorth).append(" North to ").append(this.selectedSouth).append(" South ");
                            oldvill.setNewBounds(Zones.safeTileX(oldvill.getTokenX() - this.selectedWest), Zones.safeTileY(oldvill.getTokenY() - this.selectedNorth), Zones.safeTileX(oldvill.getTokenX() + this.selectedEast), Zones.safeTileY(oldvill.getTokenY() + this.selectedSouth));
                        }

                        if(!oldvill.getMotto().equals(this.motto)) {
                            oldvill.addHistory(this.getResponder().getName(), "New motto: " + this.motto);
                            oldvill.setMotto(this.motto);
                        }

                        if(oldvill.isDemocracy() != this.democracy) {
                            oldvill.setDemocracy(this.democracy);
                            builder2.append("Democracy: ").append(this.democracy);
                        }

                        if(this.changingName) {
                            oldvill.setName(this.villageName);
                            oldvill.setFaithCreate(0.0F);
                            oldvill.setFaithHeal(0.0F);
                            oldvill.setFaithWar(0.0F);
                            oldvill.setLastChangedName(System.currentTimeMillis());
                            this.deed.setDescription(this.villageName);
                            builder2.append("Changed name to ").append(this.villageName);
                        }

                        oldvill.addHistory(this.getResponder().getName(), builder2.toString());
                        this.getResponder().getCommunicator().sendSafeServerMessage("The settlement has been updated according to your specifications!");
                    }
                }
            } catch (IOException var20) {
                logger.log(Level.INFO, "Failed to create settlement:" + var20.getMessage(), var20);
                this.getResponder().getCommunicator().sendNormalServerMessage("Failed to charge your account. The operation was aborted.");
                return false;
            }

            if(this.error.length() > 0) {
                this.createQuestion5();
                return false;
            } else {
                this.removeSettlementMarkers();
                this.removePerimeterMarkers();
                return true;
            }
        }
    }

    private boolean checkSize() {
        Iterator var3;
        if(this.getResponder().getPower() < 3) {
            if (Feature.TOWER_CHAINING.isEnabled()) {
                InfluenceChain chain = InfluenceChain.getInfluenceChain(this.getResponder().getKingdomId());
                boolean found = false;
                var3 = chain.getChainMarkers().iterator();

                label108:
                while(true) {
                    Item marker;
                    do {
                        if (!var3.hasNext()) {
                            break label108;
                        }

                        marker = (Item)var3.next();
                    } while(!marker.isChained() && this.getResponder().getKingdomId() != 4);

                    if (marker.isGuardTower() && Math.abs(this.tokenx - marker.getTileX()) <= 50 && Math.abs(this.tokeny - marker.getTileY()) <= 50) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    this.getResponder().getCommunicator().sendSafeServerMessage("You must found the settlement within 50 tiles of an allied tower linked to the kingdom influence chain.");
                    return false;
                }
            } else {
                boolean found = false;

                for(int x = this.tokenx - 50; x <= this.tokenx + 50; x += 10) {
                    for(int y = this.tokeny - 50; y <= this.tokeny + 50; y += 10) {
                        if (Zones.getKingdom(this.tokenx, this.tokeny) == this.getResponder().getKingdomId()) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    this.getResponder().getCommunicator().sendSafeServerMessage("You must found the settlement within 50 tiles of your own kingdom.");
                    return false;
                }
            }
        }

        Village oldvill = null;
        if(this.expanding) {
            try {
                oldvill = Villages.getVillage(this.deed.getData2());
            } catch (NoSuchVillageException var6) {
                this.getResponder().getCommunicator().sendSafeServerMessage("The settlement could not be located.");
                return false;
            }
        }

        Map<Village, String> decliners = Villages.canFoundVillage(this.selectedWest, this.selectedEast, this.selectedNorth, this.selectedSouth, this.tokenx, this.tokeny, this.initialPerimeter, true, oldvill, this.getResponder());
        if (!decliners.isEmpty()) {
            this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here:");
            Iterator focusZoneReject1 = decliners.keySet().iterator();

            while(focusZoneReject1.hasNext()) {
                Village checkFocusZones2 = (Village)focusZoneReject1.next();
                String reason = (String) decliners.get(checkFocusZones2);
                if (reason.startsWith("has perimeter")) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(checkFocusZones2.getName() + " " + reason);
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage("Some settlement nearby " + reason);
                }
            }

            return false;
        } else {
            boolean checkFocusZones = !this.expanding;
            if (this.expanding && oldvill != null) {
                if (oldvill.getStartX() != this.tokenx - this.selectedWest) {
                    checkFocusZones = true;
                }

                if (oldvill.getStartY() != this.tokeny - this.selectedNorth) {
                    checkFocusZones = true;
                }

                if (oldvill.getEndX() != this.tokenx + this.selectedEast) {
                    checkFocusZones = true;
                }

                if (oldvill.getEndY() != this.tokenx + this.selectedNorth) {
                    checkFocusZones = true;
                }

                if (oldvill.getPerimeterDiameterX() != this.perimeterDiameterX) {
                    checkFocusZones = true;
                }

                if (oldvill.getPerimeterDiameterY() != this.perimeterDiameterY) {
                    checkFocusZones = true;
                }
            }

            if (checkFocusZones) {
                String focusZoneReject = Villages.isFocusZoneBlocking(this.selectedWest, this.selectedEast, this.selectedNorth, this.selectedSouth, this.tokenx, this.tokeny, this.initialPerimeter, true);
                if(focusZoneReject.length() > 0) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(focusZoneReject);
                    return false;
                }
            }

            return true;
        }
    }

    public void setSize() {
        this.diameterX = this.selectedWest + this.selectedEast + 1;
        this.diameterY = this.selectedNorth + this.selectedSouth + 1;
        this.maxGuards = GuardPlan.getMaxGuards(this.diameterX, this.diameterY);
        this.tiles = this.diameterX * this.diameterY;
        this.perimeterDiameterX = this.diameterX + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterDiameterY = this.diameterY + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        this.perimeterTiles = this.perimeterDiameterX * this.perimeterDiameterY - (this.diameterX + 5 + 5) * (this.diameterY + 5 + 5);
    }

    private boolean checkToken() {
        if(!Villages.mayCreateTokenOnTile(true, this.tokenx, this.tokeny)) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You may not found the settlement there. A fence or a wall is nearby. The token is a public affair and must not be restricted access.");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkDeedItem() {
        try {
            this.deed = Items.getItem(this.target);
            if(!this.deed.isNewDeed() && !Servers.localServer.testServer && this.deed.getTemplateId() != 862) {
                this.getResponder().getCommunicator().sendNormalServerMessage("This " + this.deed.getName() + " may no longer be used.");
                return false;
            } else {
                return true;
            }
        } catch (NoSuchItemException var2) {
            logger.log(Level.WARNING, "Failed to locate settlement deed with id " + this.target, var2);
            this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
            return false;
        }
    }

    private boolean checkBlockingCreatures() {
        Village currVill = null;
        if(this.expanding) {
            try {
                currVill = Villages.getVillage(this.deed.getData2());
            } catch (NoSuchVillageException var3) {
                return false;
            }
        }

        Object cret = Villages.isAggOnDeed(currVill, this.getResponder(), this.selectedWest, this.selectedEast, this.selectedNorth, this.selectedSouth, this.tokenx, this.tokeny, true);
        if(cret != null) {
            this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since there are dangerous aggressive creatures or dens in the area.");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkBlockingKingdoms() {
        if(!Servers.localServer.testServer && this.getResponder().getPower() < 2) {
            int mindist = Kingdoms.minKingdomDist;
            if(Zones.getKingdom(this.tokenx, this.tokeny) == this.getResponder().getKingdomId()) {
                mindist = 60;
            }

            int eZoneStartX = -1;
            int eZoneStartY = -1;
            int eZoneEndX = -1;
            int eZoneEndY = -1;
            Village existing = Villages.getVillage(this.tokenx, this.tokeny, true);
            if (existing != null) {
                eZoneStartX = existing.getStartX() - 5 - existing.getPerimeterSize() - mindist;
                eZoneStartY = existing.getStartY() - 5 - existing.getPerimeterSize() - mindist;
                eZoneEndX = existing.getEndX() + 5 + existing.getPerimeterSize() + mindist;
                eZoneEndY = existing.getEndY() + 5 + existing.getPerimeterSize() + mindist;
            }

            return Zones.isKingdomBlocking(this.tokenx - this.selectedWest - 5 - this.initialPerimeter - mindist, this.tokeny - this.selectedNorth - 5 - this.initialPerimeter - mindist, this.tokenx + this.selectedEast + 5 + this.initialPerimeter + mindist, this.tokeny + this.selectedSouth + 5 + this.initialPerimeter + mindist, this.getResponder().getKingdomId(), eZoneStartX, eZoneStartY, eZoneEndX, eZoneEndY);
        } else {
            return true;
        }
    }

    private boolean checkBlockingItems() {
        if(this.getResponder().getPower() < 2) {
            int maxnorth = Math.max(0, this.tokeny - this.selectedNorth - (Servers.localServer.isChallengeServer()?20:Kingdoms.minKingdomDist));
            int maxsouth = Math.min(Zones.worldTileSizeY, this.tokeny + this.selectedSouth + (Servers.localServer.isChallengeServer()?20:Kingdoms.minKingdomDist));
            int maxwest = Math.max(0, this.tokenx - this.selectedWest - (Servers.localServer.isChallengeServer()?20:Kingdoms.minKingdomDist));
            int maxeast = Math.min(Zones.worldTileSizeX, this.tokenx + this.selectedEast + (Servers.localServer.isChallengeServer()?20:Kingdoms.minKingdomDist));
            int maxcnorth = Math.max(0, this.tokeny - this.selectedNorth - (Servers.localServer.isChallengeServer()?20:60));
            int maxcsouth = Math.min(Zones.worldTileSizeY, this.tokeny + this.selectedSouth + (Servers.localServer.isChallengeServer()?20:60));
            int maxcwest = Math.max(0, this.tokenx - this.selectedWest - (Servers.localServer.isChallengeServer()?20:60));
            int maxceast = Math.min(Zones.worldTileSizeX, this.tokenx + this.selectedEast + (Servers.localServer.isChallengeServer()?20:60));
            new Float((float)maxwest, (float)maxnorth, (float)(maxeast - maxwest), (float)(maxsouth - maxnorth));
            Item[] var10 = Items.getWarTargets();
            int var11 = var10.length;

            int var12;
            Item targ;
            for(var12 = 0; var12 < var11; ++var12) {
                targ = var10[var12];
                if (targ.getTileX() > maxcwest && targ.getTileX() < maxceast && targ.getTileY() < maxcsouth && targ.getTileY() > maxcnorth) {
                    this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since this is a battle ground.");
                    return false;
                }
            }

            var10 = Items.getSupplyDepots();
            var11 = var10.length;

            for(var12 = 0; var12 < var11; ++var12) {
                targ = var10[var12];
                if (targ.getTileX() > maxcwest && targ.getTileX() < maxceast && targ.getTileY() < maxcsouth && targ.getTileY() > maxcnorth) {
                    this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since this is a battle ground.");
                    return false;
                }
            }

            Item altar = Villages.isAltarOnDeed(this.selectedWest, this.selectedEast, this.selectedNorth, this.selectedSouth, this.tokenx, this.tokeny, true);
            if(altar != null) {
                if (!altar.isEpicTargetItem() || !Servers.localServer.PVPSERVER) {
                this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since the " + altar.getName() + " makes it holy ground.");
                return false;
            }

                if (EpicServerStatus.getRitualMissionForTarget(altar.getWurmId()) != null) {
                    this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since the " + altar.getName() + " is currently required for an active mission.");
                    return false;
                }
            }

            EndGameItem alt = EndGameItems.getGoodAltar();
            if (alt != null && alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 > maxwest && (int)alt.getItem().getPosX() >> 2 < maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since this is holy ground.");
                return false;
            }

            alt = EndGameItems.getEvilAltar();
            if (alt != null && alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 > maxwest && (int)alt.getItem().getPosX() >> 2 < maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since this is holy ground.");
                return false;
            }

            if(Zones.isWithinDuelRing(this.tokenx - this.selectedWest - 5 - this.initialPerimeter, this.tokeny - this.selectedNorth - 5 - this.initialPerimeter, this.tokenx + this.selectedEast + 5 + this.initialPerimeter, this.tokeny + this.selectedSouth + 5 + this.initialPerimeter)) {
                this.getResponder().getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since the duelling ring is holy ground.");
                return false;
            }
        }

        return true;
    }

    private boolean checkStructuresInArea() {
        if(this.getResponder().getPower() <= 1) {
            ArrayList<Structure> newStructures = new ArrayList();
            ArrayList<Structure> oldStructures = new ArrayList();
            Structure[] surfStructures = Zones.getStructuresInArea(this.tokenx - this.selectedWest - 2, this.tokeny - this.selectedNorth - 2, this.tokenx + this.selectedEast + 2, this.tokeny + this.selectedSouth + 2, true);
            Structure[] caveStructures = Zones.getStructuresInArea(this.tokenx - this.selectedWest - 2, this.tokeny - this.selectedNorth - 2, this.tokenx + this.selectedEast + 2, this.tokeny + this.selectedSouth + 2, false);
            Structure[] var5 = surfStructures;
            int var6 = surfStructures.length;

            int var7;
            Structure c;
            for(var7 = 0; var7 < var6; ++var7) {
                c = var5[var7];
                newStructures.add(c);
            }

            var5 = caveStructures;
            var6 = caveStructures.length;

            for(var7 = 0; var7 < var6; ++var7) {
                c = var5[var7];
                newStructures.add(c);
            }

            if(this.expanding) {
                try {
                    Village oldvill = Villages.getVillage(this.deed.getData2());
                    Structure[] surfStructuresOld = Zones.getStructuresInArea(oldvill.getStartX(), oldvill.getStartY(), oldvill.getEndX(), oldvill.getEndY(), true);
                    Structure[] caveStructuresOld = Zones.getStructuresInArea(oldvill.getStartX(), oldvill.getStartY(), oldvill.getEndX(), oldvill.getEndY(), true);
                    Structure[] var18 = surfStructuresOld;
                    int var9 = surfStructuresOld.length;

                    int var10;
                    for(var10 = 0; var10 < var9; ++var10) {
                        c = var18[var10];
                        oldStructures.add(c);
                    }

                    var18 = caveStructuresOld;
                    var9 = caveStructuresOld.length;

                    for(var10 = 0; var10 < var9; ++var10) {
                        c = var18[var10];
                        oldStructures.add(c);
                    }
                } catch (NoSuchVillageException var12) {
                    ;
                }
            }

            if (newStructures.size() > 0) {
                boolean ok = false;
                Iterator var16 = newStructures.iterator();

                while(var16.hasNext()) {
                    Structure lStructure = (Structure)var16.next();
                    ok = false;
                    if (this.expanding) {
                        Iterator var20 = oldStructures.iterator();

                        while(var20.hasNext()) {
                            Structure lOldstructure = (Structure)var20.next();
                            if (lOldstructure.getWurmId() == lStructure.getWurmId()) {
                                ok = true;
                                break;
                            }
                        }
                    }

                    if (!ok && lStructure.isTypeHouse() && !lStructure.mayManage(this.getResponder())) {
                        this.getResponder().getCommunicator().sendSafeServerMessage("You need to have manage permissions for the structure " + lStructure.getName() + " to found the settlement here.");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void addTileCost(StringBuilder buf) {
        buf.append("text{text=\"You selected a size of ").append(this.diameterX).append(" by ").append(this.diameterY).append(".\"}");
        if(!this.expanding) {
            if(!Servers.localServer.isFreeDeeds()) {
                buf.append("text{text=\"The Purchase price for these tiles are ").append((new Change(this.getChargeableTiles() * Villages.TILE_COST)).getChangeString()).append(".\"}");
            }

            if(Servers.localServer.isUpkeep()) {
                buf.append("text{text=\"The Monthly upkeep is ").append((new Change(this.getChargeableTiles() * Villages.TILE_UPKEEP)).getChangeString()).append(".\"}");
            }

            buf.append("text{text=\"\"}");
        } else {
            try {
                Village oldvill = Villages.getVillage(this.deed.getData2());
                int diff = getExpansionDiff(this.getFreeTiles(), this.tiles, oldvill.getNumTiles());

                if(diff > 0 && !Servers.localServer.isFreeDeeds()) {
                    buf.append("text{text=\"The initial cost for the tiles will be ").append((new Change(((long)diff) * Villages.TILE_COST)).getChangeString()).append(".\"}");
                }

                if(Servers.localServer.isUpkeep()) {
                    buf.append("text{text=\"The new monthly upkeep cost for the tiles will be ").append((new Change(this.getChargeableTiles() * Villages.TILE_UPKEEP)).getChangeString()).append(".\"}");
                }

                buf.append("text{text=\"\"}");
            } catch (NoSuchVillageException var4) {
                buf.append("text{text=\"The settlement for this deed was not found, so nothing will happen.\"}");
            }
        }

        this.totalUpkeep = this.getChargeableTiles() * Villages.TILE_UPKEEP;
    }

    private void addChangeNameCost(StringBuilder buf) {
        if(this.changingName && !Servers.localServer.isFreeDeeds()) {
            buf.append("text{text=\"The cost for changing name is ").append(new Change(NAME_CHANGE_COST).getChangeString()).append(".\"}");
        }

    }

    private void addPerimeterCost(StringBuilder buf) {
        if(!this.expanding) {
            buf.append("text{text=\"You have selected a perimeter of 5").append(Servers.localServer.isFreeDeeds() ? "" : " free").append(" tiles plus ").append(this.initialPerimeter).append(" additional tiles from your settlement boundary.\"}");
            if(this.initialPerimeter > 0) {
                if(!Servers.localServer.isFreeDeeds()) {
                    buf.append("text{text=\"The initial cost for the perimeter tiles will be ").append((new Change(this.getChargeablePerimeter() * Villages.PERIMETER_COST)).getChangeString()).append(".\"}");
                }

                if(Servers.localServer.isUpkeep()) {
                    buf.append("text{text=\"The monthly upkeep cost for the perimeter tiles will be ").append((new Change(this.getChargeablePerimeter() * Villages.PERIMETER_UPKEEP)).getChangeString()).append(".\"}");
                }
            }

            buf.append("text{text=\"\"}");
        } else {
            try {
                buf.append("text{text=\"You selected a perimeter size of ").append(this.initialPerimeter).append(" outside of the free ").append(5).append(" tiles.\"}");
                Village oldvill = Villages.getVillage(this.deed.getData2());
                int diff = getExpansionDiff(this.getFreePerimeter(), this.perimeterTiles, oldvill.getPerimeterNonFreeTiles());

                if(diff > 0 && !Servers.localServer.isFreeDeeds()) {
                    buf.append("text{text=\"The additional cost for the extra perimeter tiles will be ").append((new Change(((long)diff) * Villages.PERIMETER_COST)).getChangeString()).append(".\"}");
                }

                if(Servers.localServer.isUpkeep()) {
                    if(this.initialPerimeter > 0) {
                        buf.append("text{text=\"The new monthly upkeep cost for the perimeter tiles will be ").append((new Change(this.getChargeablePerimeter() * Villages.PERIMETER_UPKEEP)).getChangeString()).append(".\"}");
                    } else if(diff < 0) {
                        buf.append("text{text=\"The monthly upkeep cost for perimeter tiles will go away now.\"}");
                    }
                }

                buf.append("text{text=\"\"}");
            } catch (NoSuchVillageException var4) {
                buf.append("text{text=\"The settlement for this deed was not found, so nothing will happen.\"}");
            }
        }

        this.totalUpkeep += this.getChargeablePerimeter() * Villages.PERIMETER_UPKEEP;
    }

    private void addCitizenMultiplier(StringBuilder buf) {
        buf.append("text{type=\"bold\";text=\"Notes\"}");
        buf.append("text{type=\"italic\";text=\"The maximum number of citizens, including guards, is ").append(this.tiles / 11).append("\"}");
        if(!Servers.isThisAPvpServer()) {
            buf.append("text{type=\"italic\";text=\"The maximum number of branded animals is ").append(this.tiles / 11).append("\"}");
        }

        buf.append("text{text=\"\"}");
        if(this.expanding) {
            try {
                Village nsv = Villages.getVillage(this.deed.getData2());
                int curr = 0;

                for(Citizen citizen : nsv.getCitizens()) {
                    if(WurmId.getType(citizen.wurmId) == 0) {
                        ++curr;
                    }
                }

                buf.append("text{text=\"You have ").append(curr).append(" player citizens.\"}");
                if(Servers.localServer.isUpkeep()) {
                    if(curr > this.tiles / 11) {
                        buf.append("text{text=\"Since you have more than the max amount of citizens (").append(this.tiles / 11).append("), upkeep costs are doubled.\"}");
                        this.totalUpkeep *= 2L;
                    } else {
                        buf.append("text{text=\"If you exceed the max amount of citizens (").append(this.tiles / 11).append("), upkeep costs will be doubled.\"}");
                    }
                }
            } catch (NoSuchVillageException var9) {
                logger.log(Level.WARNING, var9.getMessage());
            }
        } else if(Servers.localServer.isUpkeep()) {
            buf.append("text{text=\"If you exceed the max amount of citizens (").append(this.tiles / 11).append("), upkeep costs will be doubled.\"}");
        }

        if(this.totalUpkeep < Villages.MINIMUM_UPKEEP && Servers.localServer.isUpkeep()) {
            this.totalUpkeep = Villages.MINIMUM_UPKEEP;
            buf.append("text{text=\"Upkeep is always minimum ").append(Villages.MINIMUM_UPKEEP_STRING).append(".\"}");
        }

    }

    private void addTotalUpkeep(StringBuilder buf) {
        if(Servers.localServer.isUpkeep()) {
            buf.append("text{text=\"Total upkeep per month will be ").append((new Change(this.totalUpkeep)).getChangeString()).append(".\"}");
        }

    }

    private void addGuardCost(StringBuilder buf) {
        if(!this.expanding) {
            if(this.selectedGuards > 0) {
                if(Servers.localServer.isFreeDeeds()) {
                    buf.append("text{text=\"You will hire ").append(this.selectedGuards).append(" guards.\"}");
                } else {
                    buf.append("text{text=\"You will hire ").append(this.selectedGuards).append(" guards for a cost of ").append((new Change((long) this.selectedGuards * Villages.GUARD_COST)).getChangeString()).append(".\"}");
                }

                if(Servers.localServer.isUpkeep()) {
                    buf.append("text{text=\"The guard upkeep will be ").append((new Change(GuardPlan.getCostForGuards(this.selectedGuards))).getChangeString()).append(".\"}");
                }
            } else {
                buf.append("text{text=\"You decide to hire no guards right now.\"}");
            }

            buf.append("text{text=\"\"}");
        } else {
            try {
                Village nsv = Villages.getVillage(this.deed.getData2());
                int diff = this.selectedGuards - nsv.plan.getNumHiredGuards();
                if(diff > 0) {
                    if(Servers.localServer.isFreeDeeds()) {
                        buf.append("text{text=\"You will hire ").append(diff).append(" new guards.\"}");
                    } else {
                        buf.append("text{text=\"You will hire ").append(diff).append(" new guards for a cost of ").append((new Change((long) diff * Villages.GUARD_COST)).getChangeString()).append(".\"}");
                    }

                    if(Servers.localServer.isUpkeep()) {
                        buf.append("text{text=\"The new guard upkeep will be ").append((new Change(GuardPlan.getCostForGuards(this.selectedGuards))).getChangeString()).append(".\"}");
                    }
                } else {
                    buf.append("text{text=\"You will dismiss ").append(Math.abs(diff)).append(" guards.\"}");
                    if(Servers.localServer.isUpkeep()) {
                        buf.append("text{text=\"The new guard upkeep will be ").append((new Change(GuardPlan.getCostForGuards(this.selectedGuards))).getChangeString()).append(".\"}");
                    }
                }

                buf.append("text{text=\"\"}");
            } catch (NoSuchVillageException var4) {
                buf.append("text{text=\"The settlement for this deed was not found, so nothing will happen.\"}");
            }
        }

        this.totalUpkeep += GuardPlan.getCostForGuards(this.selectedGuards);
    }

    public void sendIntro() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        String fs = "founding";
        if(this.expanding) {
            fs = "resizing";
        }

        buf.append("text{type=\"italic\";text=\"This form will take you through the process of ").append(fs).append(" your settlement. You will be asked how large you want your settlement to be, if you want to buy a larger perimeter, what you want to call your new settlement and how many guards you wish to hire. Please be aware that there is no refund for purchased tiles.\"}");
        buf.append("text{text=\"\"}");
        if(Servers.localServer.isChallengeOrEpicServer()) {
            buf.append("text{type=\"bold\";color=\"255,0,0\";text=\"Note that deeds in the Epic and Challenge cluster may become harmed by natural events. It is rare but may happen.\"}");
            buf.append("text{type=\"bold\";color=\"255,0,0\";text=\"In case you are not prepared for this, please resell this form.\"}");
            buf.append("text{text=\"\"}");
        }

        buf.append("text{type=\"bold\";text=\"What you need first:\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"  1. Have may Manage permission for all buildings within your new settlement area.\"}");
        buf.append("text{type=\"italic\";text=\"  2. To check there are no large animals such as spiders in your local area (including underground).\"}");
        buf.append("text{type=\"italic\";text=\"  3. To check there are no lairs in your local area.\"}");
        if(!Servers.localServer.isFreeDeeds()) {
            buf.append("text{type=\"italic\";text=\"  4. Have sufficient funds in your in-game bank account.\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"How to use this form\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Each stage of the process is on its own page. This is so we can make some checks as we go along and also give you a running commentary as to cost.\"}");
        buf.append("text{type=\"italic\";text=\"At the bottom of the screen are additional notes and explanations if you need them.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"You can abort this process by closing the window or selecting cancel on the next screen.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Okay, lets get started!\"}");
        buf.append("text{text=\"\"}");
        buf.append("harray {button{text=\"Start the Settlement Application\";id=\"submit\"}}}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 500, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        if(!this.checkDeedItem()) {
            buf.append("text{text=\"There is a problem with the ").append(this.deed.getName()).append(" used.\"}");
        } else if(this.deed.isNewDeed() || this.deed.getTemplateId() == 862) {
            buf.append("text{text=\"\"}");
            if(this.error != null && this.error.length() > 0) {
                buf.append("text{color=\"255,0,0\";text=\"").append(this.error).append("\"}");
                buf.append("text{text=\"\"}");
            }

            buf.append("text{type=\"italic\";text=\"Note: This is the main settlement, NOT the perimeter.\"}");
            buf.append("text{text=\"\"}");
            String from = " you ";
            if(!this.expanding) {
                buf.append("text{text=\"Please stand on the tile where you want your new settlement token to be.\"}");
            } else {
                try {
                    Village old = Villages.getVillage(this.deed.getData2());
                    this.tokenx = old.getTokenX();
                    this.tokeny = old.getTokenY();
                    this.selectedWest = this.tokenx - old.getStartX();
                    this.selectedEast = old.getEndX() - this.tokenx;
                    this.selectedNorth = this.tokeny - old.getStartY();
                    this.selectedSouth = old.getEndY() - this.tokeny;
                    from = " the token ";
                } catch (NoSuchVillageException ignored) {}
            }

            buf.append("text{text=\"Please enter in the boxes below the distances in tiles between").append(from).append("and the border of your settlement.").append(" Example: 5, 5, 6, 6 will create a deed 11 tiles by 13 tiles.").append(" Minimum is 5.\"}");
            buf.append("text{text=\"\"}");
            buf.append("text{text=\"");
            if(!Servers.localServer.isFreeDeeds()) {
                buf.append("One tile costs ").append(Villages.TILE_COST_STRING).append(" initially. ");
            } else {
                buf.append("There is no initial cost for deeding here. ");
            }

            if(Servers.localServer.isUpkeep()) {
                buf.append("Every month the upkeep per tile is ").append(Villages.TILE_UPKEEP_STRING).append(".");
            } else {
                buf.append("There are no monthly upkeep charges here.");
            }

            buf.append("\"}");
            if(!this.hasCompass && !this.expanding) {
                if(this.dir == 0) {
                    buf.append("harray{label{text=\"Settlement size left:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeW\";text=\"").append(this.selectedWest).append("\"}");
                    buf.append("label{text=\" right:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeE\";text=\"").append(this.selectedEast).append("\"}");
                    buf.append("}");
                    buf.append("harray{label{text=\"Settlement size front:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeN\";text=\"").append(this.selectedNorth).append("\"}");
                    buf.append("label{text=\" back:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeS\";text=\"").append(this.selectedSouth).append("\"}");
                    buf.append("}");
                } else if(this.dir == 2) {
                    buf.append("harray{label{text=\"Settlement size left:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeN\";text=\"").append(this.selectedNorth).append("\"}");
                    buf.append("label{text=\" right:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeS\";text=\"").append(this.selectedSouth).append("\"}");
                    buf.append("}");
                    buf.append("harray{label{text=\"Settlement size front:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeE\";text=\"").append(this.selectedEast).append("\"}");
                    buf.append("label{text=\" back:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeW\";text=\"").append(this.selectedWest).append("\"}");
                    buf.append("}");
                } else if(this.dir == 4) {
                    buf.append("harray{label{text=\"Settlement size left:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeE\";text=\"").append(this.selectedEast).append("\"}");
                    buf.append("label{text=\" right:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeW\";text=\"").append(this.selectedWest).append("\"}");
                    buf.append("}");
                    buf.append("harray{label{text=\"Settlement size front:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeS\";text=\"").append(this.selectedSouth).append("\"}");
                    buf.append("label{text=\" back:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeN\";text=\"").append(this.selectedNorth).append("\"}");
                    buf.append("}");
                } else {
                    buf.append("harray{label{text=\"Settlement size left:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeS\";text=\"").append(this.selectedSouth).append("\"}");
                    buf.append("label{text=\" right:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeN\";text=\"").append(this.selectedNorth).append("\"}");
                    buf.append("}");
                    buf.append("harray{label{text=\"Settlement size front:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeW\";text=\"").append(this.selectedWest).append("\"}");
                    buf.append("label{text=\" back:\"}");
                    buf.append("input{maxchars=\"3\";id=\"sizeE\";text=\"").append(this.selectedEast).append("\"}");
                    buf.append("}");
                }
            } else {
                buf.append("harray{label{text=\"Settlement size west:\"}");
                buf.append("input{maxchars=\"3\";id=\"sizeW\";text=\"").append(this.selectedWest).append("\"}");
                buf.append("label{text=\" east:\"}");
                buf.append("input{maxchars=\"3\";id=\"sizeE\";text=\"").append(this.selectedEast).append("\"}");
                buf.append("}");
                buf.append("harray{label{text=\"Settlement size north:\"}");
                buf.append("input{maxchars=\"3\";id=\"sizeN\";text=\"").append(this.selectedNorth).append("\"}");
                buf.append("label{text=\" south:\"}");
                buf.append("input{maxchars=\"3\";id=\"sizeS\";text=\"").append(this.selectedSouth).append("\"}");
                buf.append("}");
            }
        }

        buf.append("text{text=\"\"}");
        buf.append("harray {button{text=\"Survey Area\";id=\"submit\"}label{text=\" \";id=\"spacedlxg\"};button{text=\"Show Intro\";id=\"back\"};label{text=\" \";id=\"sacedlxg\"};button{text=\"Cancel\";id=\"cancel\"};};");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Help\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"This form will take you through the process of founding or resizing your settlement. You will be asked how large you want your settlement to be, if you want to buy a larger perimeter, what you want to call your new settlement and how many guards you wish to hire. You may wish to read the help articles on the Wiki for more information before you start.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"What you need first:\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"  1. Have may Manage permission for all buildings within your new settlement area.\"}");
        buf.append("text{type=\"italic\";text=\"  2. To check there are no large animals such as spiders in your local area (including underground).\"}");
        buf.append("text{type=\"italic\";text=\"  3. To check there are no lairs in your local area.\"}");
        if(!Servers.localServer.isFreeDeeds()) {
            buf.append("text{type=\"italic\";text=\"  4. Sufficient funds in your in-game bank account.\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 600, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion2() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        this.addTileCost(buf);
        buf.append("text{type=\"italic\";text=\"You may now move around and inspect the selected area while continuing with this form. The border has been marked for your convenience.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Stage Two - The Deed Perimeter\"}");
        if(this.error != null && this.error.length() > 0) {
            buf.append("text{color=\"255,0,0\";type=\"bold\";text=\"").append(this.error).append("\"}");
            buf.append("text{text=\"\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Please enter the number of tiles BEYOND the 5").append(Servers.localServer.isFreeDeeds() ? "" : " initial tiles that you get for free").append(". You can simply leave the number at zero if you are happy with the ").append(5).append(Servers.localServer.isFreeDeeds() ? "" : " free").append(" tiles or extend the perimeter at a later date.\"}");
        buf.append("text{text=\"\"}");
        buf.append("harray{label{text=\"Perimeter Size: 5").append(Servers.localServer.isFreeDeeds() ? "" : " free").append(" tiles plus: \"}");
        buf.append("input{maxchars=\"3\";id=\"perimeter\";text=\"").append(this.initialPerimeter).append("\"};label{text=\" tiles radius\"}}");
        buf.append("text{text=\"\"}");
        buf.append("harray {button{text=\"Survey Area\";id=\"submit\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Back\";id=\"back\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Cancel\";id=\"cancel\"};};");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Help\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"The perimeter surrounding a settlement on all sides is purely to stop non-citizens from building or founding their own settlement - there are no other restrictions. You do not own and control the perimeter in the same way as the main settlement. On PvP servers kingdom guards will hunt enemies within the perimeter.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"The minimum perimeter size is 5");
        if(Servers.localServer.isFreeDeeds() && !Servers.localServer.isUpkeep()) {
            buf.append(", which you may increase.\"}");
        } else {
            buf.append(", which comes at no cost. You may pay to extend this if you wish. The current cost is ").append(Villages.PERIMETER_COST_STRING).append(" per tile");
            if(Servers.localServer.isUpkeep()) {
                buf.append(", and the upkeep is ").append(Villages.PERIMETER_UPKEEP_STRING).append(" per tile");
            }

            buf.append(".\"}");
            buf.append("text{type=\"italic\";text=\"There are no refunds for downsizing your deed. You will receive what is left in the upkeep fund if you later choose to disband.\"}");
        }

        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 600, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion3() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{type=\"italic\";text=\"Your deed size will be ").append(this.diameterX).append(" by ").append(this.diameterY).append(" and the perimeter will extend beyond the border by ").append(5 + this.initialPerimeter).append(" tiles (including the required ").append(5).append(Servers.localServer.isFreeDeeds() ? "" : " free").append(" tiles)\"}");
        buf.append("text{text=\"\"}");
        if(this.error != null && this.error.length() > 0) {
            buf.append("text{color=\"255,0,0\";type=\"bold\";text=\"").append(this.error).append("\"}");
            buf.append("text{text=\"\"}");
        }

        buf.append("text{type=\"bold\";text=\"Stage Three - Naming Your Deed\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Note! The name and motto may contain the following letters: \"}");
        buf.append("text{type=\"italic\";text=\"a-z,A-Z,\', and -\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Settlement name:\"}");
        if(this.expanding) {
            try {
                Village k = Villages.getVillage(this.deed.getData2());
                if(k.mayChangeName()) {
                    buf.append("input{maxchars=\"40\";id=\"vname\";text=\"").append(this.villageName).append("\"}");
                    buf.append("text{type=\"bold\";color=\"255,50,0\";text=\"NOTE: Changing name will").append(Servers.localServer.isFreeDeeds() ? "" : " cost " + new Change(NAME_CHANGE_COST).getChangeString() + ",").append(" remove all faith bonuses, and lock the name for 6 months.\"}");
                } else {
                    buf.append("text{text=\"").append(this.villageName).append("\"}");
                    buf.append("passthrough{id=\"vname\";text=\"").append(this.villageName).append("\"}");
                }

                this.permanent = k.isPermanent;
            } catch (NoSuchVillageException var3) {
                buf.append("text{type=\"bold\";text=\"Error: This settlement no longer exists and the operation will fail.\"}");
            }
        } else {
            buf.append("input{maxchars=\"40\";id=\"vname\";text=\"").append(this.villageName).append("\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Settlement motto:\"}");
        buf.append("input{maxchars=\"100\";id=\"motto\";text=\"").append(this.motto).append("\"}");
        buf.append("text{text=\"\"}");
        buf.append("checkbox{id=\"democracy\";selected=\"").append(this.democracy).append("\";text=\"Make this a democracy: \"}");
        Kingdom k1 = Kingdoms.getKingdom(this.spawnKingdom);
        if(k1 != null && !k1.isCustomKingdom() && this.getResponder().getPower() >= 3) {
            buf.append("checkbox{id=\"permanent\";selected=\"").append(this.permanent).append("\";text=\"Make this a permanent settlement: \"}");
            buf.append("harray{label{text=\"Select a kingdom if this is the start town: \"};dropdown{id=\"kingdomid\";default=\"").append(this.spawnKingdom).append("\";options=\"None,").append("Jenn-Kellon").append(",").append("Mol Rehan").append(",").append("Horde of the Summoned").append(",").append("Freedom Isles").append("\"}}");
        }

        buf.append("harray {button{text=\"Save this name\";id=\"submit\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Go Back\";id=\"back\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Cancel\";id=\"cancel\"};};");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Help\"}");
        buf.append("text{type=\"italic\";text=\"You can enter the name and motto for your deed as well as mark this as a democracy. The citizens of a settlement are allowed to vote for a new mayor up to once every week. The challenger requires 51% of the vote to succeed if it is a democracy, otherwise it is not possible to change mayor.  In a democracy you cannot revoke the citizenship of a citizen. By default, Wurm settlements are Autocracies.\"}");
        buf.append("text{text=\"\"}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 600, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion4() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        if(this.error != null && this.error.length() > 0) {
            buf.append("text{color=\"255,0,0\";text=\"").append(this.error).append("\"}");
            buf.append("text{text=\"\"}");
        }

        if(this.changingName) {
            buf.append("text{color=\"255,0,0\";text=\"You are changing name and the settlement will lose its faith bonuses.\"}");
            buf.append("text{text=\"\"}");
        }

        buf.append("text{type=\"bold\";text=\"Settlement Size\"}");
        this.addTileCost(buf);
        this.addPerimeterCost(buf);
        this.addChangeNameCost(buf);
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"You have nearly finished the deed process.\"}");
        buf.append("text{type=\"italic\";text=\"Please note that if the mayor changes kingdom for some reason the settlement will start disbanding.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Do you wish to hire guards?\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"For ").append(this.villageName).append(" you may hire up to ").append(this.maxGuards).append(" guards.\"}");
        buf.append("text{text=\"\"}");
        if(Servers.localServer.isChallengeOrEpicServer() && !Servers.localServer.isFreeDeeds()) {
            buf.append("text{text=\"The only guard type is heavy guards. The running upkeep cost increases the more guards you have in a sort of ladder system. The first guards are cheaper than the last.\"};");
            buf.append("text{text=\"You can test various numbers and review the cost for upkeep on the next screen.\"};");
        } else if(Servers.localServer.isFreeDeeds()) {
            buf.append("text{text=\"The only guard type is heavy guards. There is no cost for hiring guards here").append(Servers.localServer.isUpkeep() ? ", but they have an upkeep of " + Villages.GUARD_UPKEEP_STRING + " per month" : ".").append("\"};");
            buf.append("text{text=\"\"};");
        } else {
            buf.append("text{text=\"The only guard type is heavy guards. The cost for hiring them is ").append(Villages.GUARD_COST_STRING).append(" and running upkeep is ").append(Villages.GUARD_UPKEEP_STRING).append(" per month.\"};");
            buf.append("text{text=\"\"};");
        }

        if(Servers.localServer.PVPSERVER) {
            buf.append("label{text=\"Note that you will need at least 1 guard to enforce the role rules on deed!\"}");
            buf.append("text{text=\"\"}");
        }

        buf.append("harray{label{text=\"How many guards do you wish to hire?\"}");
        buf.append("input{maxchars=\"3\";text=\"").append(this.selectedGuards).append("\";id=\"guards\"}}");
        buf.append("text{text=\"\"}");
        buf.append("harray {button{text=\"Save the number of guards\";id=\"submit\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Go Back\";id=\"back\"};label{text=\" \";id=\"spacedlxg\"};button{text=\"Cancel\";id=\"cancel\"};};");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Help\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Guards enforce the rules on deed and protect you against enemies. Guards can be hired at any time, but it is good to start your settlement with at least a few! ");
        if(!Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep()) {
            buf.append("There are two costs to guards, their initial hiring fee (covers their travel expenses) and their monthly salary which is added to the deed upkeep cost. There is a maximum number of guards you can have depending on the size of your deed though you can hire none at all if you wish!\"}");
        } else if(Servers.localServer.isUpkeep()) {
            buf.append("There is no cost to hire guards, but you must pay a monthly salary which is added to the deed upkeep cost. There is a maximum number of guards you can have depending on the size of your deed though you can hire none at all if you wish!\"}");
        } else {
            buf.append("There is no cost to hire guards and no monthly salary to pay. There is a maximum number of guards you can have depending on the size of your deed though you can hire none at all if you wish!\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 600, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion5() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        if(this.error != null && this.error.length() > 0) {
            buf.append("text{color=\"255,0,0\";text=\"").append(this.error).append("\"}");
            buf.append("text{text=\"\"}");
        }

        if(this.changingName) {
            buf.append("text{color=\"255,0,0\";text=\"You are changing name and the settlement will lose its faith bonuses.\"}");
            buf.append("text{text=\"\"}");
        }

        String fs = "Found";
        if(this.expanding) {
            fs = "Resize";
        }

        buf.append("text{type=\"italic\";text=\"Here are all your settings and the costs. If you are happy, click on ").append(fs).append(" Settlement. If you wish to change anything, use the Go Back button.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Settlement Size\"}");
        this.addTileCost(buf);
        this.addPerimeterCost(buf);
        this.addChangeNameCost(buf);
        buf.append("text{type=\"bold\";text=\"Guards\"}");
        this.addGuardCost(buf);
        buf.append("text{type=\"bold\";text=\"Citizens\"}");
        this.addCitizenMultiplier(buf);
        buf.append("text{text=\"\"}");
        if(!Servers.localServer.isFreeDeeds() || Servers.localServer.isUpkeep()) {
            buf.append("text{type=\"bold\";text=\"Total\"}");
        }

        this.addTotalUpkeep(buf);
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Name and Type\"}");
        if(!this.expanding) {
            buf.append("text{text=\"Your settlement name is: ").append(this.villageName).append("\"}");
        }

        buf.append("text{text=\"Your settlement motto is: \'").append(this.motto).append("\'\"}");
        buf.append("text{text=\"\"}");
        if(this.democracy) {
            buf.append("label{text=\"The settlement is a democracy.\"}");
        } else {
            buf.append("label{text=\"The settlement is an autocracy.\"}");
        }

        buf.append("text{text=\"\"}");
        if(this.getResponder().getPower() >= 3 && this.permanent) {
            buf.append("text{type=\"bold\";text=\"Start City\"}");
            buf.append("label{text=\"The settlement will be a start city.\"}");
            buf.append("label{text=\"The start kingdom will be ").append(Kingdoms.getNameFor(this.spawnKingdom)).append(".\"}");
            buf.append("text{text=\"\"}");
        }

        boolean hasError = false;
        Village village = null;
        if (this.expanding) {
            try {
                village = Villages.getVillage(this.deed.getData2());
            } catch (NoSuchVillageException var14) {
                hasError = true;
                buf.append("text{type=\"bold\";color=\"255,0,0\";text=\"Error: This settlement no longer exists and the operation will fail.\"}");
            }
        }

        if (!hasError && Feature.HIGHWAYS.isEnabled()) {
            buf.append("text{type=\"bold\";text=\"" + this.villageName + " KOS.v.Highways\"}");
            if (this.expanding) {
                if (!village.isKosAllowed() && village.getReputations().length <= 0) {
                    if (this.willHaveHighay()) {
                        if (!village.isHighwayAllowed()) {
                            hasError = true;
                            buf.append("text{type=\"bold\";color=\"255,0,0\";text=\"Error: The new size covers a highway but highways have not been allowed, see settlment settings to change this option.\"}");
                        } else if (village.hasHighway()) {
                            buf.append("text{text=\"The new size will still cover a highway which is already allowed by settlement settings.\"}");
                        } else {
                            buf.append("text{text=\"The new size will now cover a highway which is already allowed by settlement settings.\"}");
                        }
                    } else if (village.isHighwayAllowed()) {
                        buf.append("text{text=\"Note: You will not be able to use KOS as highways have been enabled for this settlment, see settlment settings to change this option.\"}");
                    } else {
                        buf.append("label{text=\"Note: To allow KOS or Highways, use the settlement settings.\"}");
                    }
                } else if (this.willHaveHighay()) {
                    hasError = true;
                    buf.append("text{type=\"bold\";color=\"255,0,0\";text=\"Error: Cannot expand over a highway if KOS is enabled, see settlment settings to change this option.\"}");
                } else {
                    buf.append("text{text=\"Note: You will not be able to have highway through this settlement as KOS is active.\"}");
                }
            } else if (this.willHaveHighay()) {
                buf.append("text{text=\"The settlement size covers a highway, and will auto-set that flag in settlement settings.\"}");
            } else {
                buf.append("label{text=\"Note: To allow KOS or Highways, use the settlement settings after founding.\"}");
            }
        }

        if (!hasError) {
            buf.append("text{type=\"bold\";text=\"" + fs + " " + this.villageName + "\"}");
            buf.append("text{type=\"italic\";text=\"By clicking on the " + fs + " Settlement button you agree to the following terms:\"}");
        buf.append("text{type=\"italic\";text=\"This is an irreversible and non refundable operation.\"}");
        buf.append("text{text=\"\"}");
        long fullcost;
            Change needed;
            long toCharge;
            Change cc;
        if(this.expanding) {
            fullcost = this.getResizeCostDiff();
            if(fullcost > 0L) {
                    needed = new Change(fullcost);
                    toCharge = village.getAvailablePlanMoney();
                    cc = new Change(toCharge);
                    buf.append("text{type=\"italic\";text=\"This change will cost " + needed.getChangeString() + ".\"}");
                    buf.append("text{type=\"italic\";text=\"Up to " + cc.getChangeString() + " can be taken from the settlement upkeep funds.\"}");
                    long rest = getExpandMoneyNeededFromBank(fullcost, village);
                    if (rest > 0L) {
                        Change restc = new Change(rest);
                        buf.append("text{type=\"italic\";text=\"" + restc.getChangeString() + " will be taken from your bank account.\"}");
                }
            } else {
                buf.append("text{type=\"italic\";text=\"This change is free of charge.\"}");
            }
        } else if(!Servers.localServer.isFreeDeeds()) {
            fullcost = this.getFoundingCost();
                needed = new Change(fullcost);
                buf.append("text{type=\"italic\";text=\"The full cost for founding this deed will be " + needed.getChangeString() + ".\"}");
            if(this.deed.getTemplateId() != 862) {
                buf.append("text{type=\"italic\";text=\"The cost of the deed form will cover up to 7 silver coins of these. The rest will go into upkeep.\"}");
            }

                toCharge = this.getFoundingCharge();
                if (toCharge > 0L) {
                    cc = new Change(toCharge);
                    buf.append("text{type=\"italic\";text=\"" + cc.getChangeString() + " will be taken from your bank account.\"}");
            } else if(this.deed.getTemplateId() != 862) {
                    long left = 100000L - fullcost;
                    Change leftc = new Change(left);
                    buf.append("text{type=\"italic\";text=\"The settlement will be founded and " + leftc.getChangeString() + " will be put into upkeep.\"}");
                }
            }
        }

        buf.append("harray{" + (!hasError ? "button{text=\"" + fs + " Settlement\";id=\"submit\"};label{text=\" \"};" : "") + "button{text=\"Go Back\";id=\"back\"};label{text=\" \"};button{text=\"Cancel\";id=\"cancel\"};}}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 700, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion6() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"You have founded and become the mayor and first citizen of ").append(this.villageName).append(".\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"The deed for this settlement has been placed in your inventory, and if you have hired any guards, they will no doubt be here as soon as they can be.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"You can change the size of your deed and its perimeter if you wish, as well as hire and fire guards, using the tools attached to the deed or token.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Remember to keep money for upkeep as otherwise your settlement will disband and you will lose everything.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"italic\";text=\"Good luck, and may the Gods look over you with kindness! \"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Lords of Wurm\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"Notes\"}");
        buf.append("text{type=\"italic\";text=\"The maximum number of citizens, including guards, is ").append(this.tiles / 11).append("\"}");
        if(!Servers.isThisAPvpServer()) {
            buf.append("text{type=\"italic\";text=\"The maximum number of branded animals is ").append(this.tiles / 11).append("\"}");
        }

        buf.append("text{text=\"\"}");
        buf.append("harray {button{text=\"Finish\";id=\"submit\"}}}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 430, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    private void copyValues(VillageFoundationQuestion vfq) {
        vfq.dir = this.dir;
        vfq.error = this.error;
        vfq.initialPerimeter = this.initialPerimeter;
        vfq.sequence = this.sequence;
        vfq.selectedWest = this.selectedWest;
        vfq.selectedEast = this.selectedEast;
        vfq.selectedNorth = this.selectedNorth;
        vfq.selectedSouth = this.selectedSouth;
        vfq.diameterX = this.selectedWest + this.selectedEast + 1;
        vfq.diameterY = this.selectedNorth + this.selectedSouth + 1;
        vfq.maxGuards = GuardPlan.getMaxGuards(this.diameterX, this.diameterY);
        vfq.changingName = this.changingName;
        vfq.selectedGuards = this.selectedGuards;
        vfq.tiles = this.diameterX * this.diameterY;
        vfq.perimeterDiameterX = this.diameterX + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        vfq.perimeterDiameterY = this.diameterY + 5 + 5 + this.initialPerimeter + this.initialPerimeter;
        vfq.perimeterTiles = this.perimeterDiameterX * this.perimeterDiameterY - (this.diameterX + 5 + 5) * (this.diameterY + 5 + 5);
        vfq.motto = this.motto;
        vfq.villageName = this.villageName;
        vfq.permanent = this.permanent;
        vfq.democracy = this.democracy;
        vfq.spawnKingdom = this.spawnKingdom;
        vfq.deed = this.deed;
        vfq.tokenx = this.tokenx;
        vfq.tokeny = this.tokeny;
        vfq.surfaced = this.surfaced;
        vfq.expanding = this.expanding;
    }

    private boolean willHaveHighay() {
        int startx = this.tokenx - this.selectedWest;
        int starty = this.tokeny - this.selectedNorth;
        int endx = this.tokenx + this.selectedEast;
        int endy = this.tokeny + this.selectedSouth;
        Item[] var5 = Items.getMarkers();
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Item marker = var5[var7];
            int x = marker.getTileX();
            int y = marker.getTileY();
            if (x >= startx - 2 && x <= endx + 2 && y >= starty - 2 && y <= endy + 2) {
                return true;
            }
        }

        return false;
    }

    public void setSequence(int newseq) {
        this.sequence = newseq;
    }
}
