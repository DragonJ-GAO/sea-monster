package me.add1.network;

public interface StoreStatusCallback extends StatusCallback<StoreStatusCallback.StoreStatus>{

    public class StoreStatus {
        private long totalSize;
        private long receivedSize;

        public StoreStatus(long total) {
            this.totalSize = total;
            this.receivedSize = 0;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long size) {
            this.totalSize = size;
        }

        public long getReceivedSize() {
            return receivedSize;
        }

        public long getPercent() {
            return (getReceivedSize() * 100 / getTotalSize());
        }

        public void setReceivedSize(long size) {
            this.receivedSize = size;
        }

        public void addReceivedSize(long size) {
            this.receivedSize += size;
        }

        @Override
        public String toString() {
            return String.format("StoreStatus-->totalSize:%1$d  receivedSize:%2$d", this.totalSize, this.receivedSize);
        }
    }
}
