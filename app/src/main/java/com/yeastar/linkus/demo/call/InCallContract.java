package com.yeastar.linkus.demo.call;



import com.yeastar.linkus.service.call.vo.InCallVo;

import java.util.LinkedList;

/**
 * Created by ted on 17-4-18.
 */

public interface InCallContract {

    abstract class Presenter extends BasePresenter<View> {

        abstract void callOut();

        abstract void transfer(int transferType);

        abstract void mute();

        abstract void record();

        abstract void hold();

        abstract void dialPad();

        abstract void speaker();

        abstract void hangupPhone();

        abstract void confirmTransfer();

        abstract void cancelTransfer();

        abstract void switchCallWaiting();

        abstract void refresh();


    }

    interface View extends BaseView<Presenter> {

        //更新通话界面底部六个按钮
        void updateInCallDialPad(InCallVo inCallModel);

        //来电响铃
        void ring(InCallVo inCallModel);

        //呼出语音通话
        void calling(InCallVo inCallModel);

        //单通通话
        void singleCall(InCallVo inCallModel);

        //call waiting(两通都已接通)当前通话是语音通话
        void callWaitingConnected(LinkedList<InCallVo> list);

        //call waiting响铃界面
        void callWaitingRing(LinkedList<InCallVo> list);

        void transferCall(LinkedList<InCallVo> list);

        //call waiting+转移
        void threeCall(LinkedList<InCallVo> list);

    }
}
