package com.yeastar.linkus.demo.call.multipartyCall;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.CallQualityDialog;
import com.yeastar.linkus.demo.call.InCallRelatedFragment;
import com.yeastar.linkus.demo.eventbus.CallQualityEvent;
import com.yeastar.linkus.demo.eventbus.CallStateEvent;
import com.yeastar.linkus.demo.eventbus.NetWorkLevelEvent;
import com.yeastar.linkus.demo.utils.DialogUtil;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.CallQualityVo;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultipartyCallManagerFragment extends InCallRelatedFragment {

    private MultipartyCallsAdapter callsAdapter;
    private View ivBack;
    private CallQualityDialog callQualityDialog;
    private ScheduledExecutorService scheduledExec;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerCallStatus(CallStateEvent callStateEvent) {
        int callStatus = callStateEvent.getStatus();
        int callStatusId = callStateEvent.getCallId();
        LogUtil.w("multiPartyCalls 通话状态通知 callId=" + callStatusId + "  通话状态=" + callStatus);
        LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
        if (callList != null) {
            int size = callList.size();
            if (size == 1) {
                // 多方通话变成单通，退出多方通话管理页面
                activity.getSupportFragmentManager().popBackStack();
            } else {
                // 其他情况刷新页面
                if (callsAdapter != null) {
                    callsAdapter.setList(callList);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handlerNetWorkLevel(NetWorkLevelEvent netWorkLevelEvent) {
        LogUtil.w("multiPartyCalls NetWorkLevelEvent callId=" + netWorkLevelEvent.getCallId() + "  level=" + netWorkLevelEvent.getNetworkLevel());
        if (callsAdapter != null) {
            callsAdapter.setCallQualityLevel(netWorkLevelEvent.getCallId(), netWorkLevelEvent.getNetworkLevel());
            callsAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCallQuality(CallQualityEvent callQualityEvent) {
        if (callQualityEvent.isP2p()) return;
        List<CallQualityVo> callQualityVos = callQualityEvent.getCallQualityVos();
        if (callQualityDialog != null) {
            CallQualityVo callQualityModel = getCallQualityModel(callQualityVos, callQualityDialog.getCallId());
            if (callQualityModel != null) {
                callQualityDialog.showCallQuality(callQualityModel);
            }
        }
    }

    private CallQualityVo getCallQualityModel(List<CallQualityVo> callQualityVos, int callId) {
        if (CommonUtil.isListNotEmpty(callQualityVos)) {
            for (CallQualityVo callQualityModel : callQualityVos) {
                if (callQualityModel.getCallId() == callId) {
                    return callQualityModel;
                }
            }
        }
        return null;
    }

    public MultipartyCallManagerFragment() {
        super(R.layout.fragment_multi_party_call_manager);
    }

    @Override
    public void findView(View parent) {
        super.findView(parent);
        ivBack = parent.findViewById(R.id.ivBack);
        RecyclerView rv = parent.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(activity));
        callsAdapter = new MultipartyCallsAdapter();
        LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
        callsAdapter.setNewInstance(callList);
        rv.setAdapter(callsAdapter);
        setListener();
    }

    public void setListener() {
        ivBack.setOnClickListener(v -> activity.getSupportFragmentManager().popBackStack());
        callsAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            int id = view.getId();
            InCallVo inCallVo = (InCallVo) adapter.getData().get(position);
            LogUtil.w("multiPartyCalls 被操作的成员信息：" + inCallVo);
            if (id == R.id.ivMute) {
                LogUtil.w("multiPartyCalls 静音 单个成员");
                YlsCallManager.getInstance().muteSingleMember(inCallVo);
                callsAdapter.notifyItemChanged(position);
            } else if (id == R.id.ivRemove) {
                DialogUtil.showDoubleDialog(activity, 0, R.string.call_tip_remove_member, R.string.public_ok, R.string.public_cancel, (dialog, which) -> {
                    LogUtil.w("multiPartyCalls 移除 单个成员");
                    YlsCallManager.getInstance().hangUpSingleCall(activity, inCallVo.getCallId());
                    callsAdapter.notifyItemRemoved(position);
                }, null, true);
            } else {
                LogUtil.w("multiPartyCalls 显示单个成员通话质量");
                initCallQuality(inCallVo.getCallId());
            }
        });
    }

    private void initCallQuality(int callId) {
        scheduledExec = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            int[] callIds = YlsCallManager.getInstance().getCallIdArrays();
            List<CallQualityVo> list = new ArrayList<>();
            for (int aCallId : callIds) {
                CallQualityVo callQualityVo = YlsCallManager.getInstance().getCallQuality();
                if (callQualityVo == null) {
                    callQualityVo = new CallQualityVo("--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--");
                }
                callQualityVo.setCallId(aCallId);
                list.add(callQualityVo);
            }
            EventBus.getDefault().post(new CallQualityEvent(list));
        };
        scheduledExec.scheduleAtFixedRate(task, 0, 1000 * 3, TimeUnit.MILLISECONDS);
        callQualityDialog = new CallQualityDialog(getActivity());
        callQualityDialog.setCurrentCallId(callId);
        callQualityDialog.show();
        callQualityDialog.setOnDismissListener(dialog -> {
            callQualityDialog = null;
            if (scheduledExec != null && !scheduledExec.isShutdown()) {
                List<Runnable> runnableList = scheduledExec.shutdownNow();
                if (CommonUtil.isListNotEmpty(runnableList)) {
                    for (Runnable runnable : runnableList) {
                        FutureTask<Void> cancelTask = (FutureTask) runnable;
                        if (!cancelTask.isCancelled()) {
                            cancelTask.cancel(true);
                        }
                    }
                }
                scheduledExec = null;
            }
        });
    }
}
