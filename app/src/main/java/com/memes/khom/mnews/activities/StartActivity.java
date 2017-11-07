package com.memes.khom.mnews.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.fragments.AllTopPostsFragment;
import com.memes.khom.mnews.fragments.MyPostsFragment;
import com.memes.khom.mnews.fragments.MyTopPostsFragment;
import com.memes.khom.mnews.fragments.PostListFragment;
import com.memes.khom.mnews.fragments.RecentPostsFragment;
import com.memes.khom.mnews.models.Category;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mPagerAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
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
            if (user.getPhotoUrl() != null)
                Picasso.with(this).load(user.getPhotoUrl()).into(avat);
            ((TextView) header.findViewById(R.id.textViewUserName)).setText(user.getDisplayName());
            ((TextView) header.findViewById(R.id.textViewEmail)).setText(user.getEmail());
        }


        //      mHistoryDatabase = new SearchHistoryTable(this);
        //     mHistoryDatabase.open();
        mSearchView = findViewById(R.id.searchView);
        initSearcher();

    }

    private void initSearcher() {
        DatabaseReference categRef = FirebaseDatabase.getInstance().getReference().child("categ");
        View v = mSearchView.findViewById(R.id.search_view_shadow);
        v.setBackgroundColor(Color.parseColor("#2E7D32"));

        if (mSearchView != null) {
            mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
            mSearchView.setHint("Поиск по категории...");
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    try {

                        mSearchView.close(false);
                        ((PostListFragment) mPagerAdapter.getCurrentFragment()).
                                refreshFragment(FirebaseDatabase.getInstance().
                                        getReference().child("posts").orderByChild("category").startAt(query)
                                        .endAt(query + "\uf8ff")
                                        .limitToFirst(50));

                        //  mHistoryDatabase.addItem(new SearchItem(query));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    try {
                        if (newText.isEmpty()) {
                            mSearchView.close(false);
                            ((PostListFragment) mPagerAdapter.getCurrentFragment()).
                                    refreshFragment(FirebaseDatabase.getInstance().
                                            getReference().child("posts")
                                            .limitToFirst(50));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
            });

            mSearchView.setOnNavigationIconClickListener(new SearchView.OnNavigationIconClickListener() {
                @Override
                public void onNavigationIconClick(float state) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    if (!drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.openDrawer(GravityCompat.START);
                    }
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

            final List<SearchItem> suggestionsList = new ArrayList<>();

            categRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Category ct = postSnapshot.getValue(Category.class);
                        if (ct != null)
                            suggestionsList.add(new SearchItem(ct.name));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            SearchAdapter searchAdapter = new SearchAdapter(StartActivity.this, suggestionsList);
            searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
                @Override
                public void onSearchItemClick(View view, int position, String text) {
                    mSearchView.close(true);
                    mSearchView.setQuery(text, true);
                }
            });
            mSearchView.setAdapter(searchAdapter);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {


        private Fragment mCurrentFragment;

        Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new AllTopPostsFragment();
                case 1:
                    return new RecentPostsFragment();
                case 2:
                    return new MyPostsFragment();
                case 3:
                    return new MyTopPostsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            switch (position) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }

            super.setPrimaryItem(container, position, object);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.heading_all_top);
                case 1:
                    return getString(R.string.heading_recent);
                case 2:
                    return getString(R.string.heading_my_posts);
                case 3:
                    return getString(R.string.heading_my_top_posts);
            }
            return null;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Toast.makeText(StartActivity.this, String.valueOf(position), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
