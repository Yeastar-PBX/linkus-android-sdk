package com.yeastar.linkus.demo.eventbus;

/**
 * Created by root on 17-5-19.
 */

public class CallLogChangeEvent {

    private int result;

    public CallLogChangeEvent(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}
