package com.memes.khom.mnews.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lapism.searchview.SearchView;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.fragments.AllTopPostsFragment;
import com.memes.khom.mnews.fragments.MyPostsFragment;
import com.memes.khom.mnews.fragments.MyTopPostsFragment;
import com.memes.khom.mnews.fragments.PostListFragment;
import com.memes.khom.mnews.fragments.RecentPostsFragment;
import com.memes.khom.mnews.models.Category;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mPagerAdapter;
    private SearchView mSearchView;
    private SearchableSpinner catSpinner;
    private ImageView imageClearSpinner;
   // private String[] arrayOfSelectSpinner = {"", "", "", ""};
   // private int currentFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        imageClearSpinner = findViewById(R.id.imageClearSpinner);
        navigationView.setNavigationItemSelectedListener(this);
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        SmartTabLayout tabLayout = findViewById(R.id.tabs);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        tabLayout.setViewPager(mViewPager);
        catSpinner = findViewById(R.id.searchableSpinnerCat);
        catSpinner.setTitle(getString(R.string.select_cat));
        catSpinner.setPositiveButton(getString(R.string.chose));
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

        mSearchView = findViewById(R.id.searchView);
        initSearcher();
        fillSpinnerCat();
    }

    private void initSearcher() {
        View v = mSearchView.findViewById(R.id.search_view_shadow);
        v.setBackgroundColor(Color.parseColor("#3C515C"));

        if (mSearchView != null) {
            mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
            mSearchView.setHint(R.string.find_by_tag);
            // mSearchView.setTextOnly(R.string.tag_symbol);
            // mSearchView.setSele(mSearchView.getTextOnly().length());

            EditText mSearchEditText = findViewById(com.lapism.searchview.R.id.search_searchEditText);
            if (mSearchEditText != null) {
                // mSearchEditText.setText("#");
                mSearchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable text) {
                        String val = text.toString();
                        if (val.length() == 1 && !val.substring(0, 1).equals("#"))
                            text.insert(0, "#");
                    }
                });
            }

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // mSearchView.setQuery("#"+ query, false);
                    try {
                        mSearchView.close(false);
                        ((PostListFragment) mPagerAdapter.getCurrentFragment()).
                                refreshFragment(FirebaseDatabase.getInstance().
                                        getReference().child("posts").orderByChild("title").startAt(query)
                                        .endAt(query + "\uf8ff")
                                        .limitToFirst(50));

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

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            exitDialog();
        }
    }


    private void fillSpinnerCat() {
        try {
            final List<String> cats = new ArrayList<>();
            cats.add(this.getResources().getString(R.string.all_cats));
            FirebaseDatabase.getInstance().getReference().child("categ")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                Category ct = postSnapshot.getValue(Category.class);
                                if (ct != null)
                                    cats.add(ct.name);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            final ArrayAdapter arrayAdapter = new ArrayAdapter<>(StartActivity.this, R.layout.item_sp, cats);
            catSpinner.setAdapter(arrayAdapter);

            catSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) parent.getChildAt(0);
                    textView.setTextColor(Color.WHITE);
                   // textView.setTextSize(16);
                    String item = textView.getText().toString();
                    if (!item.equals(StartActivity.this.getResources().getString(R.string.all_cats))) {
                        if (mPagerAdapter.getCurrentFragment() instanceof AllTopPostsFragment ||
                                mPagerAdapter.getCurrentFragment() instanceof RecentPostsFragment)
                            ((PostListFragment) mPagerAdapter.getCurrentFragment()).
                                    refreshFragment(FirebaseDatabase.getInstance().
                                            getReference().child("posts").orderByChild("category").startAt(textView.getText().toString())
                                            .endAt(textView.getText().toString() + "\uf8ff")
                                            .limitToFirst(50));
                       // arrayOfSelectSpinner[currentFrag] = textView.getText().toString();
                    } else {
                       // arrayOfSelectSpinner[currentFrag] = "";
                        if (mPagerAdapter.getCurrentFragment() instanceof AllTopPostsFragment ||
                                mPagerAdapter.getCurrentFragment() instanceof RecentPostsFragment)
                            ((PostListFragment) mPagerAdapter.getCurrentFragment()).
                                    refreshFragment(FirebaseDatabase.getInstance().
                                            getReference().child("posts")
                                            .limitToFirst(50));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            catSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            imageClearSpinner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    catSpinner.setSelection(0);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
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

            case R.id.nav_log_out:
                new MaterialDialog.Builder(this)
                        .title(R.string.attention)
                        .content(R.string.exit_cur_user)
                        .positiveText(R.string.yes).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(StartActivity.this, SignInActivity.class));
                        finish();
                    }
                })
                        .negativeText(R.string.cancel)
                        .show();
                break;

            default:
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void exitDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.attention)
                .content(R.string.exit_app)
                .positiveText(R.string.yes).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Process.killProcess(Process.myPid());
            }
        })
                .negativeText(R.string.cancel)
                .show();

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
            // Toast.makeText(StartActivity.this, String.valueOf(position), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPageSelected(int position) {
            // Toast.makeText(StartActivity.this, String.valueOf(position), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
