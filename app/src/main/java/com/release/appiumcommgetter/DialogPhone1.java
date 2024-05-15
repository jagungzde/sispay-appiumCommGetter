package com.release.appiumcommgetter;

import static com.release.appiumcommgetter.Global.m_LoginAct;
import static com.release.appiumcommgetter.Global.m_tempPhone1;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class DialogPhone1 extends Dialog {
    Button btnSubmit;
    Button btnSkip;
    EditText etPhone1;

    public DialogPhone1(@NonNull Context context) { super(context); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_phone1);

        etPhone1 = (EditText) findViewById(R.id.etPhone1);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etPhone1.getText().toString().length() > 0) {
                    m_tempPhone1 = etPhone1.getText().toString();
                    dismiss();
                }
                else
                {
                    Toast.makeText(m_LoginAct, "Phone number can not be empty, please insert phone number",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btnSkip.setVisibility(View.GONE);
    }
}
