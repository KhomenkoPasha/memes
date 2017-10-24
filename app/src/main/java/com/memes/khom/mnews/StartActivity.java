package com.memes.khom.mnews;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.memes.khom.mnews.fragments.MyPostsFragment;
import com.memes.khom.mnews.fragments.MyTopPostsFragment;
import com.memes.khom.mnews.fragments.RecentPostsFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.memes.khom.mnews.R.drawable.profile;

public class StartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[]{
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Button launches NewPostActivity
        findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, NewPostActivity.class));
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            Toast.makeText(this, user.getEmail(), Toast.LENGTH_LONG).show();
           // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            navigationView.setNavigationItemSelectedListener(this);
            View header = navigationView.getHeaderView(0);
            CircleImageView avat = header.findViewById(R.id.profile_image);
            Picasso.with(this).load(user.getPhotoUrl()).into(avat);
            ((TextView) header.findViewById(R.id.textViewUserName)).setText(user.getDisplayName());
            ((TextView) header.findViewById(R.id.textViewEmail)).setText(user.getEmail());
        }


        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this);

        mSearchView = findViewById(R.id.searchView);

        View v = mSearchView.findViewById(R.id.search_view_shadow);
        v.setBackgroundColor(Color.parseColor("#2E7D32"));

        if (mSearchView != null) {
            mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
            mSearchView.setHint("Поиск...");
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mHistoryDatabase.addItem(new SearchItem(query));
                    mSearchView.close(false);
                    return true;
                }


                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            mSearchView.setOnNavigationIconClickListener(new SearchView.OnNavigationIconClickListener() {
                @Override
                public void onNavigationIconClick(float state) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    if (!drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.openDrawer(GravityCompat.START);}
                }
            });


            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public boolean onOpen() {
                    return true;
                }

                @Override
                public boolean onClose() {
                    return true;
                }
            });
            mSearchView.setVoiceText("Set permission on Android 6.0+ !");
            mSearchView.setOnVoiceIconClickListener(new SearchView.OnVoiceIconClickListener() {
                @Override
                public void onVoiceIconClick() {
                    // permission
                }
            });

            List<SearchItem> suggestionsList = new ArrayList<>();
            suggestionsList.add(new SearchItem("search1"));
            suggestionsList.add(new SearchItem("search2"));
            suggestionsList.add(new SearchItem("search3"));

            SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList);
            searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
                @Override
                public void onSearchItemClick(View view, int position, String text) {
                    mHistoryDatabase.addItem(new SearchItem(text));
                    mSearchView.close(false);
                }
            });
            mSearchView.setAdapter(searchAdapter);

            suggestionsList.add(new SearchItem("search1"));
            suggestionsList.add(new SearchItem("search2"));
            suggestionsList.add(new SearchItem("search3"));
            searchAdapter.notifyDataSetChanged();

            List<SearchFilter> filter = new ArrayList<>();
            filter.add(new SearchFilter("Filter1", true));
            filter.add(new SearchFilter("Filter2", true));
            mSearchView.setFilters(filter);

            //use mSearchView.getFiltersStates() to consider filter when performing search
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_create:
                startActivity(new Intent(StartActivity.this, NewPostActivity.class));
                break;

            case R.id.my_profile:
                Intent about = new Intent(StartActivity.this, UserProfile.class);
                startActivity(about);
                break;

            case R.id.nav_send:
                ////    Intent about = new Intent(StartActivity.this, FaceebookRegAct.class);
                //   startActivity(about);
                break;

                default:
                    break;
            }


            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {
            switch (item.getItemId()) {

                case R.id.action_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
