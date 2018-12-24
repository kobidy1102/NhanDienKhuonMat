package com.example.pc_asus.nguoimu;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;


public class VideoCallViewActivity extends AppCompatActivity implements  TextToSpeech.OnInitListener,LocationListener,SensorEventListener {
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    String uid;

    String idSelected;
    TextToSpeech tts;
    private final int REQ_CODE_SPEECH_INPUT = 100;
   // private LocationListener mLocationListener;
   LocationManager locationManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    double longitude, latitude;
    float direction;


    private static final String LOG_TAG = VideoCallViewActivity.class.getSimpleName();

    ArrayList<String> arrListTNV = new ArrayList<String>();
     ArrayList<String> arrTNVFreeTime = new ArrayList<String>();
     ArrayList<String> arrListFriends = new ArrayList<String>();
    ArrayList<String> arrFriendsFreeTime = new ArrayList<String>();
    ArrayList<String> arrListFriendsNeverChange = new ArrayList<String>();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final int PERMISSION_REQ_ID_LOCATION = 25;
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }
    };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_video_call_view);
            tts= new TextToSpeech(this, this);

            mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
            uid= mCurrentUser.getUid();
            mDatabase= FirebaseDatabase.getInstance().getReference();

            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
               // tts.speak("đang kết nối, vui lòng chờ", TextToSpeech.QUEUE_FLUSH,null);
                initAgoraEngineAndJoinChannel();
                mRtcEngine.switchCamera();
                getListFriend(uid);
            }

            TextView tv_endCall= findViewById(R.id.tv_endCallActivity);
            tv_endCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").setValue(0);
                    finish();
                }
            });

            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQ_ID_LOCATION)) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
            }
                mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);



        }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        // setupLocalVideo();           // Tutorial Step 3
        joinChannel();               // Tutorial Step 4

    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(android.Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + android.Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + android.Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_LOCATION:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
                } else {
                    showLongToast("No permission for " + Manifest.permission.ACCESS_FINE_LOCATION);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

            leaveChannel();
            RtcEngine.destroy();                  //chưa gọi nên khi thoát màn hình nó chưa có đối tượng
            mRtcEngine = null;

        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
        locationManager.removeUpdates(this);

    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_480P_10, false);
    }


    // Tutorial Step 4
    private void joinChannel() {
        mRtcEngine.joinChannel(null, uid, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        surfaceView.setTag(uid); // for mark purpose
    }

    // Tutorial Step 6
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
    }

    @Override
    public void onInit(int i) {
        if(i !=TextToSpeech.ERROR) {

            Locale l = new Locale("vi");
            tts.setLanguage(l);

        }
    }







    // Bạn bè

    private void getListFriend(String uid){

        arrListFriends.clear();
        mDatabase.child("NguoiMu").child("Friends").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    arrListFriends.add(dataSnapshot.getKey());
                arrListFriendsNeverChange.add(dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mDatabase.child("NguoiMu").child("Friends").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    getStatusOfFriends(arrListFriends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }




    private void getStatusOfFriends(final ArrayList<String> arr){


        arrFriendsFreeTime.clear();
        for (int i = 0; i < arr.size(); i++) {

            Log.e("arr", "friends=" + arr.get(i));

            mDatabase.child("TinhNguyenVien").child("Status").child(arr.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        String s0 = dataSnapshot.child("statusWithFriends").getValue().toString();
                        //Log.e("ebc","status with friends:"+s0);
                        int status = Integer.parseInt(s0);
                        String s1 = dataSnapshot.child("connectionRequest").getValue().toString();
                        int status1;
                      try {
                           status1 = Integer.parseInt(s1);
                      }catch (Exception e){
                          status1=7;
                      }
                        if ((status == 1) && (status1==0 || status1==1)) {
                            arrFriendsFreeTime.add(dataSnapshot.getKey());
                            Log.e("arr","bạn đang rãnh: "+ dataSnapshot.getKey());

                        }

                        if (dataSnapshot.getKey().equalsIgnoreCase(arr.get(arr.size()-1))) {
                            //chọn dc bbe đang rãnh
                            if(arrFriendsFreeTime.size()!=0) {
                                Random rd = new Random();
                                int number = rd.nextInt(arrFriendsFreeTime.size());
                                idSelected = arrFriendsFreeTime.get(number);
                                Toast.makeText(VideoCallViewActivity.this, idSelected, Toast.LENGTH_SHORT).show();
                                mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").setValue(uid);

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkStatusOfDevice();

                                    }
                                }, 1000);


                                // nếu TNV ko bắt máy thì sẽ kết nối lại vs TNV  random

                                final boolean[] check = {true};
                                mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").addValueEventListener(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(check[0] ==true) {
                                            String disconnect = dataSnapshot.getValue().toString();

                                            if (disconnect.equals("1") && arrFriendsFreeTime.size() > 1) {        // bạn bè k bắt máy -> bận,, list bạn phải có 2 người trở lên thì mới kết nối lại tới bạn
                                                //  tts.speak("dang kết nối lại", TextToSpeech.QUEUE_FLUSH,null);
                                                Log.e("arr", "kết nối lại với bạn bè " + arrFriendsFreeTime.size());
                                                arrListFriends.remove(idSelected);
                                                getStatusOfFriends(arrListFriends);
                                                arrFriendsFreeTime.clear();
                                                check[0] =false;
                                            } else if (disconnect.equalsIgnoreCase("1") && arrFriendsFreeTime.size() == 1) {
                                                Log.e("arr", "kêt nối lại với TNV vì bb bận hết");                     //có 1 bạn mà nó k bắt máy-> gọi người lạ
                                                getListVolunteers(uid);
                                                check[0] =false;

                                            } else if (disconnect.equalsIgnoreCase("0")) {
                                                tts.speak("đã ngắt kết nối", TextToSpeech.QUEUE_FLUSH, null);
                                                check[0] =false;
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



                                Log.e("arr","id bạn bè được chọn "+ idSelected);
                            }else{ // bạn bè ko có ai rãnh thì kết nối vs người lạ
                                getListVolunteers(uid);
                            }
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }



       //Tình nguyện viên

    private void getListVolunteers(String uid){

        Log.e("abc","getlistTNV");
        arrListTNV.clear();
        mDatabase.child("TinhNguyenVien").child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    arrListTNV.add(dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mDatabase.child("TinhNguyenVien").child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {                   //list all TNV-list bạn bè -> người lạ
                    Log.e("arr","arrFriends để trừ ="+arrListFriendsNeverChange.size());
                    for(int i=0;i<arrListFriendsNeverChange.size();i++){
                        arrListTNV.remove(arrListFriendsNeverChange.get(i));
                    }
                    getStatusOfVolunteers(arrListTNV);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }




    private void getStatusOfVolunteers(final ArrayList<String> arr){


        Log.e("abc"," get Status TNV="+arr.size());
        arrTNVFreeTime.clear();
        for (int i = 0; i < arr.size(); i++) {

          Log.e("abc","tnv=" +arr.get(i));

            mDatabase.child("TinhNguyenVien").child("Status").child(arr.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String s0 = dataSnapshot.child("statusWithAll").getValue().toString();
                        int status = Integer.parseInt(s0);
                        String s1 = dataSnapshot.child("connectionRequest").getValue().toString();
                        int status1 ;
                      try {
                          status1= Integer.parseInt(s1);
                      }catch (Exception e){
                          status1=7;
                      }
                        if ((status == 1) && (status1==0 || status1==1)) {
                            arrTNVFreeTime.add(dataSnapshot.getKey());
                            Log.e("tnv free time", dataSnapshot.getKey()+" "+arrTNVFreeTime.size());

                        }

                        if (dataSnapshot.getKey().equalsIgnoreCase(arr.get(arr.size()-1))) {

                            // chọn dc bbe đang rãnh
                            if(arrTNVFreeTime.size()!=0) {
                                Random rd = new Random();
                                int number = rd.nextInt(arrTNVFreeTime.size());
                                idSelected = arrTNVFreeTime.get(number);
                                Toast.makeText(VideoCallViewActivity.this, idSelected, Toast.LENGTH_SHORT).show();
                                mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").setValue(uid);


                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("abc"," delay 5s xong ....................");
                                        checkStatusOfDevice();

                                    }
                                }, 5000);

                                final boolean[] check = {true};
                                // nếu TNV ko bắt máy thì sẽ kết nối lại vs TNV  random
                                mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(check[0] ==true) {
                                            String disconnect = dataSnapshot.getValue().toString();
                                            // bạn bè k bắt máy -> bận,, list bạn phải có 2 người trở lên thì mới kết nối lại tới bạn
                                            if (disconnect.equals("1") && arrTNVFreeTime.size() > 1) {
                                                Log.e("abc", "zô cái kết nối lại phía dưới");
                                                arrListTNV.remove(idSelected);
                                                getStatusOfVolunteers(arrListTNV);
                                                arrTNVFreeTime.clear();
                                                check[0] =false;
                                            } else if (disconnect.equalsIgnoreCase("0")) {
                                                tts.speak("đã ngắt kết nối", TextToSpeech.QUEUE_FLUSH, null);
                                                check[0] =false;
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                Log.e("arr", "id tnv được chọn"+idSelected);
                            }else{
                                Toast.makeText(VideoCallViewActivity.this, "Không có tình nguyện viên nào đang rãnh", Toast.LENGTH_SHORT).show();
                                tts.speak("không có tình nguyên viên nào đang rãnh",TextToSpeech.QUEUE_FLUSH,null);
                            }
                        }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("abc","location: "+location);
        mDatabase.child("NguoiMu").child("Location").child(uid).child("latitude").setValue(location.getLatitude());
        mDatabase.child("NguoiMu").child("Location").child(uid).child("longitude").setValue(location.getLongitude());


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        direction = event.values[0];
        int direc= (int) direction;
        int x=0;
        if(337<=direc || direc<23){
            x=1;
        }else if(23<=direc && direc<67){
            x=2;
        }else if(67<=direc && direc<113){
            x=3;
        }else if(113<=direc && direc<157){
            x=4;
        }else if(157<=direc && direc<203){
            x=5;
        }else if(203<=direc && direc<247){
            x=6;
        }else if(247<=direc && direc<293){
            x=7;
        }else if(293<=direc && direc<337){
            x=8;
        }
        mDatabase.child("NguoiMu").child("Location").child(uid).child("direction").setValue(x);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    private  void checkStatusOfDevice(){
        mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String stt = "0";
                try {
                    stt = dataSnapshot.child("checkStatusDevice").getValue().toString();
                }catch (Exception e){}

                if(stt.equals("0") )  {
                    Log.e("abc","thuê bao quý khách vừa gọi hiện đang bận");
                    mDatabase.child("TinhNguyenVien").child("Status").child(idSelected).child("connectionRequest").setValue(1); //set = 1 là tự động gọi lại
                   //TODO vẫn còn lỗi
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
