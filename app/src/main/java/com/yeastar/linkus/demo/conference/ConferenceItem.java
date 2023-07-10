package com.yeastar.linkus.demo.conference;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.TimeUtil;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.service.login.YlsLoginManager;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import kale.adapter.item.AdapterItem;

/**
 * Created by ted on 17-4-20.
 */

public class ConferenceItem implements AdapterItem<ConferenceVo> {

    private Context context;

    @Override
    public int getLayoutResId() {
        return R.layout.item_conference_combine;
    }

    private TextView confName;
    private TextView confMemberCount;
    private TextView statusAndTime;
    private ImageView imageView;
    private View bottomLine;

    @Override
    public void bindViews(@NonNull View view) {
        context = view.getContext();
        confName = view.findViewById(R.id.conference_name_tv);
        confMemberCount = view.findViewById(R.id.conference_count_tv);
        statusAndTime = view.findViewById(R.id.conference_time_tv);
        imageView = view.findViewById(R.id.conference_status_iv);
        bottomLine = view.findViewById(R.id.bottom_line);
    }

    @Override
    public void setViews() {
    }

    @Override
    public void handleData(ConferenceVo conferenceModel, int i) {
        List<ConferenceVo> list = YlsConferenceManager.getInstance().getConferenceList();
        if (Objects.equals(conferenceModel.getAdmin(), YlsLoginManager.getInstance().getMyExtension())) {
            imageView.setImageResource(R.mipmap.ic_callout);
        } else {
            imageView.setImageResource(R.mipmap.ic_callin);
        }
        String updateTime = conferenceModel.getUpdateTime();
        if (updateTime.startsWith("1")) {
            long aLong = Long.parseLong(conferenceModel.getUpdateTime());
            Date date = new Date(aLong);
            updateTime = TimeUtil.getSimpleDateStr(context, date);
        }
        statusAndTime.setText(updateTime);
        confName.setText(conferenceModel.getName());
        int count = conferenceModel.getMemberList().size();
        String s = context.getString(R.string.conference_list_member, count);
        String countStr = String.format("(%s)", s);
        confMemberCount.setText(countStr);

        if (i == list.size() - 1) {
            bottomLine.setVisibility(View.INVISIBLE);
        } else {
            bottomLine.setVisibility(View.VISIBLE);
        }
    }

}
