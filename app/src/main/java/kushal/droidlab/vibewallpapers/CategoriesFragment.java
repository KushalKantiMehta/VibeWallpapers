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
;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class CategoriesFragment extends Fragment {
    private List<Category> categoryList;
    private ProgressBar progressBar;
    private categoriesAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        progressBar = view.findViewById(R.id.categoriesProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_list_categories);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),1));
        categoryList = new ArrayList<>();

        adapter = new categoriesAdapter(getActivity(),categoryList);
        recyclerView.setAdapter(adapter);


        DatabaseReference dbcategories = FirebaseDatabase.getInstance().getReference("categories");

        dbcategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        for(DataSnapshot ds: snapshot.getChildren()) {
                              String name = ds.getKey();
                              String desc = ds.child("desc").getValue(String.class);
                                String thumb = ds.child("thumbnail").getValue(String.class);

                               Category c = new Category(name, desc, thumb);
                                 categoryList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}