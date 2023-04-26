package com.yeastar.linkus.demo.eventbus;

/**
 * Created by ted on 17-8-2.
 */

public class RecordEvent {
    private boolean isRecord;

    public RecordEvent(boolean isRecord) {
        this.isRecord = isRecord;
    }

    public boolean isRecord() {
        return isRecord;
    }
}
