
package com.yeastar.linkus.demo.conference.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseActivity;
import com.yeastar.linkus.demo.conference.ConferenceAddActivity;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.utils.DensityUtil;
import com.yeastar.linkus.demo.utils.DialogUtil;
import com.yeastar.linkus.demo.utils.OnNoDoubleClickListener;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceMemberVo;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

public class ConferenceDetailActivity extends BaseActivity implements ConferenceDetailContract.View {

    private EditText nameEt = null;
    private GridView memberGv = null;
    private Button startBtn = null;
    private CommonAdapter<ConferenceMemberVo> adapter;
    private ConferenceVo conferenceVo = null;
    private ConferenceDetailPresenter presenter;
    private boolean isCountDown = false;
    private AvatarImageView adminIv;
    private TextView adminNameTv;
    private TextView adminNumberTv;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ConferenceDetailActivity.class));
    }

    // 响应来自 AgentActivity 的事件。
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAgentEvent(AgentEvent event) {
        int requestCode = event.getRequestCode();
        int resultCode = event.getResultCode();
        Intent data = event.getData();
        YlsConferenceManager.getInstance().addNullMember(conferenceVo.getMemberList());
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            conferenceVo = (ConferenceVo) data.getSerializableExtra(Constant.EXTRA_CONFERENCE);
        }
        updateAdapter();
        checkValid();
    }

    public ConferenceDetailActivity() {
        super(R.layout.activity_conference_new);
        presenter = new ConferenceDetailPresenter(this, ConferenceDetailActivity.this);
    }

    @Override
    public void beforeSetView() {

    }

    @Override
    public void findView() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initUi();
        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void setListener() {
        startBtn.setOnClickListener(new OnNoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                LogUtil.w("手动发起会议室");
                YlsConferenceManager.getInstance().removeNullMember(conferenceVo.getMemberList());
                initName();
                presenter.startMeeting(conferenceVo);
            }
        });
    }

    private void updateAdapter() {
        initAdminInfo();
        if (conferenceVo != null) {
            List<ConferenceMemberVo> list = conferenceVo.getMemberList();
            List<ConferenceMemberVo> newList = new ArrayList<>();
            if (CommonUtil.isListNotEmpty(list)) {
                for (ConferenceMemberVo model : list) {
                    newList.add(new ConferenceMemberVo(model.getNumber(), model.getOriginalNumber()));
                }
                conferenceVo.setMemberList(newList);
            }
            YlsConferenceManager.getInstance().addNullMemberByAdmin(conferenceVo.getMemberList());
            adapter.setData(conferenceVo.getMemberListWithoutAdmin(YlsLoginManager.getInstance().getMyExtension()));
            adapter.notifyDataSetChanged();
            memberGv.setAdapter(adapter);
        }
    }

    @Override
    public void setPresenter(ConferenceDetailContract.Presenter presenter) {
    }

    @Override
    public void initUi() {
        nameEt = findViewById(R.id.conference_name_et);
        memberGv = findViewById(R.id.conference_member_gv);
        startBtn = findViewById(R.id.conference_start_btn);
        adminIv = findViewById(R.id.admin_photo_civ);
        adminNameTv = findViewById(R.id.admin_name_tv);
        adminNumberTv = findViewById(R.id.admin_number_tv);

        ViewGroup.LayoutParams params = memberGv.getLayoutParams();
        params.height = DensityUtil.getScreenWidth(this) - 80;
        memberGv.setLayoutParams(params);

        Intent intent = getIntent();
        if (intent != null) {
            conferenceVo = (ConferenceVo) intent.getSerializableExtra(Constant.EXTRA_CONFERENCE);
        }
        String myExtension = YlsLoginManager.getInstance().getMyExtension();
        if (isAdd()) {
            //新增第一个是自己的分机号
            conferenceVo = new ConferenceVo();
            conferenceVo.setAdmin(myExtension);
        } else {
            String name = TextUtils.isEmpty(conferenceVo.getName()) ? getString(R.string.conference_conference) : conferenceVo.getName();
            nameEt.setText(name);
            //把管理员放在第一个
//            sortList(conferenceVo.getAdmin(), conferenceVo.getMemberList());
            presenter.sortList(conferenceVo);
        }
        initAdapter();
        memberGv.setAdapter(adapter);
        updateAdapter();
        countDownBtn();

    }

    private void initAdminInfo() {
        String myExtension = YlsLoginManager.getInstance().getMyExtension();
        adminNameTv.setText(myExtension);
        adminNumberTv.setText(myExtension);
    }

    private void initAdapter() {
        adapter = new CommonAdapter<ConferenceMemberVo>(conferenceVo.getMemberListWithoutAdmin(YlsLoginManager.getInstance().getMyExtension()), 1) {
            @NonNull
            @Override
            public AdapterItem<ConferenceMemberVo> createItem(Object o) {
                return new ConferenceDetailItem(new ConferenceDetailItem.ItemCallBack() {
                    @Override
                    public void deleteContact(final int position) {
                        if (conferenceVo != null) {
                            ConferenceMemberVo item = (ConferenceMemberVo) adapter.getItem(position);
                            conferenceVo.getMemberList().remove(item);
                            updateAdapter();
                            checkValid();
                        }
                    }

                    public void removeMember() {
                        makeMemberDeleteAble();
                    }

                    @Override
                    public void addMember() {
                        resetMemberDeleteState();
                        YlsConferenceManager.getInstance().removeNullMember(conferenceVo.getMemberList());
                        ConferenceAddActivity.start(activity, conferenceVo);
                    }
                });
            }
        };
    }

    private void makeMemberDeleteAble() {
        List<ConferenceMemberVo> withoutAdmin = conferenceVo.getMemberListWithoutAdmin(YlsLoginManager.getInstance().getMyExtension());
        for (ConferenceMemberVo model : withoutAdmin) {
            if (model.getTag() == ConferenceMemberVo.MemberTag.NORMAL) {
                model.setDeleteAble(!model.isDeleteAble());
            }
        }
        adapter.setData(withoutAdmin);
        adapter.notifyDataSetChanged();
    }

    private void resetMemberDeleteState() {
        List<ConferenceMemberVo> withoutAdmin = conferenceVo.getMemberListWithoutAdmin(YlsLoginManager.getInstance().getMyExtension());
        for (ConferenceMemberVo model : withoutAdmin) {
            if (model.getTag() == ConferenceMemberVo.MemberTag.NORMAL) {
                model.setDeleteAble(false);
            }
        }
        adapter.setData(withoutAdmin);
        adapter.notifyDataSetChanged();
    }

    //开始会议室按钮倒计时
    private void countDownBtn() {
        long count = YlsConferenceManager.getInstance().getCountDownTime();
//        long count = YlsConferenceManager.getInstance().getEndConferenceTime() + 10000 - System.currentTimeMillis();
        if (count > 10 * 1000 || count < 0) {//当前时间不可能比之前的会议室结束时间更早或者当前时间比会议室结束时间晚太多
            startBtn.setAlpha(1f);
            startBtn.setEnabled(true);
            isCountDown = false;
            checkValid();
        } else {//倒计时
            startBtn.setAlpha(0.5f);
            startBtn.setEnabled(false);
            isCountDown = true;
            final String btnName = getString(R.string.conference_start);
            new CountDownTimer(count, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    startBtn.setText(String.format(Locale.getDefault(), "%s (%d)", btnName, millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    startBtn.setText(btnName);
                    startBtn.setAlpha(1f);
                    startBtn.setEnabled(true);
                    isCountDown = false;
                    checkValid();
                }
            }.start();
        }
    }

    private void checkValid() {
        if (!isCountDown) {
            if (conferenceVo.getMemberList().size() < 3) {
                startBtn.setAlpha(0.5f);
                startBtn.setEnabled(false);
            } else {
                startBtn.setAlpha(1f);
                startBtn.setEnabled(true);
            }
        }
    }

    private boolean isAdd() {
        return conferenceVo == null || TextUtils.isEmpty(conferenceVo.getConferenceId());
    }

    @Override
    public void showStartProgressDialog() {
        showProgressDialog(R.string.conference_start);
    }

    @Override
    public void dismissProgressDialog() {
        closeProgressDialog();
    }

    private void initName() {
        String name = nameEt.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.conference_conference);
        }
        conferenceVo.setName(name);
    }

}
