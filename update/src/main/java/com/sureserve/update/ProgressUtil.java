package com.sureserve.update;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.sureserve.update.R;

/**
 * Created by kylexy on 2017-02-10.
 */

public class ProgressUtil {
    AlertDialog mAlterDialog;
    Context mContext;
    NumberProgressBar numberProgressBar;

    public ProgressUtil(Context context) {
        this.mContext = context;
    }

    private void initDialog(String title, String message, View contentView, String positiveBtnText, String negativeBtnText, DialogInterface.OnClickListener positiveCallback, DialogInterface.OnClickListener negativeCallback, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.PAppCompatAlertDialogStyle);
        builder.setTitle(title == null ? "提示" : title);
        if (message != null) {
            builder.setMessage(message);
        }
        if (contentView != null) {
            builder.setView(contentView);
        }
        if (positiveBtnText != null) {
            builder.setPositiveButton(positiveBtnText, positiveCallback);
        }
        if (negativeBtnText != null) {
            builder.setNegativeButton(negativeBtnText, negativeCallback);
        }
        builder.setCancelable(cancelable);
        mAlterDialog = builder.create();
    }

    //普通对话框
    public void showSimpleDialog(String title, String message, String positiveBtnText, String negativeBtnText, DialogInterface.OnClickListener positiveCallback, DialogInterface.OnClickListener negativeCallback, boolean cancelable) {
        initDialog(title, message, null, positiveBtnText, negativeBtnText, positiveCallback, negativeCallback, cancelable);
        show();
    }

    //带ProgressBar的对话框
    public void showProgressDialog(String title, String message, String positiveBtnText, String negativeBtnText, DialogInterface.OnClickListener positiveCallback, DialogInterface.OnClickListener negativeCallback, boolean cancelable) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_update_dialog, null);
        if (message != null) {
            final TextView messageTv = (TextView) view.findViewById(R.id.progressbar_msg);
            messageTv.setText(message);
        }

        numberProgressBar = (NumberProgressBar) view.findViewById(R.id.progressbar_number);
        numberProgressBar.setVisibility(View.GONE);

        initDialog(title, null, view, positiveBtnText, negativeBtnText, positiveCallback, negativeCallback, cancelable);
    }

    public boolean isShowing() {
        if (mAlterDialog == null)
            return false;
        else
            return mAlterDialog.isShowing();
    }

    public void dismiss() {
        if (mAlterDialog != null) mAlterDialog.dismiss();
    }

    public void show() {
        if (mAlterDialog != null) mAlterDialog.show();
    }

    public void setProgress(int progress) {

        if (mAlterDialog != null && numberProgressBar != null) {
            numberProgressBar.setVisibility(View.VISIBLE);
            numberProgressBar.setMax(100);
            numberProgressBar.setProgress(progress);
        }
    }

    public AlertDialog getAlterDialog() {
        return mAlterDialog;
    }
}
