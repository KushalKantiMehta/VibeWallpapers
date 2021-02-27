package kushal.droidlab.vibewallpapers;

import android.content.Intent;
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
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private static final int GOOGLE_SiGN_IN_CODE =212;
    private GoogleSignInClient mGoogleSignInClient;

    RecyclerView recyclerView;
    WallpapersAdapter adapter;
    List<Wallpaper> wallpaperList;
    List<Wallpaper> favList;
    DatabaseReference dbwallpaper,dbfavs;
    ProgressBar progressBar;
    String category="favourites";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            return inflater.inflate(R.layout.fav_not_available, container, false);
        }

        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(),gso);


        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {

            wallpaperList = new ArrayList<>();
            favList = new ArrayList<>();


            progressBar = view.findViewById(R.id.ProgressBar_fav);
            recyclerView = view.findViewById(R.id.recycler_view_list_fav);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            progressBar.setVisibility(View.VISIBLE);
            adapter = new WallpapersAdapter(getActivity(), wallpaperList);
            recyclerView.setAdapter(adapter);


            dbwallpaper = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");


            dbfavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");


            fetchFavWallpapers(category);

        }else{
            view.findViewById(R.id.google_signin_fav).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(intent,GOOGLE_SiGN_IN_CODE);
                }
            });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SiGN_IN_CODE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(),"Log in Successfully",Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new FavoriteFragment()).commit();
                }
                else{
                    Toast.makeText(getActivity(),"Log in Failed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}