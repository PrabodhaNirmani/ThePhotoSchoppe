package com.example.acer.thephotoschoppe.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acer.thephotoschoppe.R;
import com.example.acer.thephotoschoppe.flickr.FlickrManager;
import com.example.acer.thephotoschoppe.listener.OnSwipeTouchListener;
import com.example.acer.thephotoschoppe.models.Photo;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class SingleViewActivity extends AppCompatActivity {

    private static final String TAG="SingleView";
    private ImageView imageView;
    private TextView titleTV;

    Context context;
    private int position;
    ArrayList<Photo> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_view);
        setTitle("Photo");
        context=this;

        photos= FlickrManager.getPhotos();

        // Get intent data
        Intent i = getIntent();

        // Selected image id
        position = i.getExtras().getInt("id");
        String url=i.getExtras().getString("url");

        imageView = (ImageView) findViewById(R.id.imageView);

        titleTV = (TextView) findViewById(R.id.title);
        titleTV.setText((position+1)+" of "+photos.size());
        Picasso.with(this)
                .load(url)
                .into(imageView);

        imageView.setOnTouchListener(new OnSwipeTouchListener(this){

            public void onSwipeTop() {
            }
            public void onSwipeRight() {
                if(position>0){
                    position=position-1;
                    titleTV.setText((position+1)+" of "+photos.size());
                    Picasso.with(getInstance())
                            .load(photos.get(position).getSrcUrl())
                            .into(imageView);
                }



            }
            public void onSwipeLeft() {

                if(position+1<photos.size()){
                    position=position+1;
                    titleTV.setText((position+1)+" of "+photos.size());
                    Picasso.with(getInstance())
                            .load(photos.get(position).getSrcUrl())
                            .into(imageView);
                }
            }
            public void onSwipeBottom() {
            }

        });

        TextView exploreWebTV = (TextView) findViewById(R.id.explore_web_tv);
        exploreWebTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                exploreInWeb();
            }
        });
        ImageView exploreWebBtn = (ImageView) findViewById(R.id.explore_web_btn);
        exploreWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             exploreInWeb();
            }
        });

        final TextView attachPhotoTV = (TextView) findViewById(R.id.attach_email_tv);
        attachPhotoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            attachPhoto();
            }
        });
        ImageView attachPhotoBtn = (ImageView) findViewById(R.id.attach_email_btn);
        attachPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            attachPhoto();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + photos.get(position).getName());
            try {

                while (true){
                    if(isStoragePermissionGranted()){
                        boolean newFile = file.createNewFile();
                        Log.d(TAG,"bool val : "+newFile);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private void attachPhoto(){

        Picasso.with(getApplicationContext()).load(photos.get(position).getSrcUrl()).into(target);
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + photos.get(position).getName());
        if (file.exists()) {

            Uri uri = Uri.fromFile(file);

            //Create the Intent
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

            //Fill it with Data
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Photo attached from Photo Shoppe]");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

            //Send it off to the Activity-Chooser
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));




        }

    }

    private void exploreInWeb(){
        String url = photos.get(position).getWebUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private SingleViewActivity getInstance(){
        return this;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");

                String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};

                int permsRequestCode = 200;

                requestPermissions(perms, permsRequestCode);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                Log.d(TAG,"permission granted : "+storageAccepted);
                    }

                } else {
                    // Permission Denied
                    Log.d(TAG,"permission denied.");

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
