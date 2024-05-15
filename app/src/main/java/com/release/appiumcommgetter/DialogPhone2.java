package com.release.appiumcommgetter;

import static com.release.appiumcommgetter.Global.m_tempPhone2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class DialogPhone2 extends Dialog {
    Button btnSubmit;
    Button btnSkip;
    EditText etPhone2;

    public DialogPhone2(@NonNull Context context) { super(context); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_phone2);

        etPhone2 = (EditText) findViewById(R.id.etPhone2);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPhone2.getText().toString().length() > 0)
                    m_tempPhone2 = etPhone2.getText().toString();
                dismiss();
            }
        });

        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_tempPhone2 = "NOT USED";
                dismiss();
            }
        });

        btnSkip.setVisibility(View.GONE);
    }
}
