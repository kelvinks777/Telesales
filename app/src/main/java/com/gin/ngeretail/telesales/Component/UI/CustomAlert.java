package com.gin.ngeretail.telesales.Component.UI;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gin.ngeretail.telesales.R;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CustomAlert {
    private AppCompatActivity activity;

    public CustomAlert(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void showAskDialog(String message, DialogInterface.OnClickListener okListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String buttonOK = activity.getResources().getString(R.string.positive_yes);
        String buttonCancel = activity.getResources().getString(R.string.negative_no);
        builder.setCancelable(false)
                .setTitle(activity.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton(buttonOK, okListener)
                .setNegativeButton(buttonCancel, (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showError(String message, final Exception e, DialogInterface.OnClickListener listenerOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String buttonOK = activity.getResources().getString(R.string.positive_ok);
        builder.setCancelable(false)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(buttonOK, listenerOk)
                .setNeutralButton("DETAIL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showInfoDialog("Detil Kesalahan",getStacktrace(e));
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showInfoDialog(String title, String message) {
        showDetailInfoDialog(title, message, null);
    }

    public void showDetailInfoDialog(String title, String message, DialogInterface.OnClickListener listenerOk) {
        final AlertDialog.Builder commonDialogBuilder = new AlertDialog.Builder(activity);
        commonDialogBuilder.setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", listenerOk);
        AlertDialog dialog = commonDialogBuilder.create();
        dialog.show();
    }

    public static String getStacktrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }



    public void showToastMessage(String message){
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void showAskDialogItems(String title, CharSequence[] items, DialogInterface.OnClickListener listenerOk) {
        final AlertDialog.Builder commonDialogBuilder = new AlertDialog.Builder(activity);
        String buttonCancel = "";
        commonDialogBuilder.setCancelable(false)
                .setTitle(title)
                .setIcon(R.drawable.ic_sim_card)
                //.setMessage(message)
                .setItems(items, listenerOk)
                // .setPositiveButton(buttonOK, listenerOk)
                .setNegativeButton(buttonCancel, (dialog, which) -> dialog.cancel());
        AlertDialog dialog = commonDialogBuilder.create();
        dialog.show();
    }

}
