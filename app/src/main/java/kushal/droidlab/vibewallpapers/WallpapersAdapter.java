package kushal.droidlab.vibewallpapers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.categoryViewHolder> {


    private InterstitialAd mInterstitialAd;
    private Context mCtx;
    private List<Wallpaper> wallpaperList;



    public WallpapersAdapter(Context mCtx, List<Wallpaper> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;

    }


    @NonNull
    @Override
    public categoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_wallpaper,parent,false);
          return new categoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoryViewHolder holder, int position) {

        Wallpaper w = wallpaperList.get(position);
        Glide.with(mCtx)
                .load(w.url)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }



    public class categoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        public categoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView =itemView.findViewById(R.id.imageView_wallpaperimage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int p = getAdapterPosition();
            Wallpaper w = wallpaperList.get(p);
            Intent intent = new Intent(mCtx,WallpaperViewActivity.class);
            intent.putExtra("category",w.category);
            intent.putExtra("url",w.url);
            intent.putExtra("id",w.id);
            intent.putExtra("fav",w.isFavourite);
            intent.putExtra("desc",w.desc);
            intent.putExtra("title",w.title);
            mCtx.startActivity(intent);
        }
    }


}
