package com.yeastar.linkus.demo.call.Audio;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.widget.VerticalRecyclerView;
import com.yeastar.linkus.utils.SoundManager;

import java.util.ArrayList;
import java.util.List;

public class AudioPopupView extends CenterPopupView {

    private PopupSelect popupSelect;
    private List<AudioVo> audioVoList;


    public AudioPopupView(@NonNull Context context, List<String> names, int[] iconResIds, int[] val, PopupSelect popupSelect) {
        super(context);
        this.popupSelect = popupSelect;
        audioVoList = new ArrayList<>();
        int lens = names.size();
        for (int i = 0; i < lens; i++) {
            audioVoList.add(new AudioVo(names.get(i), iconResIds[i], val[i] == SoundManager.getInstance().getAudioRoute(), i == lens - 1, val[i]));
        }
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.layout_popup_sound;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        VerticalRecyclerView recyclerView = findViewById(R.id.rv_sound);
        AudioAdapter adapter = new AudioAdapter(audioVoList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> dismissWith(() -> {
            if (popupSelect != null) {
                popupSelect.onItemClick(position);
            }
        }));
    }
}
