package com.yeastar.linkus.demo.call.ring;

import android.content.Context;

/**
 * Created by ted on 17-4-27.
 */

public interface RingContract {

    interface Presenter {

        void answer(int callId, String conferenceId, String callerNumber);

        void reject(Context context, int callId, String callerNumber);

    }
}
