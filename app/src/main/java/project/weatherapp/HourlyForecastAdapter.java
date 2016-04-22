package project.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sampath on 2/13/2016.
 */
public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    OnItemClickListener mItemClickListener;
    private Context context;
    List<HashMap> dataset;
    final int maxMemory=(int) (Runtime.getRuntime().maxMemory()/1024);
    LruCache<String,Bitmap> imgMemoryCache;

    public HourlyForecastAdapter(Context context, List<HashMap> dataset, LruCache<String, Bitmap> imageMemeorycache) {
        this.context = context;
        this.dataset = dataset;
        this.imgMemoryCache = imageMemeorycache;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public HourlyForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.hourlyforecast_entry, parent, false);

        CardView hourCard = (CardView)view.findViewById(R.id.hourly_card);
        if (hourCard != null) {
            hourCard.setLayoutParams(new CardView.LayoutParams(200, 250));
        }

        return new ViewHolder(view, viewType);
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
        public MyDownloadImageAsyncTask(ImageView icon)
        {
            imageViewReference = new WeakReference<>(icon);
        }

        @Override
        protected  Bitmap doInBackground(String... urls)
        {
            Bitmap bitmap=null;
            for (String url :urls)
            {
                bitmap=DownloadUtility.downloadImage(url);
                if(bitmap!=null) {
                    imgMemoryCache.put(url, bitmap);
                }
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
                //bitmap.recycle(); bitmap = null; System.gc();
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View vView;
        TextView time, temp;
        ImageView icon;

        public ViewHolder(View view, int viewType) {
            super(view);
            vView = view;

            time = (TextView)vView.findViewById(R.id.hourly_time);
            icon = (ImageView)vView.findViewById(R.id.hourly_icon);
            temp = (TextView)vView.findViewById(R.id.hourly_temp);

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
                time.setText(item.get("time").toString());
                String imgUrl = "http://icons.wxug.com/i/c/j/" + item.get("icon").toString() + ".gif";
                new MyDownloadImageAsyncTask(icon).execute(imgUrl);
                temp.setText(item.get("temp_c").toString());
            } else {
                time.setText(item.get("time").toString());
                String imgUrl = "http://icons.wxug.com/i/c/j/" + item.get("icon").toString() + ".gif";
                new MyDownloadImageAsyncTask(icon).execute(imgUrl);
                temp.setText(item.get("temp_f").toString());
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
