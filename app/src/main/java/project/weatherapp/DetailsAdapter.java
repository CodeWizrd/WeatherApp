package project.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sampath on 2/14/2016.
 */
public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {
    OnItemClickListener mItemClickListener;
    private Context context;
    List<Pair> dataset;

    public DetailsAdapter(Context context, List<Pair> dataset, LruCache<String, Bitmap> imageMemeorycache) {
        this.context = context;
        this.dataset = dataset;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (dataset != null)
            return dataset.size();
        else return 0;
    }

    public DetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.details_entry, parent, false);

        CardView curCard = (CardView)view.findViewById(R.id.details_card);
        if (curCard != null) {
            curCard.setLayoutParams(new CardView.LayoutParams(200, 240));
        }

        ViewHolder vh = new ViewHolder(view, viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair item = dataset.get(position);
        holder.bindData(item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView category, value;

        public ViewHolder(View v, int viewType) {
            super(v);
            view = v;

            category = (TextView)v.findViewById(R.id.details_category);
            value = (TextView)v.findViewById(R.id.details_value);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.OnItemClick(v, getLayoutPosition());
                    }
                    notifyDataSetChanged();
                }
            });
        }

        public void bindData(Pair item) {
            category.setText(item.first.toString());
            value.setText(item.second.toString());
        }
    }


    public interface OnItemClickListener {
        public void OnItemClick(View v, int position);
        //public void OnItemLongClick(View v, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }
}
