package kushal.droidlab.vibewallpapers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;


import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


import java.util.Collections;
import java.util.List;

public class WallpaperActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    WallpapersAdapter adapter;
    List<Wallpaper> wallpaperList;
    List<Wallpaper> favList;
    DatabaseReference dbwallpaper,dbfavs;
    ProgressBar progressBar;
    Toolbar toolbar;
    String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        /* initializations */


        Intent intent =getIntent();
        category = intent.getStringExtra("category");

        favList =new ArrayList<>();
        wallpaperList = new ArrayList<>();


        recyclerView =findViewById(R.id.recycler_view_list_wallpaper);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(WallpaperActivity.this,3));
        adapter = new WallpapersAdapter(this,wallpaperList);
        progressBar = findViewById(R.id.wallpaperProgressBar);
        toolbar = findViewById(R.id.main_toolbar_wallpaper);



        toolbar.setTitle(category);
        setSupportActionBar(toolbar);

        recyclerView.setAdapter(adapter);
        dbwallpaper = FirebaseDatabase.getInstance().getReference("images")
                .child(category);



        if(FirebaseAuth.getInstance().getCurrentUser() !=null){
            dbfavs =FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");
            fetchFavWallpapers(category);
        }
        else{
            fetchWallpaper(category);
        }


    }

    private void fetchFavWallpapers(final String category){
        progressBar.setVisibility(View.VISIBLE);
        dbfavs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if(snapshot.exists()){
                    for(DataSnapshot wallpaperSnapshot: snapshot.getChildren()){

                        String id = wallpaperSnapshot.getKey();
                        String title =wallpaperSnapshot.child("title").getValue(String.class);
                        String desc =wallpaperSnapshot.child("desc").getValue(String.class);
                        String url =wallpaperSnapshot.child("url").getValue(String.class);

                        Wallpaper w = new Wallpaper(id ,title,desc ,url,category);
                        favList.add(w);

                    }

                }

                fetchWallpaper(category);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void fetchWallpaper(final String category){

        progressBar.setVisibility(View.VISIBLE);
        dbwallpaper.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if(snapshot.exists()){
                    for(DataSnapshot wallpaperSnapshot: snapshot.getChildren()){

                        String id = wallpaperSnapshot.getKey();
                        String title =wallpaperSnapshot.child("title").getValue(String.class);
                        String desc =wallpaperSnapshot.child("desc").getValue(String.class);
                        String url =wallpaperSnapshot.child("url").getValue(String.class);

                        Wallpaper w = new Wallpaper(id ,title,desc ,url,category);
                        if(isFavourite(w)){
                            w.isFavourite=true;
                        }
                        wallpaperList.add(w);

                    }
                    Collections.reverse(wallpaperList);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private boolean isFavourite(Wallpaper w){

    for(Wallpaper f: favList){
        if(f.id.equals(w.id)){
            return true;
        }
    }
    return false;
   }
}
