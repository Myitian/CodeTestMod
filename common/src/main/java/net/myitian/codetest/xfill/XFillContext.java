package net.myitian.codetest.xfill;

import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class XFillContext {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int MIN_XZ_POS = -Level.MAX_LEVEL_SIZE;
    private static final int MAX_XZ_POS = Level.MAX_LEVEL_SIZE;
    private static final int MAX_XZ_SIZE = MAX_XZ_POS - MIN_XZ_POS + 1;
    private static final int MIN_Y_POS = DimensionType.MIN_Y;
    private static final int MAX_Y_POS = DimensionType.MAX_Y;
    private static final int MAX_Y_SIZE = MAX_Y_POS - MIN_Y_POS + 1;
    public final int startX;
    public final int startY;
    public final int startZ;
    public final int endX;
    public final int endY;
    public final int endZ;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    private final byte[] blockStates;
    private final BlockInput[] usedBlockInfo;

    public XFillContext(BlockPos pos0, BlockPos pos1) {
        if (!isValidSelection(pos0, pos1)) {
            throw new IllegalArgumentException("Invalid selection: " + pos0 + " to " + pos1);
        }
        startX = Math.min(pos0.getX(), pos1.getX());
        startY = Math.min(pos0.getY(), pos1.getY());
        startZ = Math.min(pos0.getZ(), pos1.getZ());
        endX = Math.max(pos0.getX(), pos1.getX());
        endY = Math.max(pos0.getY(), pos1.getY());
        endZ = Math.max(pos0.getZ(), pos1.getZ());
        sizeX = endX - startX + 1;
        sizeY = endY - startY + 1;
        sizeZ = endZ - startZ + 1;
        blockStates = new byte[sizeX * sizeY * sizeZ];
        usedBlockInfo = new BlockInput[256];
    }

    public static boolean isPosValid(int x, int y, int z) {
        return y >= MIN_Y_POS && y <= MAX_Y_POS
            && x >= MIN_XZ_POS && x <= MAX_XZ_POS
            && z >= MIN_XZ_POS && z <= MAX_XZ_POS;
    }

    public static boolean isSizeValid(int x, int y, int z) {
        if (y < 0 || y > MAX_Y_SIZE
            || x < 0 || x > MAX_XZ_SIZE
            || z < 0 || z > MAX_XZ_SIZE) {
            return false;
        }
        long totalSize = (long) y * x * z;
        return totalSize <= MAX_ARRAY_SIZE && totalSize >= 0;
    }

    public static boolean isValidSelection(BlockPos pos0, BlockPos pos1) {
        int startX = Math.min(pos0.getX(), pos1.getX());
        int startY = Math.min(pos0.getY(), pos1.getY());
        int startZ = Math.min(pos0.getZ(), pos1.getZ());
        if (isPosValid(startX, startY, startZ)) {
            int endX = Math.max(pos0.getX(), pos1.getX());
            int endY = Math.max(pos0.getY(), pos1.getY());
            int endZ = Math.max(pos0.getZ(), pos1.getZ());
            if (isPosValid(endX, endY, endZ)) {
                int sizeX = endX - startX + 1;
                int sizeY = endY - startY + 1;
                int sizeZ = endZ - startZ + 1;
                return isSizeValid(sizeX, sizeY, sizeZ);
            }
        }
        return false;
    }

    public byte getBlockInfoByOffset(int x, int y, int z) {
        if (validateOffset(x, y, z)) {
            return getBlockInfoByOffsetUnchecked(x, y, z);
        } else {
            throw new IndexOutOfBoundsException("Position: (" + x + ", " + y + ", " + z + ")");
        }
    }

    public byte getBlockInfoByOffsetUnchecked(int offsetX, int offsetY, int offsetZ) {
        int index = (offsetY * sizeZ + offsetZ) * sizeX + offsetX;
        return blockStates[index];
    }

    public byte getBlockInfoByPos(int x, int y, int z) {
        if (validatePos(x, y, z)) {
            return getBlockInfoByOffsetUnchecked(x - startX, y - startY, z - startZ);
        } else {
            throw new IndexOutOfBoundsException("Position: (" + x + ", " + y + ", " + z + ")");
        }
    }

    public @Nullable BlockInput getUsedBlockInfo(int index) {
        if (index < 0 || index >= usedBlockInfo.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + usedBlockInfo.length);
        }
        return usedBlockInfo[index];
    }

    public void setBlockInfoByOffset(int x, int y, int z, byte value) {
        if (validateOffset(x, y, z)) {
            setBlockInfoByOffsetUnchecked(x, y, z, value);
        } else {
            throw new IndexOutOfBoundsException("Position: (" + x + ", " + y + ", " + z + ")");
        }
    }

    public void setBlockInfoByOffsetUnchecked(int offsetX, int offsetY, int offsetZ, byte value) {
        int index = (offsetY * sizeZ + offsetZ) * sizeX + offsetX;
        blockStates[index] = value;
    }

    public void setBlockInfoByPos(int x, int y, int z, byte value) {
        if (validatePos(x, y, z)) {
            setBlockInfoByOffsetUnchecked(x - startX, y - startY, z - startZ, value);
        } else {
            throw new IndexOutOfBoundsException("Position: (" + x + ", " + y + ", " + z + ")");
        }
    }

    public void setUsedBlockInfo(int index, BlockInput blockInfo) {
        if (index < 0 || index >= usedBlockInfo.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + usedBlockInfo.length);
        }
        if (index == 0) {
            throw new IllegalArgumentException("Index 0 is reserved for undefined block info.");
        }
        usedBlockInfo[index] = blockInfo;
    }

    public int size() {
        return blockStates.length;
    }

    public boolean validateOffset(int x, int y, int z) {
        return y >= 0 && y <= sizeY
            && x >= 0 && x <= sizeX
            && z >= 0 && z <= sizeZ;
    }

    public boolean validatePos(int x, int y, int z) {
        return y >= startY && y <= endY
            && x >= startX && x <= endX
            && z >= startZ && z <= endZ;
    }
}