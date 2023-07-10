package com.yeastar.linkus.demo.conference;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseFragment;
import com.yeastar.linkus.demo.conference.detail.ConferenceDetailActivity;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

/**
 * Created by root on 16-12-28.
 */
@SuppressWarnings("unchecked")
public class ConferenceFragment extends BaseFragment {

    private ListView listView = null;
    private CommonAdapter adapter;
    private List<ConferenceVo> conferenceModelList = new ArrayList<>();


    public ConferenceFragment() {
        super(R.layout.fragment_conference);
    }

    @Override
    public void findView(View parent) {
        listView = parent.findViewById(R.id.conference_list);
        conferenceModelList = YlsConferenceManager.getInstance().getConferenceList();
        adapter = new CommonAdapter<ConferenceVo>(conferenceModelList, 1) {
            @NonNull
            @Override
            public AdapterItem createItem(Object o) {
                return new ConferenceItem();
            }
        };
        listView.setAdapter(adapter);
        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConferenceStatus();
    }

    public void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ConferenceVo conferenceModel = conferenceModelList.get(position);
                Intent intent = new Intent(activity, ConferenceDetailActivity.class);
                intent.putExtra(Constant.EXTRA_CONFERENCE, conferenceModel);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });
    }

    private void showDeleteDialog(int position) {
        final String[] items = {getString(R.string.public_delete)};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(activity);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id = conferenceModelList.get(position).getConferenceId();
                if (TextUtils.isEmpty(id)) {
                    return;
                }
                YlsConferenceManager.getInstance().deleteConferenceLog(id);
                conferenceModelList = YlsConferenceManager.getInstance().getConferenceList();
                adapter.setData(conferenceModelList);
                adapter.notifyDataSetChanged();
            }
        });
        listDialog.show();
    }

    private void updateConferenceStatus() {
        conferenceModelList = YlsConferenceManager.getInstance().getConferenceList();
        if (CommonUtil.isListNotEmpty(conferenceModelList)) {
            adapter.setData(conferenceModelList);
            adapter.notifyDataSetChanged();
        }
    }

}
