package kushal.droidlab.vibewallpapers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;


import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    Fragment fragment_current;
    int frag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        drawerLayout= findViewById(R.id.drawerLayout);
        navigationView =findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.main_toolbar_home);
        HomeFragment homeFragment =new HomeFragment();
        fragment_current = new HomeFragment();
        frag =1;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_fragment,homeFragment)
                .commit();

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle =new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }



    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (frag != 1) {
                displayFragment(new HomeFragment(), 1);
            } else {
                super.onBackPressed();
            }

        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment;
        int frag;
        switch (item.getItemId()){
            case R.id.nav_home:
                fragment = new HomeFragment();
                frag =1;
                break;
            case R.id.nav_categories:
                fragment = new CategoriesFragment();
                frag =2;
                break;
            case R.id.nav_fav:
                fragment =new FavoriteFragment();
                frag =3;
                break;
            case R.id.nav_Account:
                fragment =new AccountFragment();
                frag =4;
                break;
            case R.id.nav_About:
                fragment =new AboutusFragment();
                frag =5;
                break;
            default:
                fragment = new HomeFragment();
                frag =1;
                break;
        }

        displayFragment(fragment,frag);
        return true;
    }

    private void displayFragment(Fragment fragment,int no){
              frag=no;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_fragment,fragment)
                .commit();
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}