package org.bxteam.divinemc.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;

public class ConcurrentFlagMatrix extends WoodlandMansionPieces.SimpleGrid {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public ConcurrentFlagMatrix(int rows, int columns, int fallbackValue) {
        super(rows, columns, fallbackValue);
    }

    public void set(int row, int column, int value) {
        this.readWriteLock.writeLock().lock();

        try {
            super.set(row, column, value);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    public void set(int startRow, int startColumn, int endRow, int endColumn, int value) {
        this.readWriteLock.writeLock().lock();

        try {
            super.set(startRow, startColumn, endRow, endColumn, value);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    public int get(int row, int column) {
        this.readWriteLock.readLock().lock();

        int result;
        try {
            result = super.get(row, column);
        } finally {
            this.readWriteLock.readLock().unlock();
        }

        return result;
    }

    public void setIf(int row, int column, int expectedValue, int newValue) {
        if (this.get(row, column) == expectedValue) {
            this.set(row, column, newValue);
        }
    }

    public boolean edgesTo(int row, int column, int value) {
        this.readWriteLock.readLock().lock();

        boolean result;
        try {
            result = super.edgesTo(row, column, value);
        } finally {
            this.readWriteLock.readLock().unlock();
        }

        return result;
    }
}
