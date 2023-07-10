package com.yeastar.linkus.demo.eventbus;


import com.yeastar.linkus.service.conference.vo.ConferenceVo;

/**
 * Created by root on 17-6-16.
 */

public class ConferenceExceptionEvent {

    private ConferenceVo conferenceVo;

    public ConferenceExceptionEvent(ConferenceVo conferenceVo) {
        this.conferenceVo = conferenceVo;
    }

    public ConferenceVo getConferenceVo() {
        return conferenceVo;
    }
}
