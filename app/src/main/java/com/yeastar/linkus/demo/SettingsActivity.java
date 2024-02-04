package com.yeastar.linkus.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.utils.SPUtil;

import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // 初始化这个ListPreference对象
            ListPreference lp = (ListPreference) findPreference("list_preference_codec");
            // 设置获取ListPreference中发生的变化
            lp.setOnPreferenceChangeListener(this);
            /**让ListPreference中的摘要内容（即summary）显示为当前ListPreference中的实体对应的值
             * 这个方法的作用是为了当下一次打开这个程序时会显示上一次的设置的summary(摘要)
             * 如果没有添加这个方法，当再次打开这个程序时，它将不会显示上一次程序设置的值，而
             * 是显示默认值*/
            lp.setSummary(lp.getEntry());
            String codec = (String) SPUtil.getParam(getContext(), SPUtil.CODEC, "ilbc");
            String[] valArray = getResources().getStringArray(R.array.entries_values_str);
            int index = Arrays.asList(valArray).indexOf(codec);
            String[] nameArray = getResources().getStringArray(R.array.entries_str);
            lp.setSummary(nameArray[index]);
            lp.setValueIndex(index);

            SwitchPreferenceCompat  agcASwitchPreference = findPreference("switch_preference_agc");
            boolean agc = (boolean) SPUtil.getParam(getContext(), SPUtil.SETTING_AGC, true);
            Log.i("Setting","agcASwitchPreference agc="+agc);
            agcASwitchPreference.setChecked(agc);
            agcASwitchPreference.setOnPreferenceChangeListener(this);

            SwitchPreferenceCompat  ecASwitchPreference = findPreference("switch_preference_ec");
            boolean ec = (boolean) SPUtil.getParam(getContext(), SPUtil.SETTING_EC, true);
            ecASwitchPreference.setChecked(ec);
            ecASwitchPreference.setOnPreferenceChangeListener(this);

            SwitchPreferenceCompat  ncASwitchPreference = findPreference("switch_preference_nc");
            boolean nc = (boolean) SPUtil.getParam(getContext(), SPUtil.SETTING_NC, true);
            ncASwitchPreference.setChecked(nc);
            ncASwitchPreference.setOnPreferenceChangeListener(this);

        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                // 获取ListPreference中的实体内容
                CharSequence[] entries = listPreference.getEntries();

                // 获取ListPreference中的实体内容的下标值
                int index = listPreference.findIndexOfValue((String) newValue);
                YlsCallManager.getInstance().setCodec(getContext(), (String) newValue);
                // 把listPreference中的摘要显示为当前ListPreference的实体内容中选择的那个项目
                listPreference.setSummary(entries[index]);
            }
            if (preference instanceof SwitchPreferenceCompat) {
                SwitchPreferenceCompat switchPreferenceCompat = (SwitchPreferenceCompat) preference;
                if (switchPreferenceCompat.getKey().equals("switch_preference_agc")) {
                    YlsBaseManager.getInstance().agcSetting(getContext(), (boolean) newValue);
                } else if (switchPreferenceCompat.getKey().equals("switch_preference_ec")) {
                    YlsBaseManager.getInstance().echoSetting(getContext(), (boolean) newValue);
                } else if (switchPreferenceCompat.getKey().equals("switch_preference_nc")) {
                    YlsBaseManager.getInstance().ncSetting(getContext(), (boolean) newValue);
                }
                switchPreferenceCompat.setChecked((boolean) newValue);
            }

            return false;
        }
    }
}