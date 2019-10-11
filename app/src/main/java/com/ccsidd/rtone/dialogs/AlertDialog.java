package com.ccsidd.rtone.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.AlertDialogListener;

/**
 * Created by dung on 7/17/15.
 */
public class AlertDialog extends Dialog implements View.OnClickListener{

    private String title;
    private boolean isPromptDialog = false;
    private String promptMessage;
    private boolean isShowOK = false;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPromptDialog() {
        return isPromptDialog;
    }

    public void setPromptDialog(boolean isPromptDialog) {
        this.isPromptDialog = isPromptDialog;
    }

    public void setIsShowOK(boolean isShowOK) {
        this.isShowOK = isShowOK;
    }

    public void setAlertDialogListener(AlertDialogListener alertDialogListener) {
        this.alertDialogListener = alertDialogListener;
    }

    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    private String message;
    private AlertDialogListener alertDialogListener;
    private Context context;
    EditText edMessage;

    public AlertDialog(Context context, String title, String message) {
        super(context);
        this.title = title;
        this.message = message;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_dialog);
        TextView tvTitle = (TextView)findViewById(R.id.alert_dialog_tvTitle);
        TextView tvMessage = (TextView)findViewById(R.id.alert_dialog_tvMessage);
        edMessage = (EditText)findViewById(R.id.alert_dialog_edMessage);
        TextView tvYes = (TextView)findViewById(R.id.alert_dialog_btnYes);
        TextView tvNo = (TextView)findViewById(R.id.alert_dialog_btnNo);

        tvTitle.setText(title);
        tvMessage.setText(Html.fromHtml(message));
        tvYes.setTag("Yes");
        tvNo.setTag("No");
        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);

        edMessage.setText(promptMessage);

        if (isPromptDialog)
        {
            tvMessage.setVisibility(View.GONE);
            edMessage.setVisibility(View.VISIBLE);
        }
        else
        {
            tvMessage.setVisibility(View.VISIBLE);
            edMessage.setVisibility(View.GONE);
        }

        if (isShowOK)
        {
            tvNo.setVisibility(View.GONE);
            tvNo.setText("OK");
        }
    }

    @Override
    public void onClick(View view) {
        if (alertDialogListener != null)
            if (view instanceof TextView)
            {
                TextView tv = (TextView)view;
                if (tv.getTag().equals("Yes"))
                {
                    promptMessage = edMessage.getText().toString();
                    alertDialogListener.btnYesPressed(this);
                }
                else
                    alertDialogListener.btnNoPressed(this);
            }
    }
}
