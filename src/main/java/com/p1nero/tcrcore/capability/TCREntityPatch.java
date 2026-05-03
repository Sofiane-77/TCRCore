package com.p1nero.tcrcore.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class TCREntityPatch {

    private boolean fighting;

    private BlockPos spawnPos = BlockPos.ZERO;

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public boolean hasSpawnPos() {
        return !this.spawnPos.equals(BlockPos.ZERO);
    }

    public boolean isEmpty() {
        return this.equals(TCREntityCapabilityProvider.EMPTY);
    }

    public void setFighting(boolean fighting) {
        this.fighting = fighting;
    }

    public boolean isFighting() {
        return fighting;
    }

    public void saveNBTData(CompoundTag tag) {
        tag.putBoolean("fighting", fighting);
        tag.put("SpawnPos", NbtUtils.writeBlockPos(this.spawnPos));
    }

    public void loadNBTData(CompoundTag tag) {
        fighting = tag.getBoolean("fighting");
        spawnPos = NbtUtils.readBlockPos(tag.getCompound("SpawnPos"));
    }
}
