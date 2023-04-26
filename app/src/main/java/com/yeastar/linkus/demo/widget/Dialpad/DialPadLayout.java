package com.yeastar.linkus.demo.widget.Dialpad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.yeastar.linkus.demo.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

/**
 * Created by ted on 17-4-13.
 * 通用拨号键盘
 */
public class DialPadLayout extends LinearLayout {

    private AppCompatEditText mTabDialInputTv;
    private ImageView mTabDialDeleteIv;
    private List<DialPadModel> list = new ArrayList<>();

    private static final int[] SOUND =
            {ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_2, ToneGenerator.TONE_DTMF_3,
                    ToneGenerator.TONE_DTMF_4, ToneGenerator.TONE_DTMF_5, ToneGenerator.TONE_DTMF_6,
                    ToneGenerator.TONE_DTMF_7, ToneGenerator.TONE_DTMF_8, ToneGenerator.TONE_DTMF_9,
                    ToneGenerator.TONE_DTMF_S, ToneGenerator.TONE_DTMF_0, ToneGenerator.TONE_DTMF_P};
    private static final String[] DIALPAD_MAIN_ARRAY =
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "﹡", "0", "#"};
    private static final String[] DIALPAD_SUB_TEXT_ARRAY =
            {null, "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ", null, "+", null};
    private ToneGenerator mToneGenerator;
    private DialPadCallBack dialPadCallBack;
    private DialNumberCallBack dialNumberCallBack;
    private boolean isTransparent = false;
    private boolean showDel = true;
    private boolean paste = false;

    public DialPadLayout(Context context) {
        super(context);
        init(context);
    }

    public DialPadLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttribute(context, attrs);
        init(context);

    }

    public DialPadLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context, attrs);
        init(context);
    }

    public void setDialPadCallBack(DialPadCallBack dialPadCallBack) {
        this.dialPadCallBack = dialPadCallBack;
    }

    public void setDialNumberCallBack(DialNumberCallBack dialNumberCallBack) {
        this.dialNumberCallBack = dialNumberCallBack;
    }

    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DialPadLayout);
        isTransparent = ta.getBoolean(R.styleable.DialPadLayout_transparent, false);
        showDel = ta.getBoolean(R.styleable.DialPadLayout_showDel, true);
        paste = ta.getBoolean(R.styleable.DialPadLayout_paste, true);
        ta.recycle();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(final Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        initData();
        View convertView;
        if (isTransparent) {
            convertView = inflater.inflate(R.layout.layout_dialpad_black, this);
        } else {
            convertView = inflater.inflate(R.layout.layout_dialpad, this);
        }
        mTabDialInputTv = convertView.findViewById(R.id.tab_dial_input_tv);
        if (paste) {
            mTabDialInputTv.setEnabled(true);
        } else {
            mTabDialInputTv.setEnabled(false);
        }
        mTabDialDeleteIv = convertView.findViewById(R.id.tab_dial_delete_iv);

        mTabDialInputTv.setOnTouchListener((v, event) -> {
            mTabDialInputTv.setCursorVisible(true);
            closeKeyboard(mTabDialInputTv);
            return false;
        });

        mTabDialInputTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mTabDialDeleteIv.setVisibility(INVISIBLE);
                    mTabDialInputTv.setTextSize(17);
                } else {
                    mTabDialDeleteIv.setVisibility(showDel ? VISIBLE : INVISIBLE);
                    mTabDialInputTv.setTextSize(26);
                }
                if (dialPadCallBack != null) {
                    dialPadCallBack.onDialNumber(getInputNumber());
                }
                if (dialNumberCallBack != null && count > 0 && before < count) {
                    dialNumberCallBack.onTextChanged(s.charAt(s.length() - 1) + "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mTabDialDeleteIv.setOnClickListener(v -> {
            String number = getInputNumber();
            if (!TextUtils.isEmpty(number)) {
                int index = mTabDialInputTv.getSelectionStart();
                if (index != 0) {//光标在最前的时候不做删除
                    if (textNotNull()) {
                        getText().delete(index - 1, index);
                    }
                    if (index == 1 && number.length() == 1) {
                        mTabDialInputTv.setCursorVisible(false);
                    } else if (index > number.length()) {
                        mTabDialInputTv.setCursorVisible(false);
                    }
                }
            }
        });

        mTabDialDeleteIv.setOnLongClickListener(v -> {
            if (textNotNull()) {
                getText().clear();
            }
            mTabDialInputTv.setCursorVisible(false);
            return false;
        });

        GridView mTabDialDialPadGv = convertView.findViewById(R.id.tab_dial_dialpad_gv);
        CommonAdapter adapter = new CommonAdapter<DialPadModel>(list, 1) {
            @NonNull
            @Override
            public AdapterItem createItem(Object o) {
                if (isTransparent) {
                    return new DialPadBlackItem(new DialCallBack() {
                        @Override
                        public void onItemClick(int position, String callNumber) {
                            insertNumber(context, position, callNumber);
                        }

                        @Override
                        public void onItemKLongClick(int position, String callNumber) {
                            if (position == 10) {
                                callNumber = "+";
                            }
                            insertNumber(context, position, callNumber);
                        }
                    });
                } else {
                return new DialPadItem(new DialCallBack() {
                    @Override
                    public void onItemClick(int position, String callNumber) {
                        insertNumber(context, position, callNumber);
                    }

                    @Override
                    public void onItemKLongClick(int position, String callNumber) {
                        if (position == 10) {
                            callNumber = "+";
                        }
                        insertNumber(context, position, callNumber);
                    }
                });
                }
            }
        };
        mTabDialDialPadGv.setAdapter(adapter);
    }

    private Editable getText() {
        return mTabDialInputTv.getText();
    }

    private boolean textNotNull() {
        return mTabDialInputTv.getText() != null;
    }

    private void insertNumber(Context context, int position, String callNumber) {
        int index = mTabDialInputTv.getSelectionStart();
        if (textNotNull()) {
            if (index > getText().length()) {
                mTabDialInputTv.setCursorVisible(false);
            }
            getText().insert(index, callNumber);
        }
        playSound(context, position);
    }

    private void closeKeyboard(EditText editText) {
        Class<EditText> cls = EditText.class;
        Method method;
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(editText, false);
        } catch (Exception e) {
            // handle exception
        }
    }

    private void initData() {
        list.clear();
        for (int i = 0; i < 12; i++) {
            DialPadModel dialpadModel = new DialPadModel(DIALPAD_MAIN_ARRAY[i], DIALPAD_SUB_TEXT_ARRAY[i]);
            list.add(dialpadModel);
        }
    }

    private void playSound(Context context, int i) {
        if (mToneGenerator == null) {
            try {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_DTMF);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
                int volume = currVolume * 100 / maxVolume;
                mToneGenerator = new ToneGenerator(
                        AudioManager.STREAM_DTMF, volume);
            } catch (Exception e) {
                e.printStackTrace();
                mToneGenerator = null;
            }
        }
        if (mToneGenerator != null) {
            mToneGenerator.startTone(SOUND[i], 150); // 发声
        }
    }

    public void mediaPlayRelease() {
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
            mToneGenerator.release();
            mToneGenerator = null;
        }
    }

    public String getInputNumber() {
        if (mTabDialInputTv != null && textNotNull()) {
            return getText().toString().trim();
        }
        return "";
    }

    public void setInputNumber(String number) {
        mTabDialInputTv.setText(number);
        mTabDialInputTv.setSelection(number.length());
        mTabDialInputTv.setCursorVisible(false);
    }

    public AppCompatEditText getTabDialInputTv() {
        return mTabDialInputTv;
    }


    public interface DialPadCallBack {
        void onDialNumber(String number);
    }

    public interface DialCallBack {
        void onItemClick(int position, String callNumber);

        void onItemKLongClick(int position, String callNumber);
    }

    public interface DialNumberCallBack {
        void onTextChanged(String number);
    }

}
