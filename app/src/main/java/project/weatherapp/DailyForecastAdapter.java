package project.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 2/12/2016.
 */
public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {
    OnItemClickListener mItemClickListener;
    private Context context;
    List<HashMap> dataset;
    final int maxMemory=(int) (Runtime.getRuntime().maxMemory()/1024);
    LruCache<String,Bitmap> imgMemoryCache;

    //Context context, JSONObject dataSource, LruCache<String, Bitmap> imageMemeorycache
    public DailyForecastAdapter(Context context, List<HashMap> dataset, LruCache<String, Bitmap> imageMemeorycache) {
        this.context = context;
        this.dataset = dataset;
        this.imgMemoryCache = imageMemeorycache;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public DailyForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.dailyforecast_row, parent, false);

        ViewHolder vh = new ViewHolder(view, viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap item = dataset.get(position);
        holder.bindData(item);
    }

    @Override
    public int getItemCount() {
        if (dataset != null)
            return dataset.size();
        else return 0;
    }

    private  class MyDownloadImageAsyncTask extends AsyncTask<String,Void,Bitmap>
    {

        private  final WeakReference<ImageView> imageViewReference;
        public MyDownloadImageAsyncTask(ImageView imv)
        {

            imageViewReference = new WeakReference<ImageView>(imv);
        }
        @Override
        protected  Bitmap doInBackground(String... urls)
        {
            Bitmap bitmap=null;
            for (String url :urls)
            {
                bitmap=DownloadUtility.downloadImage(url);
                if(bitmap!=null)
                    imgMemoryCache.put(url,bitmap);

            }
            return  bitmap;

        }

        @Override
        protected  void  onPostExecute(Bitmap bitmap)
        {
            if(imageViewReference != null && bitmap != null)
            {
                final  ImageView imageView=imageViewReference.get();
                if(imageView!=null)
                {
                    imageView.setImageBitmap(bitmap);
                }
            }
            //bitmap.recycle(); bitmap = null; System.gc();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View vView;
        TextView weekday, high, low;
        ImageView icon;


        public ViewHolder(View view, int viewType) {
            super(view);
            vView = view;
            weekday = (TextView)vView.findViewById(R.id.daily_weekday);
            icon = (ImageView)vView.findViewById(R.id.daily_icon);
            high = (TextView)vView.findViewById(R.id.daily_high);
            low = (TextView)vView.findViewById(R.id.daily_low);
            //TO DO:


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

        public void bindData(HashMap item) {
            if (DisplayWeatherActivity.metric) {
                weekday.setText(item.get("weekday").toString());
                String imgUrl = "http://icons.wxug.com/i/c/j/" + item.get("icon").toString() + ".gif";
                new MyDownloadImageAsyncTask(icon).execute(imgUrl);
                high.setText(item.get("high_c").toString());
                low.setText(item.get("low_c").toString());
            } else {
                weekday.setText(item.get("weekday").toString());
                String imgUrl = "http://icons.wxug.com/i/c/j/" + item.get("icon").toString() + ".gif";
                new MyDownloadImageAsyncTask(icon).execute(imgUrl);
                high.setText(item.get("high_f").toString());
                low.setText(item.get("low_f").toString());
            }
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


