package project.weatherapp;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Sampath on 3/2/2016.
 * To do: use FragmentStatePagerAdapter
 */
public class CityPagerAdapter extends FragmentPagerAdapter implements
        ViewPager.OnPageChangeListener {

    private int count;
    FragmentManager fragmentManager;
    ViewPager viewPager;
    static Toolbar toolbar;
    static String curntLoc = "";
    private static WeatherActivityFragment curFrag;

    public CityPagerAdapter (FragmentManager fm, ViewPager viewPager, Toolbar toolbar, File dir) {
        super(fm);
        fragmentManager = fm;

        count = LocationData.cities.length;
        this.viewPager = viewPager;
        CityPagerAdapter.toolbar = toolbar;
        viewPager.setAdapter(this);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = WeatherActivityFragment.newInstance(position);
        //id[position] = fragment.getId();
        if (LocationData.cities[position].current) {
            curFrag = (WeatherActivityFragment)fragment;
        }
        return fragment;
    }

    @Override
    public int getCount() { return count; }


    @Override
    public int getItemPosition(Object object) {

        WeatherActivityFragment fragment = (WeatherActivityFragment)object;
        if (fragment != null) {
            fragment.update();
        }
        return super.getItemPosition(object);

        //return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle (int position) {
        Locale l = Locale.getDefault();
        String name = LocationData.cities[position].name;
        return name.toUpperCase(l);
    }

    public void refreshLocation() {
        if (curFrag != null)
        curFrag.refreshLocation();
    }

    void reloadData() {
        count = LocationData.cities.length;
        WeatherActivityFragment fragment = (WeatherActivityFragment)instantiateItem(viewPager, viewPager.getCurrentItem());
        fragment.datasetChanged();
        fragment.setPageScrollers(fragment.getView());
        notifyDataSetChanged();
    }

    public void scrollLeft() {
        if (viewPager.getCurrentItem() > 0)
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public void scrollRight() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public static void setTitle(int i) {
        String title = LocationData.cities[i].name;
        Locale l = Locale.getDefault();
        Log.d("set title ", title);
        if (title != null &&  toolbar != null)
            toolbar.setTitle(title.toUpperCase(l));
    }

    // ViewPager Listener
    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        String title = getPageTitle(i).toString();
        Log.d("onpageselected", title);
        if (title != null &&  toolbar != null)
            toolbar.setTitle(title);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
    // End of ViewPager listener


    public interface Updateable {
        void update();
    }

}
