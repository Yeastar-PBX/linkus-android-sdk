package com.yeastar.linkus.demo.conference.detail;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.utils.permission.PermissionRequest;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceMemberVo;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;

import java.util.List;

/**
 * Created by ted on 17-4-25.
 */

public class ConferenceDetailPresenter implements ConferenceDetailContract.Presenter {

    private Activity context;
    private ConferenceDetailContract.View view;

    public ConferenceDetailPresenter(Activity context, ConferenceDetailContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void startMeeting(ConferenceVo conferenceVo) {
        conferenceVo.setAdmin(YlsLoginManager.getInstance().getMyExtension());
        view.showStartProgressDialog();
        startConference(context, conferenceVo);
    }

    @Override
    public void sortList(ConferenceVo conferenceVo) {
        String myExtension = YlsLoginManager.getInstance().getMyExtension();
        if (!TextUtils.isEmpty(myExtension)) {
            int index = findIndexByExtension(conferenceVo.getMemberList(), myExtension);
            if (index > 0) {
                ConferenceMemberVo vo = conferenceVo.getMemberList().remove(index);
                conferenceVo.getMemberList().add(0, vo);
            }
        }
    }

    private int findIndexByExtension(List<ConferenceMemberVo> list, String extension) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getNumber().equals(extension)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void startConference(final Activity activity, final ConferenceVo conferenceVo) {
        PermissionRequest request = new PermissionRequest(activity, new PermissionRequest.PermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                jumpToConferenceInCallFragment(conferenceVo, activity);
            }

            @Override
            public void onFailure(List<String> permissions) {
                view.dismissProgressDialog();
            }

        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO);
        }
    }

    private void jumpToConferenceInCallFragment(ConferenceVo conferenceVo, Activity activity) {
        if (conferenceVo == null) {
            LogUtil.w("jumpToConferenceInCallFragment 会议室为空");
            view.dismissProgressDialog();
            return;
        }
        //默认响铃状态
        conferenceVo.resetMemberStatus(0);
        conferenceVo.setConferenceId(null);
        conferenceVo.setId(null);
        YlsConferenceManager.getInstance().setConferenceVo(conferenceVo);
        LogUtil.w("---- 创建新会议室 ----");
        sortList(conferenceVo);
        String[] numberArray = conferenceVo.getNumberArray();
        if (numberArray == null || numberArray.length == 0) {
            return;
        }
        YlsConferenceManager.getInstance().startConference(activity, conferenceVo.getName(), numberArray, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                view.dismissProgressDialog();
                activity.finish();
                Intent intent = new Intent(activity, CallContainerActivity.class);
                intent.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }

            @Override
            public void onFailed(int code) {
                view.dismissProgressDialog();
                YlsConferenceManager.getInstance().setConferenceVo(null);
                switch (code) {
                    case YlsConstant.CONFERENCE_NAME_REGEX_ERROR:
                        ToastUtil.showToast("会议室名称长度不能超过63个字节");
                    case YlsConstant.CONFERENCE_NAME_LENGTH_ERROR:
                        ToastUtil.showLongToast("会议室名称不能使用包含不包含 :、!、$、(、)、/、#、;、,、[、]、\"、=、<、>、&、\\、'、```、^、%、@、{、}、|、空格");
                        break;
                    case YlsConstant.CONFERENCE_IN_USE_ERROR:
                        ToastUtil.showToast(R.string.conference_tip_meeting);
                        break;
                    case YlsConstant.SDK_NETWORK_DISABLE:
                    case YlsConstant.SDK_LOGIN_DISABLE:
                        ToastUtil.showToast(R.string.connectiontip_connect_fail);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onException(Throwable exception) {
                view.dismissProgressDialog();
            }
        });
    }
}
