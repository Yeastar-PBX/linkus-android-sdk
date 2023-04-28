package com.yeastar.linkus.demo.call;

/**
 * Created by ted on 17-7-25.
 */

public interface CallingContract {
    /**
     * Created by ted on 17-5-27.
     * 系统来电交互接口
     */

    interface PhoneStateCallback {

        void onRing();

        void onCalling();

        void onHangup();

        void onAnswerBusy();

    }

}
