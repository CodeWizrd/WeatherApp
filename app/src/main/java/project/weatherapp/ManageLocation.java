package project.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.io.File;
import java.util.HashMap;

public class ManageLocation extends AppCompatActivity implements OnItemClickListener {

    RecyclerView recyclerView;
    LocationAdapter locationAdapter;
    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_manageloc);
        setSupportActionBar(toolbar);
        dir = DisplayWeatherActivity.filesDir;

        final Switch vSwitch = (Switch)findViewById(R.id.switch_location);
        if (vSwitch != null) {
            SharedPreferences sharedPref = DisplayWeatherActivity.sharedPreferences;
            Boolean locationSwitch = sharedPref.getBoolean("follow_me", true);
            Log.d("Location Switch", locationSwitch.toString());
            final LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            vSwitch.setChecked(locationSwitch &&
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            SharedPreferences.Editor editor = DisplayWeatherActivity.sharedPreferences.edit();
            editor.putBoolean("follow_me", vSwitch.isChecked());
            editor.apply();
            vSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final LocationManager locationManager = (LocationManager)
                            getSystemService(Context.LOCATION_SERVICE);
                    if (isChecked && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertForNoGps();
                    }

                    SharedPreferences.Editor editor = DisplayWeatherActivity.sharedPreferences.edit();
                    editor.putBoolean("follow_me", vSwitch.isChecked());
                    editor.apply();
                }
            });
        }

        recyclerView = (RecyclerView)findViewById(R.id.location_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //locationAdapter = new LocationAdapter(this, getFilesDir());
        locationAdapter = new LocationAdapter(this, dir);
        locationAdapter.setOnItemClickListener((OnItemClickListener) this);
        recyclerView.setAdapter(locationAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildDialogForAdd();
                }
            });
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public  void onBackPressed() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.putExtra("Page", getIntent().getIntExtra("PAGE", 0));
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
    }

    public void buildAlertForNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    // To Do:
    // Add Location
    AutoCompleteAdapter autoCompleteAdapter;
    int listSelection;
    void buildDialogForAdd() {
        autoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.autocomplete);

        final android.app.AlertDialog.Builder  builder = new android.app.AlertDialog.Builder
                (this, AlertDialog.THEME_HOLO_DARK  /*THEME_DEVICE_DEFAULT_DARK*/);

        builder.setMessage("Add Location")
                .setCancelable(false);

        final AutoCompleteTextView autoComplete = new AutoCompleteTextView(this);
        autoComplete.setTextColor(getResources().getColor(R.color.colorAccent));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        autoComplete.setLayoutParams(lp);
        autoComplete.setAdapter(autoCompleteAdapter);

        //autoComplete.setAdapter(new AutoCompleteAdapter(mContext, R.layout.autocompletetextview));

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                autoCompleteAdapter.notifyDataSetChanged();
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(r, 500);

        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locationAdapter.notifyDataSetChanged();
                listSelection = position;
            }
        });
        builder.setView(autoComplete);
        builder.setIcon(R.drawable.ic_add_white_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused")
                                final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                //int listSelection = autoComplete.getListSelection();
                Log.d("autocomplete listSel", Integer.toString(listSelection));
                String selection = autoCompleteAdapter.getItem(listSelection);
                int position = autoCompleteAdapter.autocompleteNamesList.indexOf(selection);
                Log.d("autocomplete position", Integer.toString(position));
                locationAdapter.locationData.addLocation(autoCompleteAdapter.autocompleteResultList.get(position),
                        autoCompleteAdapter.autocompleteNamesList.get(position));
                HashMap item = new HashMap();
                item.put("id", autoCompleteAdapter.autocompleteResultList.get(position));
                item.put("title", autoCompleteAdapter.autocompleteNamesList.get(position));
                if (locationAdapter.mDataset.size() == 1 &&
                        locationAdapter.mDataset.get(0).get("id").equals("Zilch"))
                    locationAdapter.mDataset.remove(0);
                locationAdapter.notifyDataSetChanged();
                locationAdapter.mDataset.add(item);
                locationAdapter.notifyDataSetChanged();
                handler.removeCallbacksAndMessages(null);
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        handler.removeCallbacksAndMessages(null);
                        handler.removeCallbacks(r);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v, int position) {

    }
}
