package com.gin.ngeretail.telesales.Component.UI;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class CustomRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LayoutInflater inflater;
    public int resource;
    private List<T> items;
    private CustomCardView.CardListener<T> recyclerClickListener;
    private AppCompatActivity activity;
    private Object[] params;

    public CustomRecyclerViewAdapter(AppCompatActivity activity, int resource, List<T> items, Object... params) {
        this.inflater = LayoutInflater.from(activity);
        this.items = items;
        this.resource = resource;
        this.activity = activity;
        this.params = params;
    }

    public void setParameters(Object... params) {
        this.params = params;
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        private CustomCardView<T> customCardView;

        public RecyclerViewHolders(View cardView) {
            super(cardView);
            customCardView = (CustomCardView<T>) cardView;
            if (recyclerClickListener != null) {
                customCardView.setCardListener(new CustomCardView.CardListener<T>() {
                    @Override
                    public void onCardClick(int position, View view, T data) {
                        recyclerClickListener.onCardClick(getAdapterPosition(), view, data);
                    }
                });
            }
        }

        public void setData(T data) {
            customCardView.setData(data);
        }
    }

    public T GetItem(int position) {
        return items.get(position);
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CustomCardView<T> cardView = (CustomCardView<T>) inflater.inflate(this.resource, parent, false);
        cardView.setParameters(params);
        return new RecyclerViewHolders(cardView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerViewHolders recyclerViewHolders = (RecyclerViewHolders) holder;
        recyclerViewHolders.setData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public void setRecyclerListener(CustomCardView.CardListener recyclerClickListener) {
        this.recyclerClickListener = recyclerClickListener;
    }

    public void updateSource(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
