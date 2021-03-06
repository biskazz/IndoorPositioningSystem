package com.nellymincheva.indoorpositioningsystem;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Messenger mBeaconsService = null;
    boolean mBeaconsBound;
    private static int RESULT_LOAD_IMG = 1;
    final int REQUEST_ENABLE_BT=2;
    String imgDecodableString;

    int roomWidthInPixels = 0 ;
    final double[] roomScale = {1};



    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mBeaconsService = new Messenger(service);
            mBeaconsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mBeaconsService = null;
            mBeaconsBound = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        */

        SwitchLanguage("bg");
        setContentView(R.layout.activity_main);


        final RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        final ViewGroup.LayoutParams roomParams = room.getLayoutParams();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final double roomWidth = Float.parseFloat(sharedPreferences.getString("roomWidth", "1"));
        final double roomHeight = Float.parseFloat(sharedPreferences.getString("roomHeight", "1"));
        ViewTreeObserver viewTreeObserver = room.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    room.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    roomWidthInPixels = room.getMeasuredWidth();
                    roomScale[0] = room.getMeasuredWidth() / roomWidth;
                    roomParams.height = (int) (roomHeight * roomScale[0]);
                    DrawBeacons();
                }
            });
        }
        final TextView textView = (TextView) findViewById(R.id.main_activity_text_view);
        final TextView beacon1Info = (TextView) findViewById(R.id.beacon1Info);
        final TextView beacon2Info = (TextView) findViewById(R.id.beacon2Info);
        final TextView beacon3Info = (TextView) findViewById(R.id.beacon3Info);
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userX = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_Y, 0);
                        double distance1 = intent.getDoubleExtra(UserPositionService.EXTRA_DISTANCE_1, 0);
                        double signal1 = intent.getDoubleExtra(UserPositionService.EXTRA_SIGNAL_1, 0);
                        double distance2 = intent.getDoubleExtra(UserPositionService.EXTRA_DISTANCE_2, 0);
                        double signal2 = intent.getDoubleExtra(UserPositionService.EXTRA_SIGNAL_2, 0);
                        double distance3 = intent.getDoubleExtra(UserPositionService.EXTRA_DISTANCE_3, 0);
                        double signal3 = intent.getDoubleExtra(UserPositionService.EXTRA_SIGNAL_3, 0);
                        textView.setText("User position: (" + (double)Math.round(userX*100)/100 + "; " + (double)Math.round(userY*100)/100 + ") ");
                        beacon1Info.setText("Distance: " + (double)Math.round(distance1*100)/100 + "m, Signal: " + signal1 + "dBm");
                        beacon2Info.setText("Distance: " + (double)Math.round(distance2*100)/100 + "m, Signal: " + signal2 + "dBm");
                        beacon3Info.setText("Distance: " + (double)Math.round(distance3*100)/100 + "m, Signal: " + signal3 + "dBm");
                        userImg.setX(((float) userX*(float)roomScale[0]));
                        userImg.setY(((float) userY*(float)roomScale[0]));

                    }
                }, new IntentFilter(UserPositionService.ACTION_USER_POSITION_BROADCAST)
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, UserPositionService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        DrawBeacons();

        //RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        //room.getMeasuredWidth();
       // ViewGroup.LayoutParams params = room.getLayoutParams();
// Changes the height and width to the specified *pixels*
       // params.width = room.getHeight();//(int)((params.width/Math.min(roomHeight,roomWidth)) * Math.max(roomWidth, roomHeight));

    }

    private void DrawBeacons() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final ImageView beacon1Img = (ImageView) findViewById(R.id.beacon1);
        final ImageView beacon2Img = (ImageView) findViewById(R.id.beacon2);
        final ImageView beacon3Img = (ImageView) findViewById(R.id.beacon3);

        beacon1Img.setX(Float.parseFloat(sharedPreferences.getString("beacon1X", "0"))*(float)roomScale[0]);
        beacon1Img.setY(Float.parseFloat(sharedPreferences.getString("beacon1Y", "0"))*(float)roomScale[0]);
        beacon2Img.setX(Float.parseFloat(sharedPreferences.getString("beacon2X", "0"))*(float)roomScale[0]);
        beacon2Img.setY(Float.parseFloat(sharedPreferences.getString("beacon2Y", "0"))*(float)roomScale[0]);
        beacon3Img.setX(Float.parseFloat(sharedPreferences.getString("beacon3X", "0"))*(float)roomScale[0]);
        beacon3Img.setY(Float.parseFloat(sharedPreferences.getString("beacon3Y", "0"))*(float)roomScale[0]);
    }

    @Override
    protected void onResume(){

        if(roomWidthInPixels!=0){

            final RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
            final ViewGroup.LayoutParams roomParams = room.getLayoutParams();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            final double roomWidth = Float.parseFloat(sharedPreferences.getString("roomWidth", "1"));
            final double roomHeight = Float.parseFloat(sharedPreferences.getString("roomHeight", "1"));
            roomScale[0] = roomWidthInPixels / roomWidth;
            roomParams.height = (int) (roomHeight * roomScale[0]);
            DrawBeacons();
        }
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBeaconsBound) {
            unbindService(mConnection);
            mBeaconsBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_change_map:

                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
// Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                return true;
            }
        return true;
    }
    public void loadImageGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e+"Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    private void SwitchLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
