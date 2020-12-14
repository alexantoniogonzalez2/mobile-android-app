package com.example.comp90018_2020_sem2_project.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comp90018_2020_sem2_project.util.CameraPhotoHandler;
import com.example.comp90018_2020_sem2_project.Adapter.BroadcastMessageAdapter;
import com.example.comp90018_2020_sem2_project.dataClass.ExternalUser;
import com.example.comp90018_2020_sem2_project.dataClass.Location_STR;
import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.util.CircleBubbleTransformation;
import com.example.comp90018_2020_sem2_project.util.CircleBubbleTransformationUser;
import com.example.comp90018_2020_sem2_project.util.PicassoMarker;
import com.example.comp90018_2020_sem2_project.util.AnimationUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.hardware.Sensor.TYPE_LIGHT;


public class MapFragment extends Fragment implements View.OnClickListener {

    //Map marker values for tracking
    PicassoMarker marker;
    private Marker myMarkerPrivate;
    List<PicassoMarker> targets;

    // Variable to change the location by choosing the final location
    private boolean changeLocation;


    //Sensors and trackers for luminosity
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightEvent;
    private float maxValue;
    private Boolean DarkMode= false;

    // Initialise the map fragment
    SupportMapFragment supportMapFragment;

    // Client Location provider and id values
    FusedLocationProviderClient client;
    private DatabaseReference mDatabase ;
    private String userid ;

    //Object to hold the current users details on device
    ExternalUser currentUser;

    // //Getting Firebase Instance
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    //Getting Database Reference
    DatabaseReference databaseReference = database.getReference();

    //Get a reference to the app users
    Query query = databaseReference.child("Users");

    //Maps database reference string to externalUser object
    HashMap<String, ExternalUser> nearbyExternalUsers;

    // Store users location, requires it to be updated when moved
    Location locationuser;
    LatLng currentUserLatLng;


    // Map artifacts for recording audio
    private Button recordButton;
    private MediaRecorder recorder;
    private  String fileName = null;
    private String recordedName = null;
    private String uploadFilename = null;
    private boolean permissionToRecordAccepted = false;
    private String userName;
    private RelativeLayout progressRL;

    // Objects for downloading and recieving external audio broadcasts
    private DatabaseReference mDatabaseaudio;
    BroadcastMessageAdapter adapter;
    RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    List<String> users;
    List<String> stringURL;

    //Radius within which external users will be shown
    private int LocationRadius = 2000;

    // Objects used for accessing the camera and displaying broadcast photos
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageButton photoThumbnail;
    ImageView expandedView;
    TextView photoText;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference userStorageReference = storage.getReferenceFromUrl("gs://ass2-c9cc7.appspot.com/Users");
    Query imageUpdateQuery = databaseReference.child("BroadcastImages");
    public static Uri lastPhotoUri;

    /*
    Deal with activity result form using the camera to then display the taken picture and send it to other users.
    Broadcasts by uploading to firebase
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode != 0) {
            try {
                Bitmap thumbnailImage = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), lastPhotoUri), 300, 400);
                uploadPhoto(thumbnailImage, lastPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Uploads a photo and it's thumbnail to the current users node in firebase storage
     */
    private void uploadPhoto(Bitmap imageBitmap, Uri uri) {
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
        byte[] imageData = imageOutputStream.toByteArray();
        userStorageReference.child(userid).child("lastImageThumbnail").putBytes(imageData);
        userStorageReference.child(userid).child("lastFullImage").putFile(uri);
        mDatabase.child("BroadcastImages").child(userid).setValue(System.currentTimeMillis());
        return;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getActivity().getIntent();
        userid = i.getStringExtra("message");
        userName = i.getStringExtra("username");

        ///change MAP theme based on luminosity;
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor =sensorManager.getDefaultSensor(TYPE_LIGHT);
        maxValue = lightSensor.getMaximumRange();

        lightEvent =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float currLum = event.values[0];
                final int val = (int) (255f*currLum/maxValue);
                changeTheme(val);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }


    /*
    Sets up the view of the map and establishes database references
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        targets = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        progressRL = root.findViewById(R.id.progressBarRL);


         // downloading audio
        mAuth = FirebaseAuth.getInstance();
        users = new ArrayList<>();
        stringURL = new ArrayList<>();
        mDatabaseaudio = FirebaseDatabase.getInstance().getReference().child("VoiceBroadcast").child(userid);
        recyclerView = root.findViewById(R.id.rv_map);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DisplayVoiceMessage();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setup the record audio button
        recordButton = root.findViewById(R.id.recordButton);
        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    startRecording();

                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    stopRecording();

                }
                return false;
            }
        });
        // END RECORDING


        // Button to change the location by choosing the final location
        changeLocation = false;
        final Button setLocationButton = root.findViewById(R.id.set_location);
        setLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (!changeLocation) {
                    changeLocation = true;
                    setLocationButton.setBackgroundResource(R.drawable.cancel);

                }else{
                    changeLocation = false;
                    setLocationButton.setBackgroundResource(R.drawable.move_btn);
                }
            }
        });

        // Buttons to change the location by one step at time
        final ImageButton buttonMoveUp = root.findViewById(R.id.arrow_up);
        ImageButton buttonMoveDown = root.findViewById(R.id.arrow_down);
        ImageButton buttonMoveRight = root.findViewById(R.id.arrow_right);
        ImageButton buttonMoveLeft = root.findViewById(R.id.arrow_left);
        buttonMoveUp.setOnClickListener(new View.OnClickListener(){
           public void onClick(View view) { moveOneStep("up");  }
        });
        buttonMoveDown.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) { moveOneStep("down"); }
        });
        buttonMoveRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) { moveOneStep("right"); }
        });
        buttonMoveLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) { moveOneStep("left"); }
        });

        /*
        Setup the button for accessing the camera and viewing photo thumbnails in app.
        Establishes the photo and thumbnail displays on the map
         */
        photoThumbnail = root.findViewById(R.id.photoThumbnail);
        photoText = root.findViewById(R.id.photolabel);
        expandedView = root.findViewById(R.id.expanded_image);
        final ImageButton cameraButton = root.findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    CameraPhotoHandler cph = new CameraPhotoHandler(view.getContext());
                    Uri photoUri = cph.createImageFile();
                    lastPhotoUri = photoUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException | IOException e) {
                }
            }
        });


        // Reference the map fragment to the xml fragment
        supportMapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);

        // Create an object of location services
        client = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check if permission for location is given
        // If user has already given permission, call current location function
        // If location permission was not given, request location permission
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(userid);

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        /*
        Through this listener it is updated the location with the method in which the user just
        choose a point in the map.
        */
        supportMapFragment.getMapAsync((new OnMapReadyCallback() {

            @Override
            public void onMapReady(final GoogleMap googleMap) {
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
            {
                @Override
                public void onMapClick(LatLng arg0) {

                    if (changeLocation) {
                        // It is the removed the current marker.
                        AnimationUtil.animateMarkerTo(myMarkerPrivate,arg0);
                        // It is added a new marker in the clicked place.
                        float zoom = googleMap.getCameraPosition().zoom;
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(arg0, zoom);
                        googleMap.animateCamera(update);
                        // It is created a new location.
                        Location newLocation = new Location("new location");
                        newLocation.setLatitude(arg0.latitude);
                        newLocation.setLongitude(arg0.longitude);
                        // It is updated the location.
                        locationuser = newLocation;
                        Location_STR l = new Location_STR(newLocation);
                        mDatabase.child("Users").child(userid).child("CurrentLocation").setValue(l);
                        changeLocation = false;
                        setLocationButton.setBackgroundResource(R.drawable.move_btn);
                    }
                }
            });
            }
        }));
        return root;
    }

    // Gets the users location and pans to that location in the map
    private void getCurrentLocation(final String Uid) {

        // Redundant but needs to be checked before requesting the last location
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Gets users last known location ( Could be null if this is being run first time )
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    // Store the location returned by the client in Location instance
                    Location location1 = task.getResult();
                    locationuser = task.getResult();
                    // Check if user has a previous known location
                    // If true display it in the map

                    if (location1 != null) {
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(final Location location) {
                                // Sync the value returned here to the fragment UI
                                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        UiSettings uiSettings = googleMap.getUiSettings();
                                        uiSettings.setZoomControlsEnabled(true);
                                        // Convert the location in latitude and logitude
                                        // Create a marker on that position and zoom
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        final Location_STR l = new Location_STR(location);
                                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                        rootRef.child("Users").child(userid).child("CurrentLocation");
                                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.hasChild("Users/"+userid+"/CurrentLocation")){
                                                    mDatabase.child("Users").child(userid).child("CurrentLocation").setValue(l);
                                                }
                                                establishExternalUserReferences();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });
                            }
                        });

                    } else {

                        // If there was no previous location stored, request the current location of the user
                        // Check for permission
                        // Sync the returned value

                        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            final LocationRequest locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(1000).setNumUpdates(1);
                            LocationCallback locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    final Location location1 = locationResult.getLastLocation();
                                    locationuser = location1;
                                    supportMapFragment.getMapAsync((new OnMapReadyCallback() {
                                        @Override
                                        public void onMapReady(GoogleMap googleMap) {

                                            final Location_STR l = new Location_STR(location1);
                                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                            rootRef.child("Users").child(userid).child("CurrentLocation");
                                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(!snapshot.hasChild("Users/"+userid+"/CurrentLocation")){
                                                        mDatabase.child("Users").child(userid).child("CurrentLocation").setValue(l);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }));
                                }
                            };
                            client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        } else {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        establishExternalUserReferences();
                    }
                }
            });
        }

    }

    // Callback function when user grants location permission
    // Request code 44 is for location services
    // If permission granted call location services
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation(userid);
            }
        }
    }

    /*
    Establishes the initial map of external users and sets the query methods for dealing with
    changes to the users and broadcasts in the database.
     */
    public void establishExternalUserReferences() {
        nearbyExternalUsers = new HashMap<String, ExternalUser>();

        //Establish the extUser object the the main user
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                currentUser = new ExternalUser(snapshot.child((userid)));
                double lat = (double) snapshot.child(userid).child("CurrentLocation").child("latitude").getValue();
                double lng = (double) snapshot.child(userid).child("CurrentLocation").child("longitude").getValue();
                currentUserLatLng = new LatLng(lat, lng);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Set up the methods for handling changes to each user node in the database
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final String userKey = dataSnapshot.getKey();

                //Handle setup of main user on map
                if (userKey.equals(userid)) {
                    supportMapFragment.getMapAsync((new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //Setup user data and create/move to marker
                            ExternalUser curUser = currentUser;
                            LatLng userPos = currentUser.getmOptions().getPosition();
                            Marker myMarker = googleMap.addMarker((curUser.getmOptions()));
                            myMarkerPrivate = myMarker;
                            curUser.setMarker(myMarker);
                            generatePicassoMarker(myMarker, curUser,true);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPos, 10));
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userPos, 15);
                            CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
                            googleMap.moveCamera(update);
                            googleMap.animateCamera(zoom);
                        }
                    }));

                } else {
                    //Handle setup of an external user on the map
                    if (dataSnapshot.hasChild("CurrentLocation")) {
                        nearbyExternalUsers.put(userKey, new ExternalUser(dataSnapshot));
                    }
                    supportMapFragment.getMapAsync((new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            if (dataSnapshot.hasChild("CurrentLocation")) {
                                //Setup data for external user and create/display their marker
                                ExternalUser extUser = nearbyExternalUsers.get(userKey);
                                LatLng extUserPos = extUser.getmOptions().getPosition();
                                Marker myMarker = googleMap.addMarker(extUser.getmOptions());
                                extUser.setMarker(myMarker);
                                generatePicassoMarker(myMarker, extUser, false);
                                // Get the distance between our location and all the other users in our "externaluser hashmap"
                                // Requires an array, but since there will be only one distance it will always be stored in result[0]
                                float[] result = new float[10];
                                Location.distanceBetween(currentUserLatLng.latitude, currentUserLatLng.longitude, extUserPos.latitude, extUserPos.longitude, result);

                                if (result[0] <= LocationRadius) {
                                    extUser.getMarker().setVisible(true);
                                } else {
                                    extUser.getMarker().setVisible(false);
                                }
                            }
                        }
                    }));
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, final String s) {
                //Handle an update to the main user
                //Updates the map-marker location
                if (dataSnapshot.getKey().equals(userid)) {
                    double lat = (double) dataSnapshot.child("CurrentLocation").child("latitude").getValue();
                    double lng = (double) dataSnapshot.child("CurrentLocation").child("longitude").getValue();
                    currentUserLatLng = new LatLng(lat, lng);
                    currentUser.getMarker().setPosition(currentUserLatLng);
                    updateExternalUserVisibility();

                } else {
                    //Handle an update to an external user
                    //Get the new location of the user and update their map marker location.
                    double lat = (double) dataSnapshot.child("CurrentLocation").child("latitude").getValue();
                    double lng = (double) dataSnapshot.child("CurrentLocation").child("longitude").getValue();
                    LatLng newPos = new LatLng(lat, lng);
                    if (nearbyExternalUsers.get(dataSnapshot.getKey()) != null) {
                        nearbyExternalUsers.get(dataSnapshot.getKey()).getMarker().setPosition(newPos);
                        float[] result = new float[10];
                        Location.distanceBetween(currentUserLatLng.latitude, currentUserLatLng.longitude, newPos.latitude, newPos.longitude, result);

                        if (result[0] <= LocationRadius) {
                            nearbyExternalUsers.get(dataSnapshot.getKey()).getMarker().setVisible(true);
                        } else {
                            nearbyExternalUsers.get(dataSnapshot.getKey()).getMarker().setVisible(false);
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        /*
        Add an event listener for the broadcast image update detection
         */
        imageUpdateQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            /*
            Retrieve the latest broadcast image and thumbnail URIs, and set them to the objects
            in the map.
             */
            @Override
            public void onChildChanged(@NonNull final DataSnapshot snapshot, @Nullable String previousChildName) {
                String uploaderName = "";
                if (snapshot.getKey().equals(userid)) {
                    uploaderName = currentUser.getUserName();
                } else {
                    uploaderName = nearbyExternalUsers.get(snapshot.getKey()).getUserName();
                }
                final StorageReference imgRef = userStorageReference.child(snapshot.getKey()).child("lastFullImage");
                final String finalUploaderName = uploaderName;
                StorageReference thumbRef = userStorageReference.child(snapshot.getKey()).child("lastImageThumbnail");

                //Get thumbnail URI from firebase
                thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Load thumbnail into thumbnail image button
                        photoText.setText("Posted by:\n"+ finalUploaderName);
                        Picasso.get().load(uri.toString()).into(photoThumbnail);
                        photoThumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //When thumbnail clicked, load the full image
                                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri.toString()).into(expandedView);
                                        expandedView.setVisibility(View.VISIBLE);
                                        expandedView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //When full image clicked, remove its visibility
                                                expandedView.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    /*
    Updates the visibility of the external users based on the location radius
     */
    public void updateExternalUserVisibility() {
        for (String key: nearbyExternalUsers.keySet()) {
            LatLng extUserPos = nearbyExternalUsers.get(key).getMarker().getPosition();
            float[] result = new float[10];
            Location.distanceBetween(currentUserLatLng.latitude, currentUserLatLng.longitude, extUserPos.latitude, extUserPos.longitude, result);
            if (result[0] <= LocationRadius) {
                nearbyExternalUsers.get(key).getMarker().setVisible(true);
            } else {
                nearbyExternalUsers.get(key).getMarker().setVisible(false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        changeLocation = true;
    }

    /*
    Generates a picasso marker based on the input marker
     */
    public void generatePicassoMarker(Marker myMarker, ExternalUser extUser, Boolean userType) {

        myMarker.setTitle(extUser.getUserName());
        myMarker.setSnippet(extUser.getEmail());

        marker = new PicassoMarker(myMarker);
        targets.add(marker);
        int avatar = R.drawable.boy_idle__2_;
        if(extUser.getAvatarString() != null) {
            if (extUser.getAvatarString().equals("Avatar1")) {
                avatar = R.drawable.boy_idle__2_;
            } else {
                avatar = R.drawable.idle__2_;
            }
        }
        if(userType) {

            Picasso.get()
                    .load(avatar)
                    .resize(150, 150)
                    .centerCrop()
                    .transform(new CircleBubbleTransformationUser(getContext(),true))
                    .into(marker);
        }
        else{
            Picasso.get()
                    .load(avatar)
                    .resize(150, 150)
                    .centerCrop()
                    .transform(new CircleBubbleTransformation(getContext(),false))
                    .into(marker);
        }

    }

    /*
    Moves the current user one step in a specified direction
     */
    public void moveOneStep(final String direction) {

        supportMapFragment.getMapAsync((new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                double deltaLatitude = 0;
                double deltaLongitude = 0;
                double delta = 0.0005;
                switch (direction) {
                    case "up":
                        deltaLatitude = delta;
                        break;
                    case "down":
                        deltaLatitude = -delta;
                        break;
                    case "right":
                        deltaLongitude = delta;
                        break;
                    case "left":
                        deltaLongitude = -delta;
                        break;
                }

                double newLat = currentUserLatLng.latitude + deltaLatitude;
                double newLng = currentUserLatLng.longitude + deltaLongitude;
                LatLng newLatLng = new LatLng(newLat, newLng);
                // It is added a new marker in the clicked place.
                float zoom = googleMap.getCameraPosition().zoom;
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newLatLng, zoom);
                googleMap.animateCamera(update);
                mDatabase.child("Users").child(userid).child("CurrentLocation").child("latitude").setValue(newLat);
                mDatabase.child("Users").child(userid).child("CurrentLocation").child("longitude").setValue(newLng);
                changeLocation = false;

            }
        }));
    }


    /*
    Begin the audio recording functionality
     */
    private void startRecording() {

        recorder = new MediaRecorder();
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        Date currentTime = Calendar.getInstance().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String timeToString = dateFormat.format(currentTime);
        fileName += "/recorded"+timeToString+".3gp";
        uploadFilename = userid + timeToString +".3gp";
        recordedName = userid + timeToString ;

        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("failed", "prepare() failed" + e);
        }

        try {
            recorder.start();
        } catch (Exception e) {
            Log.e("failed", "start() failed" + e);
        }

    }

    /*
    Stop the recording
     */
    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;

            uploadAudio();
        } catch(RuntimeException stopException) {
            recorder.reset();
            File file = new File(fileName);
            file.delete();
            fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }
    }

    /*
    Upload the audio to firebase storage and broadcast
     */
    private void uploadAudio() {
        progressRL.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference filepath = storageReference.child("Users").child(userid).child("Audio").child(uploadFilename);
        Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {
                       Uri downloadUrl = uri;
                       String uris = downloadUrl.toString();

                       for(Map.Entry<String,ExternalUser> entry : nearbyExternalUsers.entrySet()){
                           String userId = entry.getKey();
                           ExternalUser externalUser = entry.getValue();
                           String myUserId = userid;
                           LatLng externalUserLoc = externalUser.getCurrentLoc();
                           float[] result = new float[10];
                           Location.distanceBetween(currentUserLatLng.latitude, currentUserLatLng.longitude, externalUserLoc.latitude, externalUserLoc.longitude, result);

                           if(!userId.equals(myUserId) && result[0] <= LocationRadius ) {
                               HashMap<String, String> map = new HashMap<String, String>();
                               map.put("username",userName);
                               map.put("url",uris);
                               map.put("from",myUserId);
                               map.put("listened","true");
                               mDatabase.child("VoiceBroadcast").child(userId).child(recordedName).setValue(map);
                           }
                       }
                   }
               });

                progressRL.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Broadcasted!", Toast.LENGTH_SHORT).show();
                fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
        });

    }

    /*
    Displays a button to play a recieved audio message
     */
    private void DisplayVoiceMessage() {
                mDatabaseaudio.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        stringURL.clear();
                        for (DataSnapshot recordings : snapshot.getChildren()) {
                            String user = recordings.child("username").getValue().toString();
                            String url = recordings.child("url").getValue().toString();
                            users.add(user);
                            stringURL.add(url);
                        }
                        adapter = new BroadcastMessageAdapter(getActivity());
                        recyclerView.setAdapter(adapter);
                        adapter.addData(users,stringURL);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }


    /*
    Changes the theme of the map based on the input luminosity
     */
    public  void changeTheme(final int val){
        supportMapFragment.getMapAsync((new OnMapReadyCallback() {

            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if(!DarkMode && val<=600) {
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.night_style));
                    DarkMode =true;
                }
                if(DarkMode && val>600){
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.standard_style));
                    DarkMode =false;
                }
            }
        }));
    }


    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(lightEvent,lightSensor,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightEvent);
    }
}