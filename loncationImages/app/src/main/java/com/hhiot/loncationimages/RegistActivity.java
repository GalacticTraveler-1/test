package com.hhiot.loncationimages;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegistActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_regist);
//    }


    private Button btnRegister;
    private EditText etAccount,etPass,etPassConfirm;
    private CheckBox cbAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        getSupportActionBar().setTitle("注册");

        etAccount = findViewById(R.id.et_account);
        etPass = findViewById(R.id.et_password);
        etPassConfirm = findViewById(R.id.et_password_confirm);
        cbAgree = findViewById(R.id.cb_agree);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener((View.OnClickListener) this);

    }


    public void onClick(View v) {
        String name = etAccount.getText().toString();
        String pass = etPass.getText().toString();
        String passConfirm = etPassConfirm.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegistActivity.this, "用户名不能为空", Toast.LENGTH_LONG).show();

            return;
        }

        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(RegistActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        if (!TextUtils.equals(pass,passConfirm)) {
            Toast.makeText(RegistActivity.this, "密码不一致", Toast.LENGTH_LONG).show();
            return;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(RegistActivity.this, "请同意用户协议", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(RegistActivity.this, "注册成功！", Toast.LENGTH_LONG).show();

    }

}
