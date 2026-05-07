package com.p1nero.tcrcore.save_data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

public class TCRMainLevelSaveData extends SavedData {
    private Difficulty difficulty = Difficulty.NORMAL;//独立的difficulty，和游戏规则的不同
    private BlockPos villagePos = BlockPos.ZERO;
    private boolean npcPlaced;
    public static final String NAME = "tcr_level_save_data";
    private Level level;

    public void setLevel(Level level) {
        this.level = level;
    }

    private static TCRMainLevelSaveData create() {
        return new TCRMainLevelSaveData();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isHardDifficulty() {
        return difficulty.equals(Difficulty.HARD);
    }

    public boolean isEasyDifficulty() {
        return difficulty.equals(Difficulty.EASY);
    }

    public boolean isNormalDifficulty() {
        return difficulty.equals(Difficulty.NORMAL);
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        setDirty();
    }

    public void setVillagePos(BlockPos villagePos) {
        this.villagePos = villagePos;
        setDirty();
    }

    public BlockPos getVillagePos() {
        return villagePos;
    }

    public boolean isNPCPlaced() {
        return npcPlaced;
    }

    public void setNPCPlaced(boolean girlPlaced) {
        this.npcPlaced = girlPlaced;
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        pCompoundTag.putInt("villagePosX", villagePos.getX());
        pCompoundTag.putInt("villagePosY", villagePos.getY());
        pCompoundTag.putInt("villagePosZ", villagePos.getZ());
        pCompoundTag.putBoolean("girlPlaced", npcPlaced);
        pCompoundTag.putInt("difficulty", difficulty.getId());
        return pCompoundTag;
    }

    public void load(CompoundTag nbt) {
        this.villagePos = new BlockPos(
                nbt.getInt("villagePosX"),
                nbt.getInt("villagePosY"),
                nbt.getInt("villagePosZ")
        );
        this.npcPlaced = nbt.getBoolean("girlPlaced");
        difficulty = Difficulty.byId(nbt.getInt("difficulty"));
    }

    public static TCRMainLevelSaveData decode(CompoundTag tag){
        TCRMainLevelSaveData saveData = TCRMainLevelSaveData.create();
        saveData.load(tag);
        return saveData;
    }

    public static TCRMainLevelSaveData get(ServerLevel worldIn) {
        ServerLevel world = worldIn.dimension() == Level.OVERWORLD ? worldIn : worldIn.getServer().getLevel(Level.OVERWORLD);
        if(world == null) {
            throw new IllegalStateException("主世界丢失？！请重新创建世界！Please recreate a new world!");
        }
        DimensionDataStorage dataStorage = world.getDataStorage();
        TCRMainLevelSaveData levelSaveData = dataStorage.computeIfAbsent(TCRMainLevelSaveData::decode, TCRMainLevelSaveData::create, TCRMainLevelSaveData.NAME);
        levelSaveData.setLevel(world);
        return levelSaveData;
    }


    public static TCRMainLevelSaveData get(MinecraftServer server) {
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        if(world == null) {
            throw new IllegalStateException("主世界丢失？！请重新创建世界！Please recreate a new world!");
        }
        DimensionDataStorage dataStorage = world.getDataStorage();
        TCRMainLevelSaveData levelSaveData = dataStorage.computeIfAbsent(TCRMainLevelSaveData::decode, TCRMainLevelSaveData::create, TCRMainLevelSaveData.NAME);
        levelSaveData.setLevel(world);
        return levelSaveData;
    }
}