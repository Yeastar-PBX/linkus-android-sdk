package com.yeastar.linkus.demo;

public class Constant {
    //音频模式,分别为:听筒,扩音器,蓝牙耳机,有线耳机,初始音频设置
    public static final int AUDIO_EARPIECE = 0, AUDIO_SPEAKER = 1, AUDIO_BLUETOOTH = 2, AUDIO_WIRED_HEADSET = 3, AUDIO_USB_HEADSET = 4, AUDIO_DEFAULT = -1;

    //EventBus事件(string类型)
    public final static String EVENT_SYSTEM_RING = "系统来电";
    public final static String EVENT_ANSWER_CALL = "ANSWER_CALL";
    public final static String EVENT_REJECT_CALL = "REJECT_CALL";
    public final static String EVENT_ON_HOLD = "ON_HOLD";
    public final static String EVENT_ON_UN_HOLD = "ON_UN_HOLD";
    public final static String EVENT_ON_DISCONNECT_OR_ABORT = "ON_DISCONNECT_OR_ON_ABORT";
    public final static String EVENT_INCOMING_FAILED = "INCOMING_FAILED";
    public final static String EVENT_OUTGOING_FAILED = "OUTGOING_FAILED";
    public static final String EXTRA_DATA = "data";
    public static final String TAG_FRAGMENT_CALL = "tag_fragment_call";
    public static final String EXTRA_ON_NEW_INTENT = "onNewIntent";
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_FROM = "from";

    /*------------------------------------推送相关------------------------------------------*/
    public static final String TITLE = "title";
    public static final String NAME = "Name";//来电名称
    public static final String TEXT = "text";
    public static final String NEW_VOICE_MAIL = "New Voicemail";
    public static final String START_TIMESTAMP = "starttimestamp";
    public static final String RING_TIMEOUT = "ringtimeout";
    public static final String CALL_TYPE = "calltype";
    public static final String PUSH_FROM_CALLER_NAME = "fromcallername";//voicemail的名字
    public static final String PUSH_FROM_CALLER_ID = "fromcallerid";//voicemail的号码
    public static final String PUSH_IS_VIDEO_CALL = "isvideocall";//是否video来电
    public static final String PUSH_VIDEO_PREVIEW = "videopreview";//是否门口机preview来电
    public static final String DELETE_CDR = "deletecdr";
    public static final String MISS_CALL = "Missed Call";
    public static final String OTHER_LOGIN = "OtherLogin";
    public static final String MODIFY_PASSWORD = "ModifyPassword";
    public static final String LOGIN_CHANGED = "LoginChanged";
    public static final String LOGIN_EMAIL_CHANGE = "LoginEmailChange";
    public static final String NO_PERMISSION = "NoPermission";
    public static final String HEARTBEAT = "Heartbeat";
    public static final String LINKEDID = "Linkedid";
    public static final String CALLID = "CallID";
    public static final String TYPE = "Type";
    public static final String SN = "sn";
    public static final String PUSH_DONT_POP = "pushdontpop";
    public static final String PUSH_COMPANY_NAME = "company";
    public static final String CONFERENCE = "Conference";
    public static final String PUSH_CONFERENCE = "PushConference";

    public final static String YES = "yes";
    public final static String NO = "no";

    public static final int FOREGROUND_NOTIFICATION_ID = 2;
    public static final int NEW_CALL_NOTIFICATION_ID = 3;
    public static final int MISS_CALL_GROUP_ID = 4;
    public static final int VOICE_MAIL_GROUP_ID = 5;
    public static final int RE_USER_LOGIN_GROUP_ID = 6;
    public static final int MICRO_PHONE_NOTIFICATION_ID = 7;
    public static final String NOTIFICATION_GROUP_MISS_CALL = "com.yeastar.linkus.miss_call";
    public static final String NOTIFICATION_GROUP_RE_USER_LOGIN = "com.yeastar.linkus.re_user_login";
    public static final String NOTIFICATION_GROUP_VOICE_MAIL = "com.yeastar.linkus.voice_mail";
    public static final String NOTIFICATION_CHANNEL_PUSH_ID = "push";
    public static final String NOTIFICATION_CHANNEL_NEW_CALL_ID = "new_call";
    public static final String NOTIFICATION_CHANNEL_FOREGROUND_ID = "foreground";

    public static final int SYNC_SUCCESFUL = 0;
    public static final int SYNC_FAIL = 1;
}
