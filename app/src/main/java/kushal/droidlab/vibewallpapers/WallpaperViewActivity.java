package kushal.droidlab.vibewallpapers;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;


import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import com.bumptech.glide.request.transition.Transition;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class WallpaperViewActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, RewardedVideoAdListener {

    int reward;
    ImageButton download, share;
    CheckBox favCheackbox;
    ProgressBar imageLoad;
    ImageView imageView;
    Button set_wallpaper;
    String url, category, desc, id, title;
    boolean favourite;
    Wallpaper w;
    private int width,height;
    RewardedVideoAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallpaper_view);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        mAd.loadAd("ca-app-pub-2887998937008258/4814602620",
                new AdRequest.Builder()
                        .build());




        imageLoad = findViewById(R.id.progressBar_wallpapersingleview);
        imageView = findViewById(R.id.wallpaperViewSingle);
        set_wallpaper = findViewById(R.id.set_wallpaper);
        favCheackbox = findViewById(R.id.checkbox_favorite);
        download = findViewById(R.id.imageButton_download);
        share = findViewById(R.id.imageButton_share);


        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        category = intent.getStringExtra("category");
        id = intent.getStringExtra("id");
        desc = intent.getStringExtra("desc");
        title = intent.getStringExtra("title");
        category = intent.getStringExtra("category");

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        favourite = bundle.getBoolean("fav");


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels << 1;

        if (url != null) {

            imageLoad.setVisibility(View.VISIBLE);


            Glide.with(this)
                    .load(url)
                    .into(imageView);

            if (favourite) {
                favCheackbox.setChecked(true);
            }


            imageLoad.setVisibility(View.INVISIBLE);

        } else {
            Toast.makeText(getApplicationContext(), "url missing,image not loaded", Toast.LENGTH_LONG).show();
        }


        set_wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    SetWallpaper();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        favCheackbox.setOnCheckedChangeListener(this);
        download.setOnClickListener(this);
        share.setOnClickListener(this);


    }



    private void SetWallpaper() throws IOException {

        imageLoad.setVisibility(View.VISIBLE);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperManager.setBitmap(bitmap);
        imageLoad.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(),"Wallpaper Set",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getApplicationContext(), "Pls Login First", Toast.LENGTH_LONG).show();
            compoundButton.setChecked(false);
            return;
        }


        w = new Wallpaper(id, title, desc, url, category);
        DatabaseReference dbfavs = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("favourites");

        if (b) {
            dbfavs.child(w.id).setValue(w);
        } else {
            dbfavs.child(w.id).setValue(null);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.imageButton_share:
                shareWallpaper();
                break;
            case R.id.imageButton_download:

                    if (mAd.isLoaded()) {
                        mAd.show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Try Again after sometime,Ad is being loaded",Toast.LENGTH_LONG).show();
                    }

                break;

        }
    }

    private void downloadWallpaper() {

        imageLoad.setVisibility(View.VISIBLE);

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageLoad.setVisibility(View.GONE);
                        Uri uri = saveWallpaperAndgetUri(resource, id);
                        if (uri != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Toast.makeText(getApplicationContext(), "Image Saved At DCIM/Team Wallpapers ", Toast.LENGTH_LONG).show();
                            intent.setData(uri);
                            startActivity(Intent.createChooser(intent, "team wallpaper"));
                        }

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    private Uri saveWallpaperAndgetUri(Bitmap bitmap, String id) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        } else {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {


                try {
                    ContentResolver resolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, id);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Team Wallpapers");
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);


                    assert imageUri != null;
                    OutputStream fos = resolver.openOutputStream(imageUri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    assert fos != null;
                    fos.flush();
                    fos.close();

                    return imageUri;
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                File folder = new File(Environment.getExternalStorageDirectory().toString() + "DCIM/Team wallpapers");
                folder.mkdirs();


                File file = new File(folder, id + "jpeg");

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    return FileProvider.getUriForFile(WallpaperViewActivity.this, "kushal.droidlab.teamwallpapers.provider", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }


        return null;


    }


    private void shareWallpaper() {

        imageLoad.setVisibility(View.VISIBLE);

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageLoad.setVisibility(View.GONE);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource));
                        startActivity(Intent.createChooser(intent, "team wallpaper"));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpuri = null;


        try {

            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "team_wallpaper_" + System.currentTimeMillis() + ".jpeg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bmpuri = FileProvider.getUriForFile(WallpaperViewActivity.this, "kushal.droidlab.teamwallpapers.provider", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpuri;
    }


    @Override
    public void onRewardedVideoAdLoaded() {

        Log.i("tag", "Rewarded: onRewardedVideoAdLoaded");

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

        if(reward ==1){
            downloadWallpaper();
        }

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
       reward = rewardItem.getAmount();


    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {


    }

}