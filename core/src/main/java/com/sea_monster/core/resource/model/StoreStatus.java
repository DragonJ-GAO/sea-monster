package com.sea_monster.core.resource.model;

public class StoreStatus {
    private int totalSize;
    private int receivedSize;

    public StoreStatus(int total) {
        this.totalSize = total;
        this.receivedSize = 0;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int size) {
        this.totalSize = size;
    }

    public int getReceivedSize() {
        return receivedSize;
    }

    public int getPercent() {
        return (int)(getReceivedSize()*100/getTotalSize());
    }

    public void setReceivedSize(int size) {
        this.receivedSize = size;
    }

    public void addReceivedSize(int size) {
        this.receivedSize += size;
    }

    @Override
    public String toString() {
        return String.format("StoreStatus-->totalSize:%1$d  receivedSize:%2$d", this.totalSize, this.receivedSize);
    }
}
