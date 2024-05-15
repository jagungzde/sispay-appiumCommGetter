package com.release.appiumcommgetter;

import static com.release.appiumcommgetter.Global.m_iSubId;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;


public class DialogSMSId extends Dialog {
    Button btnSave;
    Button btnCancel;
    RadioGroup rgSmsId;
    int _iSubId;
    public DialogSMSId(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_smsid);

        _iSubId = m_iSubId;
        switch (m_iSubId){
            case 0:
                RadioButton rb = (RadioButton) findViewById(R.id.rbZero);
                rb.setChecked(true);
                break;
            case 1:
                rb = (RadioButton) findViewById(R.id.rbOne);
                rb.setChecked(true);
                break;
            case 2:
                rb = (RadioButton) findViewById(R.id.rbTwo);
                rb.setChecked(true);
                break;
        }

        // add any necessary listeners or callbacks here
        rgSmsId = (RadioGroup) findViewById(R.id.rbgSimId);
        rgSmsId.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rbChecked = findViewById(i);
                _iSubId = Integer.valueOf(rbChecked.getText().toString());

            }
        });

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getContext().getApplicationContext().getSharedPreferences(Global.TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("SUBSCRIPTION_ID", _iSubId);
                editor.commit();
                m_iSubId = _iSubId;
                Toast.makeText(getContext().getApplicationContext(), "Use SMS ID = " + _iSubId + ", to retrieve SMS messages from the SIM card",
                        Toast.LENGTH_LONG).show();
                dismiss();
            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }
}
