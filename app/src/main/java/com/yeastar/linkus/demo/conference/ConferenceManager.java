package com.yeastar.linkus.demo.conference;

import android.content.Context;

import com.yeastar.linkus.demo.eventbus.ConferenceExceptionEvent;
import com.yeastar.linkus.demo.eventbus.ConferenceStatusEvent;
import com.yeastar.linkus.service.callback.ConferenceCallback;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;

import org.greenrobot.eventbus.EventBus;

public class ConferenceManager {
    private volatile static ConferenceManager instance;

    public static ConferenceManager getInstance() {
        if (instance == null) {
            synchronized (ConferenceManager.class) {
                if (instance == null) {
                    instance = new ConferenceManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        YlsConferenceManager.getInstance().setConferenceCallback(context, new ConferenceCallback() {
            @Override
            public void onConferenceException(ConferenceVo conferenceVo) {
                EventBus.getDefault().postSticky(new ConferenceExceptionEvent(conferenceVo));
            }

            @Override
            public void onConferenceStatusChange(String conferenceId, String number, int status) {
                EventBus.getDefault().post(new ConferenceStatusEvent(conferenceId, number, status));
            }
        });
    }

}
