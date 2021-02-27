package kushal.droidlab.vibewallpapers;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import java.util.List;

public class categoriesAdapter extends RecyclerView.Adapter<categoriesAdapter.categoryViewHolder> {




    private final Context mCtx;
    private final List<Category> categoryList;

    public categoriesAdapter(Context mCtx, List<Category> categoryList) {
        this.mCtx = mCtx;
        this.categoryList = categoryList;


    }


    @NonNull
    @Override
    public categoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerviewcategories,parent,false);
          return new categoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoryViewHolder holder, int position) {

        Category c =categoryList.get(position);
        holder.textView.setText("    "+c.name);
        Glide.with(mCtx)
                .load(c.thumb)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class categoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView textView;
        final ImageView imageView;
        public categoryViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView_catname);
            imageView =itemView.findViewById(R.id.imageView_catimage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

             int p = getAdapterPosition();
             Category c = categoryList.get(p);
            Intent intent = new Intent(mCtx,WallpaperActivity.class);
            intent.putExtra("category",c.name);
            mCtx.startActivity(intent);
        }
    }
}
