package com.yeastar.linkus.demo.eventbus;

/**
 * Created by ted on 17-5-17.
 * 会议室通知事件类
 */

public class ConferenceStatusEvent {
    private String conferenceId;
    private String extension ;//多个分机的时候用“-”分隔
    private int confstatus;//confstatus: 0.响铃 1.进入会议室 2.离开会议室 3.静音 4.取消静音 5.异常掉线通知 6.未接来电通知

    public ConferenceStatusEvent(String conferenceId, String extension, int confstatus) {
        this.conferenceId = conferenceId;
        this.extension = extension;
        this.confstatus = confstatus;
    }

    public String getExtension() {
        return extension;
    }

    public int getConfstatus() {
        return confstatus;
    }

    public String getConferenceId() {
        return conferenceId;
    }
}
