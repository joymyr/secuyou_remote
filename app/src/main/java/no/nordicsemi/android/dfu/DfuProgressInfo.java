package no.nordicsemi.android.dfu;

import android.os.SystemClock;

import androidx.appcompat.widget.ActivityChooserView;
/* loaded from: classes.dex */
class DfuProgressInfo {
    private int bytesReceived;
    private int bytesSent;
    private int currentPart;
    private int imageSizeInBytes;
    private int initialBytesSent;
    private int lastBytesSent;
    private long lastProgressTime;
    private final ProgressListener mListener;
    private int maxObjectSizeInBytes;
    private int progress;
    private long timeStart;
    private int totalParts;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface ProgressListener {
        void updateProgressNotification();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DfuProgressInfo(ProgressListener progressListener) {
        this.mListener = progressListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DfuProgressInfo init(int i, int i2, int i3) {
        this.imageSizeInBytes = i;
        this.maxObjectSizeInBytes = Integer.MAX_VALUE;
        this.currentPart = i2;
        this.totalParts = i3;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DfuProgressInfo setTotalPart(int i) {
        this.totalParts = i;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setProgress(int i) {
        this.progress = i;
        this.mListener.updateProgressNotification();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBytesSent(int i) {
        if (this.timeStart == 0) {
            this.timeStart = SystemClock.elapsedRealtime();
            this.initialBytesSent = i;
        }
        this.bytesSent = i;
        this.progress = (int) ((i * 100.0f) / this.imageSizeInBytes);
        this.mListener.updateProgressNotification();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addBytesSent(int i) {
        setBytesSent(this.bytesSent + i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBytesReceived(int i) {
        this.bytesReceived = i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setMaxObjectSizeInBytes(int i) {
        this.maxObjectSizeInBytes = i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isComplete() {
        return this.bytesSent == this.imageSizeInBytes;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isObjectComplete() {
        return this.bytesSent % this.maxObjectSizeInBytes == 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getAvailableObjectSizeIsBytes() {
        int i = this.imageSizeInBytes;
        int i2 = this.bytesSent;
        int i3 = this.maxObjectSizeInBytes;
        return Math.min(i - i2, i3 - (i2 % i3));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getProgress() {
        return this.progress;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getBytesSent() {
        return this.bytesSent;
    }

    int getBytesReceived() {
        return this.bytesReceived;
    }

    int getImageSizeInBytes() {
        return this.imageSizeInBytes;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getSpeed() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        float f = elapsedRealtime - this.timeStart != 0 ? (this.bytesSent - this.lastBytesSent) / ((float) (elapsedRealtime - this.lastProgressTime)) : 0.0f;
        this.lastProgressTime = elapsedRealtime;
        this.lastBytesSent = this.bytesSent;
        return f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getAverageSpeed() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = this.timeStart;
        if (elapsedRealtime - j != 0) {
            return (this.bytesSent - this.initialBytesSent) / ((float) (elapsedRealtime - j));
        }
        return 0.0f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCurrentPart() {
        return this.currentPart;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getTotalParts() {
        return this.totalParts;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isLastPart() {
        return this.currentPart == this.totalParts;
    }
}
