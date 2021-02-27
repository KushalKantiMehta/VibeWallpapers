package kushal.droidlab.vibewallpapers;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

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


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    WallpapersAdapter adapter;
    List<Wallpaper> wallpaperList;
    List<Wallpaper> favList;
    DatabaseReference dbwallpaper,dbfavs;
    ProgressBar progressBar;
    String category="all";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);





        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });



        wallpaperList = new ArrayList<>();
        favList = new ArrayList<>();


        progressBar = view.findViewById(R.id.homeProgressBar);
        recyclerView = view.findViewById(R.id.recycler_view_list_home);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        progressBar.setVisibility(View.VISIBLE);
        adapter = new WallpapersAdapter(getActivity(), wallpaperList);
        recyclerView.setAdapter(adapter);

        dbwallpaper = FirebaseDatabase.getInstance().getReference("all");




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