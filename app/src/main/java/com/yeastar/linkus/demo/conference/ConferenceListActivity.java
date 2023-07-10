package com.yeastar.linkus.demo.conference;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseActivity;
import com.yeastar.linkus.demo.conference.detail.ConferenceDetailActivity;

public class ConferenceListActivity extends BaseActivity {

    public ConferenceListActivity() {
        super(R.layout.activity_conference_list);
    }

    @Override
    public void beforeSetView() {
    }

    @Override
    public void findView() {
        ConferenceFragment conferenceFragment = new ConferenceFragment();
        conferenceFragment.setContainerId(R.id.fl_container);
        switchContent(conferenceFragment);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ConferenceListActivity.class);
        context.startActivity(starter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conference_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_conference:
                ConferenceDetailActivity.start(activity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}