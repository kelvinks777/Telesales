package com.gin.ngeretail.telesales.Component.UI;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomCardView<T> extends CardView {

    protected CardListener cardListener;
    private Context context;
    protected T data;
    protected List<Object> params = new ArrayList<>();

    public CustomCardView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    public CustomCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setParameters(Object... params) {
        if (params != null && params.length > 0)
            this.params = Arrays.asList(params);
    }

    public void setCardListener(CardListener cardListener) {
        this.cardListener = cardListener;
        InitOnClickListener();
    }

    private void InitOnClickListener() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cardListener.onCardClick(getVerticalScrollbarPosition(), v, data);
            }
        });
    }

    public void setActivity(AppCompatActivity activity) {
        this.context = activity;
    }

    public void setData(T data) {
        this.data = data;
    }

    public AppCompatActivity getCardActivity() {
        return (AppCompatActivity) context;
    }

    public Context getCardContext() { return context;}

    public interface CardListener<T> {
        void onCardClick(int position, View view, T data);
    }
}
