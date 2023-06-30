package com.yeastar.linkus.demo.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.DensityUtil;
import com.yeastar.linkus.demo.utils.TimeUtil;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.service.base.ConnectionUtils;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.utils.NetWorkUtil;


/**
 * Created by ted on 17-7-13.
 */

public class InCallContractItem extends LinearLayout {

    private AvatarImageView mInCallAvatarCiv;
    private TextView mInCallNameTv;
    private Chronometer mInCallTimeTv;
    private TextView mTvIncallHoldTitle;
    private boolean isZoom;
    private TextView mInCallNumberTv;
    private TextView mInCallCompanyTv;
    private Context context;


    public InCallContractItem(Context context) {
        super(context);
        init(context);
    }

    public InCallContractItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    public InCallContractItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InCallContract);
        isZoom = a.getBoolean(R.styleable.InCallContract_zoom, false);
        a.recycle();
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView;
        convertView = inflater.inflate(isZoom ? R.layout.item_incall_contact_zoom_out : R.layout.item_incall_contact, this);
        mInCallAvatarCiv = convertView.findViewById(R.id.civ_incall_avatar);
        mInCallNameTv = convertView.findViewById(R.id.tv_incall_name);
        mInCallCompanyTv = convertView.findViewById(R.id.tv_incall_company);
        mInCallNumberTv = convertView.findViewById(R.id.tv_incall_number);
        mInCallTimeTv = convertView.findViewById(R.id.tv_incall_time);
        mTvIncallHoldTitle = convertView.findViewById(R.id.tv_incall_hold_title);
        mInCallTimeTv.setOnChronometerTickListener(chronometer -> TimeUtil.setChronometerFormat(mInCallTimeTv));
        mInCallAvatarCiv.setVisibility(VISIBLE);
        mInCallNumberTv.setVisibility(VISIBLE);
        mInCallCompanyTv.setVisibility(VISIBLE);
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        mInCallNameTv.setEllipsize(where);
        MarginLayoutParams p = (MarginLayoutParams) mInCallNameTv.getLayoutParams();
        int margin;
        if (where == TextUtils.TruncateAt.MARQUEE) {
            margin = DensityUtil.dp2px(context, 30);
        } else {
            margin = DensityUtil.dp2px(context, 10);
        }
        p.setMarginStart(margin);
        p.setMarginEnd(margin);
        mInCallNameTv.requestLayout();
        mInCallNameTv.setMarqueeRepeatLimit(-1);
    }

    public void setTimerText(InCallVo inCallVo) {
        mInCallTimeTv.setVisibility(VISIBLE);
        mInCallTimeTv.stop();
        int state = inCallVo.getCallState();
        boolean isNetworkConnected = NetWorkUtil.isNetworkConnected(context);
        if (!isNetworkConnected) {
            mInCallTimeTv.setText(R.string.sip_nonetwork);
            mTvIncallHoldTitle.setVisibility(GONE);
        } else if (state == YlsConstant.SIP_STATE_NULL) {
            mInCallTimeTv.setText(R.string.call_connect_server);
        } else if (state == YlsConstant.SIP_CALLING) {
            // 呼出时如果是未注册上择显示正在注册中
            if (inCallVo.isCallOut() && !ConnectionUtils.isRegister()) {
                mInCallTimeTv.setText(R.string.call_registering);
            } else {
                mInCallTimeTv.setText(R.string.call_calling);
            }
        } else if (state == YlsConstant.SIP_CALL_EARLY) {
            if (inCallVo.isCallOut()) {
                mInCallTimeTv.setText(R.string.call_ringing);
            } else {
                mInCallTimeTv.setText(R.string.call_incoming);
            }
        } else if (state == YlsConstant.SIP_CONFIRMED) {
            if (inCallVo.isHold()) {
                setTimerByStart(inCallVo.getHoldStartTime());
                mTvIncallHoldTitle.setVisibility(VISIBLE);
                mInCallTimeTv.setTextColor(getResources().getColor(R.color.red_5));
            } else {
                setTimerByStart(inCallVo.getStartTime());
            }
        }
    }

    private void setTimerByStart(long time) {
        mInCallTimeTv.setVisibility(VISIBLE);
        mTvIncallHoldTitle.setVisibility(GONE);
        mInCallTimeTv.setTextColor(getResources().getColor(R.color.color_text_normal));
        mInCallTimeTv.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - time));
        mInCallTimeTv.setFormat("%s");
        mInCallTimeTv.start();
    }

    public void setTimerStop() {
        mInCallTimeTv.setText("");
        mInCallTimeTv.stop();
        mInCallTimeTv.setVisibility(GONE);
    }

    public void setTimerFlip() {
        setTimerStop();
        mInCallTimeTv.setVisibility(VISIBLE);
        mInCallTimeTv.setText(R.string.call_calling);
    }

    public void setHoldCall(InCallVo inCallVo) {
        setTimerStop();
        inCallVo.setHoldStartTime(System.currentTimeMillis());
    }

    public void setContact(InCallVo inCallVo, boolean isCenter) {
        boolean isConference = !TextUtils.isEmpty(inCallVo.getConfId());
        setContact(inCallVo.getCallName(), inCallVo.getCallNumber(), inCallVo.getTrunk(), inCallVo.getCompany(),
                isCenter, isConference, inCallVo.isCallOut());
        setPhoto((String) inCallVo.getObject());
    }

    public void setHoldContact(InCallVo inCallVo) {
        String name = inCallVo.getCallName();
        name = getCallName(name, inCallVo.getCallNumber());
        mInCallNameTv.setText(name);
        mInCallNumberTv.setVisibility(View.GONE);
        mInCallCompanyTv.setVisibility(GONE);
        setPhoto((String) inCallVo.getObject());
        setTimerStop();
    }

    public void setContact(String name, String number, String truck, String company, boolean isCenter, boolean isConference, boolean isCallOut) {
        if (isConference) {
            mInCallNameTv.setText(R.string.conference_conference);
            mInCallNumberTv.setVisibility(GONE);
        } else {
            //呼出不显示中继
            name = getCallName(name, number);
            mInCallNameTv.setText(name);
            if (!isCenter || isCallOut) {
                truck = null;
            }
            //号码和中继都不为空的时候显示号码+中继
            if (!name.equals(number) && !TextUtils.isEmpty(truck)) {
                String temp = String.format("%s | %s", number, truck);
                mInCallNumberTv.setVisibility(VISIBLE);
                mInCallNumberTv.setText(temp);
                //号码等于名字且中继为空的时候不显示号码
            } else if (name.equals(number) && TextUtils.isEmpty(truck)) {
                mInCallNumberTv.setVisibility(GONE);
            } else {
                //名字等于号码的时候，显示中继
                mInCallNumberTv.setVisibility(VISIBLE);
                if (name.equals(number)) {
                    mInCallNumberTv.setText(truck);
                } else {
                    mInCallNumberTv.setText(number);
                }
            }
            if (!TextUtils.isEmpty(company)) {
                mInCallCompanyTv.setVisibility(VISIBLE);
                mInCallCompanyTv.setText(company);
            } else {
                mInCallCompanyTv.setVisibility(GONE);
            }
        }
    }

    private String getCallName(String name, String number) {
        if (TextUtils.isEmpty(name)) {
            name = number;
        }
        return name;
    }

    public void setPhoto(String photoUri) {
        Activity context = getActivityFromView(mInCallAvatarCiv);
        if (context == null || context.isDestroyed()) return;
        mInCallAvatarCiv.loadCirclePhotoUrl(photoUri);
    }

    private Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public void setTimeTextHold() {
        mInCallTimeTv.stop();
        setTimerByStart(YlsCallManager.getInstance().getMultipartyHoldStartTime());
        mTvIncallHoldTitle.setVisibility(VISIBLE);
        mInCallTimeTv.setTextColor(getResources().getColor(R.color.red_5));
    }

    public void setMultiCallTimeText(long time) {
        mInCallTimeTv.setVisibility(VISIBLE);
        mInCallTimeTv.stop();
        boolean isNetworkConnected = NetWorkUtil.isNetworkConnected(context);
        if (!isNetworkConnected) {
            mInCallTimeTv.setText(R.string.sip_nonetwork);
        } else {
            setTimerByStart(time);
        }
    }

    public void setEnable(boolean enable) {
        setEnabled(enable);
        if (enable) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }

}
