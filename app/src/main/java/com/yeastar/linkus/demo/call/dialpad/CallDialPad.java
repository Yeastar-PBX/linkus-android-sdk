package com.yeastar.linkus.demo.call.dialpad;

import android.app.Activity;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.DpUtil;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.SoundManager;
import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;

import java.util.ArrayList;
import java.util.List;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

public class CallDialPad {
    private DialPadCallBack dialPadCallBack;
    public final static int HOLD = 0;
    public final static int MUTE = 1;
    public final static int AUDIO = 2;
    public final static int END_CALL = 3;
    public final static int DIAL_PAD = 6;
    public final static int RECORD = 7;
    public final static int ATTENDED_TRANSFER = 8;
    public final static int BLIND_TRANSFER = 9;
    public final static int CANCEL = 12;
    public final static int TRANSFER_CONFIRM = 11;
    protected Activity activity;
    protected View mParent;
    protected GridView gridView;
    private CommonAdapter adapter;
    private List<CallDialPadVo> list = new ArrayList<>();
    private static final int[] IN_CALL_ACTION_NORMAL = {HOLD, MUTE, AUDIO, END_CALL, DIAL_PAD, RECORD, ATTENDED_TRANSFER, BLIND_TRANSFER};
    private static final int[] IN_CALL_ACTION_TRANSFER = {MUTE, AUDIO, TRANSFER_CONFIRM, CANCEL, DIAL_PAD, RECORD, HOLD, BLIND_TRANSFER};

    private RelativeLayout rl;
    private ImageView topView;
    private float currHeight;
    private float lastY;
    private int topCount = 0, bottomCount = 0;
    private int MAX_HEIGHT = 354, MIN_HEIGHT = 130, MAX_RADIUS = 57, MIN_RADIUS = 40;
    private float lastYDistance;

    public CallDialPad(Activity activity, View mParent) {
        this.activity = activity;
        this.mParent = mParent;
        initView();
    }

    private void initView() {
        gridView = mParent.findViewById(R.id.incall_btn_gv);
        initList();
        adapter = new CommonAdapter<CallDialPadVo>(list, 1) {
            @NonNull
            @Override
            public AdapterItem createItem(Object o) {
                return new CallDialPadItem(action -> {
                    if (dialPadCallBack != null) {
                        dialPadCallBack.cllBack(action);
                    }
                });
            }
        };
        gridView.setAdapter(adapter);
        gridView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!mParent.isAttachedToWindow()) {
                    return false;
                }
                gridView.getViewTreeObserver().removeOnPreDrawListener(this);
                LogUtil.i("gtd height==" + gridView.getMeasuredHeight() + "   " + gridView.getHeight());
                MAX_HEIGHT = DpUtil.px2dp(gridView.getMeasuredHeight()) + 48;
                MIN_HEIGHT = DpUtil.px2dp(getMinHeight()) + 36;
                LogUtil.i("gtd MAX_HEIGHT=" + MAX_HEIGHT + " MIN_HEIGHT=" + MIN_HEIGHT);
                initDialPad();
                return false;
            }
        });
    }

    private int getMinHeight() {
        if (gridView.getChildCount() == 0) {
            return DpUtil.dp2px(57);
        }
        int maxHeight = gridView.getChildAt(0).getMeasuredHeight();
        if (maxHeight < gridView.getChildAt(1).getMeasuredHeight()) {
            maxHeight = gridView.getChildAt(1).getMeasuredHeight();
        }
        if (maxHeight < gridView.getChildAt(2).getMeasuredHeight()) {
            maxHeight = gridView.getChildAt(2).getMeasuredHeight();
        }
        if (maxHeight < gridView.getChildAt(3).getMeasuredHeight()) {
            maxHeight = gridView.getChildAt(3).getMeasuredHeight();
        }
        return maxHeight;
    }

    private void initDialPad() {
        rl = mParent.findViewById(R.id.rl);
        topView = mParent.findViewById(R.id.iv_top);
        View.OnTouchListener topViewOnTouchListener = (v, event) -> {
            int action = event.getActionMasked();
            boolean handle = topGestureDetector.onTouchEvent(event);
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    topCount++;
                    LogUtil.i("top Action was Down count==" + topCount);
                case (MotionEvent.ACTION_MOVE):
                    LogUtil.i("top Action was MOVE");
                    return true;
                case (MotionEvent.ACTION_UP):
                    LogUtil.i("top Action was UP");
                case (MotionEvent.ACTION_CANCEL):
                    topCount--;
                    LogUtil.i("top Action was CANCEL start count==" + topCount + "  currHeight==" + currHeight + "  halfHeight==" + DpUtil.dp2px((MAX_HEIGHT + MIN_HEIGHT) / 2));
                    if (topCount == 0) {
                        if (currHeight > DpUtil.dp2px((MAX_HEIGHT + MIN_HEIGHT) / 2)) {
                            floatUI(MAX_HEIGHT);
                        } else {
                            floatUI(MIN_HEIGHT);
                        }
                    } else if (topCount == -1) {
                        topCount = 0;
                    }
                    LogUtil.i("top Action was CANCEL count==" + topCount);
                default:
                    return handle;
            }

        };
        topView.setOnTouchListener(topViewOnTouchListener);

        View.OnTouchListener bottomViewOnTouchListener = (v, event) -> {
            int action = event.getActionMasked();
            boolean handle = bottomGestureDetector.onTouchEvent(event);
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    LogUtil.i("bottom Action was Down count==" + bottomCount);
                case (MotionEvent.ACTION_MOVE):
                    LogUtil.i("bottom Action was MOVE");
                    return true;
                case (MotionEvent.ACTION_UP):
                    LogUtil.i("bottom Action was UP");
                case (MotionEvent.ACTION_CANCEL):
                    bottomCount--;
                    LogUtil.i("bottom Action was CANCEL count==" + bottomCount);
                    if (bottomCount == -1) {
                        if (currHeight > DpUtil.dp2px((MAX_HEIGHT + MIN_HEIGHT) / 2)) {
                            floatUI(MAX_HEIGHT);
                        } else {
                            floatUI(MIN_HEIGHT);
                        }
                    }
                    bottomCount = 0;
                    LogUtil.i("bottom Action was CANCEL");
                default:
                    return handle;
            }

        };
        gridView.setOnTouchListener(bottomViewOnTouchListener);
        floatUI(CallManager.getInstance().isUnfoldDialPad() ? MAX_HEIGHT : MIN_HEIGHT);

    }

    //响铃状态,都不可用
    public void calling() {
        initList();
        for (CallDialPadVo vo : list) {
            if (isTransfer()) {
                //转移并且已成功呼出的情况下,[确认转移][取消转移][音频选项]等可用,其余禁用
                if (vo.getAction() != TRANSFER_CONFIRM && vo.getAction() != AUDIO && vo.getAction() != CANCEL) {
                    vo.setEnabled(false);
                } else {
                    vo.setEnabled(true);
                }
            } else {
                if (vo.getAction() != END_CALL && vo.getAction() != AUDIO) {
                    vo.setEnabled(false);
                } else {
                    vo.setEnabled(true);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void initList() {
        list.clear();
        if (isTransfer()) {
            for (Integer action : IN_CALL_ACTION_TRANSFER) {
                list.add(new CallDialPadVo(action));
            }
        } else {
            for (Integer action : IN_CALL_ACTION_NORMAL) {
                list.add(new CallDialPadVo(action));
            }
        }
    }

    private boolean isTransfer() {
        if (YlsCallManager.getInstance().getCallListCount() > 1) {
            InCallVo inCallVo = YlsCallManager.getInstance().getCallList().getFirst();
            return inCallVo.isTransfer();
        }
        return false;
    }

    //转移并且已接听的情况下，只有[拨号键盘]可用，其余禁用
    //转移并且已成功呼出的情况下,[确认转移][取消转移][拨号键盘][音频选项][静音]([录音]根据权限判断是否可用)等可用,其余禁用
    public void transferConnected(InCallVo inCallVo) {
        initList();
        //S系列只有[确认转移][取消转移][拨号键盘][音频选项][静音]可用,其他都置灰
        for (CallDialPadVo vo : list) {
            if (vo.getAction() == MUTE || vo.getAction() == AUDIO || vo.getAction() == TRANSFER_CONFIRM
                    || vo.getAction() == CANCEL || vo.getAction() == DIAL_PAD) {
                vo.setEnabled(true);
            } else {
                vo.setEnabled(false);
            }
        }
        //P系列同普通通话规则
        if (inCallVo.isRecord()) {
            setSinglePress(RECORD);
        }
        //分机的ctlRecord是no,通话是永久disable(固定会议室),ctlRecord是yes且通话是stop状态,这些情况下都是disable
        if (YlsLoginManager.getInstance().isDisableRecord() || inCallVo.isAlwaysDisableRecord()
                || (YlsLoginManager.getInstance().isPauseRecord() && !inCallVo.isRecordAble())) {
            setSingleDisable(RECORD);
        } else {
            setSingleEnable(RECORD);
        }
        if (inCallVo.isMute()) {
            setSinglePress(MUTE);
        }
        adapter.notifyDataSetChanged();
    }

    //接通状态下
    public void updateCallDialPad(InCallVo inCallVo) {
        initList();
        //连接蓝牙或者打开外放喇叭时是选中状态
        if (MediaUtil.getInstance().isBTConnected() || SoundManager.getInstance().isSpeakerOn()) {
            setSinglePress(AUDIO);
        }
        if (inCallVo.isRecord()) {
            setSinglePress(RECORD);
        }
        //P系列录音多了录音中不可点击状态
        //分机的ctlRecord是no,通话是永久disable(固定会议室),ctlRecord是yes且通话是stop状态,这些情况下都是disable
        if (YlsLoginManager.getInstance().isDisableRecord() || inCallVo.isAlwaysDisableRecord()
                || (YlsLoginManager.getInstance().isPauseRecord() && !inCallVo.isRecordAble())) {
            setSingleDisable(RECORD);
        } else {
            setSingleEnable(RECORD);
        }
        //hold的时候不能发特征码
        if (inCallVo.isHold()) {
            setSinglePress(HOLD);
            setSingleDisable(DIAL_PAD);
        }
        if (inCallVo.isMute()) {
            setSinglePress(MUTE);
        }
        //web端的多方通话禁止转移,flip,record
        if (!TextUtils.isEmpty(inCallVo.getWebConferenceId())) {
            setSingleDisable(ATTENDED_TRANSFER);
            setSingleDisable(BLIND_TRANSFER);
            setSingleDisable(RECORD);
        }
        adapter.notifyDataSetChanged();
    }

    private void setSingleEnable(int action) {
        for (CallDialPadVo vo : list) {
            if (vo.getAction() == action) {
                vo.setEnabled(true);
            }
        }
    }

    private void setSingleDisable(int action) {
        for (CallDialPadVo vo : list) {
            if (vo.getAction() == action) {
                vo.setEnabled(false);
            }
        }
    }

    private void setSinglePress(int action) {
        for (CallDialPadVo vo : list) {
            if (vo.getAction() == action) {
                vo.setPressed(true);
            }
        }
    }

    public void setCallBack(DialPadCallBack callBack) {
        this.dialPadCallBack = callBack;
    }

    public interface DialPadCallBack {
        void cllBack(int action);
    }

    private class DialPadPathGenerator implements PathGenerator {
        private int maxRadius, minRadius, mHeight, maxHeight, minHeight;

        private Path mPath = new Path();

        public DialPadPathGenerator(int maxRadius, int minRadius, int mHeight, int maxHeight, int minHeight) {
            this.maxRadius = maxRadius;
            this.minRadius = minRadius;
            this.mHeight = mHeight;
            this.maxHeight = maxHeight;
            this.minHeight = minHeight;
        }

        @Override
        public Path generatePath(Path old, View view, int width, int height) {
            if (old == null) {
                old = new Path();
            } else {
                old.reset();
            }
            int radius = DpUtil.dp2px((maxRadius - minRadius) * (maxHeight - mHeight) / (maxHeight - minHeight) + minRadius);
            currHeight = DpUtil.dp2px(mHeight);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                old.addRoundRect(0, height - DpUtil.dp2px(mHeight - 16), width, height, radius, radius, Path.Direction.CW);
            } else {
                old.addRoundRect(new RectF(0, height - DpUtil.dp2px(mHeight - 16), width, height), radius, radius, Path.Direction.CW);
            }
            old.close();
            mPath.reset();
            mPath.addCircle(width / 2, height - DpUtil.dp2px(mHeight - 16), DpUtil.dp2px(16), Path.Direction.CW);
            mPath.close();
            old.op(mPath, Path.Op.UNION);
            return old;
        }
    }

    private void floatUI(int dp) {
        if (dp == MAX_HEIGHT) {
            topView.setImageResource(R.mipmap.icon_arrow_down);
            CallManager.getInstance().setUnfoldDialPad(true);
        } else if (dp == MIN_HEIGHT) {
            topView.setImageResource(R.mipmap.icon_arrow_up);
            CallManager.getInstance().setUnfoldDialPad(false);
        } else {
            topView.setImageResource(R.mipmap.icon_arrow_vertical);
        }
        new PathInfo.Builder(new DialPadPathGenerator(MAX_RADIUS, MIN_RADIUS, dp, MAX_HEIGHT, MIN_HEIGHT), rl)
                .create()
                .apply();
        int padding = DpUtil.dp2px(MAX_HEIGHT - dp);
        LogUtil.i("gtd dp==" + dp + " padding==" + padding);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl.getLayoutParams();
        layoutParams.height = DpUtil.dp2px(MAX_HEIGHT);
        layoutParams.width = rl.getWidth();
        rl.setLayoutParams(layoutParams);
        rl.setPaddingRelative(0, padding, 0, 0);
    }


    GestureDetectorCompat topGestureDetector = new GestureDetectorCompat(activity, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (lastY == -1) {
                lastY = e1.getY();
            }
            float yDistance = e2.getY() - lastY;
            lastY = e2.getY();
            LogUtil.i("onScroll:  yDistance==" + yDistance + " lastYDistance==" + lastYDistance);
            if (isScroll) {
                isScroll = false;
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            lastYDistance = yDistance;
            float maxHeight = DpUtil.dp2px(MAX_HEIGHT);
            float minHeight = DpUtil.dp2px(MIN_HEIGHT);

            LogUtil.i("onScroll:  yDistance==" + yDistance + "  currHeight==" + currHeight);
            if (yDistance > 0 && currHeight > minHeight) {//向下收缩
                if ((currHeight - yDistance) > DpUtil.dp2px(MIN_HEIGHT)) {
                    int dpY = DpUtil.px2dp(currHeight - yDistance);
                    isScroll = true;
                    floatUI(dpY);
                }

            } else if (yDistance < 0 && currHeight < maxHeight) {//向上扩大
                float y = Math.abs(yDistance);
                if (currHeight + y < DpUtil.dp2px(MAX_HEIGHT)) {
                    int dpY = DpUtil.px2dp(currHeight + y);
                    isScroll = true;
                    floatUI(dpY);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LogUtil.i("onFling: " + e1.toString() + e2.toString() + "velocityY==" + velocityY);
            if (Math.abs(velocityY) > 50) {
                topCount--;
                if (e1.getY() > e2.getY()) {
                    floatUI(MAX_HEIGHT);
                } else if (e1.getY() < e2.getY()) {
                    floatUI(MIN_HEIGHT);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            LogUtil.i("onSingleTapConfirmed currHeight==" + currHeight);
            if (currHeight == DpUtil.dp2px(MIN_HEIGHT)) {
                floatUI(MAX_HEIGHT);
            } else {
                floatUI(MIN_HEIGHT);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            lastY = -1;
            return true;
        }

    });

    //避免手势滑动改变拨号盘UI时造成的联动
    private boolean isScroll;
    GestureDetectorCompat bottomGestureDetector = new GestureDetectorCompat(activity, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (lastY == -1 && e1 != null) {
                lastY = e1.getY();
            }
            float yDistance = e2.getY() - lastY;
            lastY = e2.getY();
            LogUtil.i("bottom onScroll:  yDistance==" + yDistance + " lastYDistance==" + lastYDistance);
            if (isScroll) {
                LogUtil.i("bottom onScroll:  distanceY==" + distanceY);
                isScroll = false;
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            lastYDistance = yDistance;
            float maxHeight = DpUtil.dp2px(MAX_HEIGHT);
            float minHeight = DpUtil.dp2px(MIN_HEIGHT);

            LogUtil.i("bottom onScroll:  yDistance==" + yDistance + "  currHeight==" + currHeight);
            if (yDistance > 0 && currHeight > minHeight) {//向下收缩
                if ((currHeight - yDistance) > DpUtil.dp2px(MIN_HEIGHT)) {
                    int dpY = DpUtil.px2dp(currHeight - yDistance);
                    isScroll = true;
                    floatUI(dpY);
                }

            } else if (yDistance < 0 && currHeight < maxHeight) {//向上扩大
                float y = Math.abs(yDistance);
                if (currHeight + y < DpUtil.dp2px(MAX_HEIGHT)) {
                    int dpY = DpUtil.px2dp(currHeight + y);
                    isScroll = true;
                    floatUI(dpY);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            LogUtil.i("bottom onFling: " + e1.toString() + e2.toString() + "velocityY==" + velocityY);
            if (Math.abs(velocityY) > 50 && e1 != null && e2 != null) {
                bottomCount--;
                if (e1.getY() > e2.getY()) {
                    floatUI(MAX_HEIGHT);
                    topView.setImageResource(R.mipmap.icon_arrow_down);
                } else if (e1.getY() < e2.getY()) {
                    floatUI(MIN_HEIGHT);
                    topView.setImageResource(R.mipmap.icon_arrow_up);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            lastY = -1;
            return true;
        }

    });

    public void setVisible(boolean visible) {
        if (rl != null) {
            rl.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

}
