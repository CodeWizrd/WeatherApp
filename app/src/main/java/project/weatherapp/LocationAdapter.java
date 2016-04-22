package project.weatherapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 3/17/2016.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    LocationData locationData;
    private final Context mContext;
    List<Map<String, ?>> mDataset;

    public LocationAdapter (Context context, File dir) {
        this.mContext = context;
        locationData = new LocationData(dir);
        mDataset = locationData.getLocationList();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, ?> item = mDataset.get(position);
        holder.bindData(item, position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon = null;
        private TextView title = null;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView)view.findViewById(R.id.icon_location);
            title = (TextView)view.findViewById(R.id.title_location);
        }

        public void bindData(final Map<String, ?> item, final int position) {

            if (mDataset.size() == 1 && item.get("id").equals("Zilch")) {
                if (title != null) {
                    title.setText((String) item.get("title"));
                }
                if (icon != null) {
                    icon.setVisibility(View.INVISIBLE);
                }
            }
            else {
                if (title != null) {
                    title.setText((String) item.get("title"));
                }
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDataset.remove(position);
                            LocationData.removeLocation((String) item.get("id"));
                            if (mDataset.size() == 0)
                                mDataset = locationData.getLocationList();
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }

    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        //this.mItemClickListener = itemClickListener;
    }
}
