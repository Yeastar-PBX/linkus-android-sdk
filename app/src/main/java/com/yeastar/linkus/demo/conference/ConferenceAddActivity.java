package com.yeastar.linkus.demo.conference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseActivity;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;

import org.greenrobot.eventbus.EventBus;

public class ConferenceAddActivity extends BaseActivity {

    private DialPadLayout dialPadLayout;
    private ClickImageView dialPadFold;
    private String number;
    private ConferenceVo conferenceVo;
    private int type;


    public ConferenceAddActivity() {
        super(R.layout.activity_conference_add);
    }

    public static void start(Context context, ConferenceVo conferenceVo, int type) {
        ConferenceManager.getInstance().setAdd(true);
        Intent starter = new Intent(context, ConferenceAddActivity.class);
        starter.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
        starter.putExtra(Constant.EXTRA_FROM, type);
        context.startActivity(starter);
    }

    public static void start(Context context, ConferenceVo conferenceVo) {
        Intent starter = new Intent(context, ConferenceAddActivity.class);
        starter.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
        context.startActivity(starter);
    }

    @Override
    public void beforeSetView() {
    }

    @Override
    public void findView() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.EXTRA_CONFERENCE)) {
            conferenceVo = (ConferenceVo) intent.getSerializableExtra(Constant.EXTRA_CONFERENCE);
        } else {
            conferenceVo = YlsConferenceManager.getInstance().getConferenceVo();
        }
        type = intent.getIntExtra(Constant.EXTRA_FROM, 0);
        dialPadLayout = findViewById(R.id.dial_pad_layout);
        dialPadFold = findViewById(R.id.dial_pad_fold);
        dialPadLayout.setDialPadCallBack(str -> {
            number = str;
        });
        dialPadFold.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(number)) {
                Intent resultIntent = new Intent();
                if (type == Constant.IN_CONFERENCE) {
                    resultIntent.putExtra(Constant.EXTRA_NUMBER, number);
                } else {
                    conferenceVo.addMember(number, number);
                    resultIntent.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
                }
                EventBus.getDefault().post(new AgentEvent(0, Activity.RESULT_OK, resultIntent));
                finish();
            } else {
                ToastUtil.showLongToast("Please input number");
                finish();
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConferenceManager.getInstance().setAdd(false);
    }
}