package com.yeastar.linkus.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yeastar.linkus.demo.widget.CustomProgressDialog;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.login.YlsLoginManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etLocaleIp;
    private EditText etLocalePort;
    private EditText etRemoteIp;
    private EditText etRemotePort;
    private CustomProgressDialog progressDialog;
    //1030
    //eyJleHBpcmUiOjE3MDU3NTQ4NTUsInNpZ24iOiJtUEloYXVidTlhZlAyTkw0WU5rZXBDcC9PY3JsU1dYWlM1Zi9yWDVyYTZrPSIsInVzZXJuYW1lIjoiMTAzMCIsInZlcnNpb24iOiIxLjAifQ__
    //1031
    //eyJleHBpcmUiOjE3MDU3NTQ4NTUsInNpZ24iOiJ4aVZ0TWR2NlJEVldhclNrQ0RUNldKMXlhWmgyWVVneUJwZGtXSm5VRHZRPSIsInVzZXJuYW1lIjoiMTAzMSIsInZlcnNpb24iOiIxLjAifQ__
    //1032
    //eyJleHBpcmUiOjE3MDU3NTQ4NTUsInNpZ24iOiI5UzFrck9pZXZ5Q3J3V1VaTUt0WG55SG5ZMkdZVXF0bjdVVnJGRHZNZlVjPSIsInVzZXJuYW1lIjoiMTAzMiIsInZlcnNpb24iOiIxLjAifQ__

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etLocaleIp = findViewById(R.id.et_locale_ip);
        etLocalePort = findViewById(R.id.et_locale_port);
        etRemoteIp = findViewById(R.id.et_remote_ip);
        etRemotePort = findViewById(R.id.et_remote_port);
        Button btnLogin = findViewById(R.id.login);
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String userName = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String localeIp = etLocaleIp.getText().toString();
        String localePort = etLocalePort.getText().toString();
        String remoteIp = etRemoteIp.getText().toString();
        String remotePort = etRemotePort.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, R.string.username_empty, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.password_empty, Toast.LENGTH_LONG).show();
            return;
        }
        if ((TextUtils.isEmpty(localeIp) || TextUtils.isEmpty(localePort) || !TextUtils.isDigitsOnly(localePort))
                && (TextUtils.isEmpty(remoteIp) || TextUtils.isEmpty(remotePort) || !TextUtils.isDigitsOnly(remotePort))) {
            Toast.makeText(this, R.string.login_tip_server_ip, Toast.LENGTH_LONG).show();
            return;
        }
        showLargeProgressDialog();
        int localePortI = TextUtils.isEmpty(localePort) ? 8111 : Integer.parseInt(localePort);
        int remotePortI = TextUtils.isEmpty(remotePort) ? 8111 : Integer.parseInt(remotePort);
        YlsLoginManager.getInstance().loginBlock(this, userName, password, localeIp,
                localePortI, remoteIp, remotePortI, new RequestCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        closeProgressDialog();
                        startActivity(new Intent(LoginActivity.this, DialPadActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailed(int code) {
                        closeProgressDialog();
                        String failStr = getString(R.string.login_tip_login_failed,code);
                        Toast.makeText(LoginActivity.this, failStr, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        closeProgressDialog();
                        Toast.makeText(LoginActivity.this, R.string.login_tip_login_exception, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLargeProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(this, CustomProgressDialog.TYPE_TEXT_MULTIPLE, R.string.login_logging, true);
        }
        if (!isDestroyed() && !isFinishing()) {
            progressDialog.show();
        }
    }

    private void closeProgressDialog() {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}