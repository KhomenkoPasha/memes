package com.memes.khom.mnews.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;
import com.memes.khom.memsnews.R;
import com.memes.khom.mnews.activities.PictureActivity;
import com.memes.khom.mnews.models.Comment;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.models.User;
import com.memes.khom.mnews.utils.Convert;
import com.rilixtech.materialfancybutton.MaterialFancyButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.rilixtech.materialfancybutton.MaterialFancyButton.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


public class RandomPostFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ChildEventListener mPostListener;
    private String mPostKey;
    private String postUID;
    private RandomPostFragment.CommentAdapter mAdapter;
    private StorageReference mStorageRef;
    private TextView mAuthorView;
    private TextView datePost;
    private ImageView post_author_photo;
    private TextView mTitleView;
    private TextView mBodyView, categ, likesCount;
    private ImageView iv_piture, share, like;
    private DownloadButtonProgress download;
    private EmojiconEditText mCommentField;
    private RecyclerView mCommentsRecycler;
    private Uri uriPhoto;
    private int likeCount;
    private boolean likeActive;
    private AdView adView;
    public static final String TAG = "RandomPostFragment";

    public RandomPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_random_post, container, false);


        adView = rootView.findViewById(R.id.ad_view_start);

        AdRequest ar = new AdRequest.Builder().addTestDevice("17E7C600E7DAEDDA830D5901FF625D10").build();
        adView.loadAd(ar);

        datePost = rootView.findViewById(R.id.post_date);
        post_author_photo = rootView.findViewById(R.id.post_author_photo);
        mAuthorView = rootView.findViewById(R.id.post_author);
        mTitleView = rootView.findViewById(R.id.post_title);
        categ = rootView.findViewById(R.id.categ);
        mBodyView = rootView.findViewById(R.id.post_body);
        mCommentField = rootView.findViewById(R.id.field_comment_text);
        ImageView mCommentButton = rootView.findViewById(R.id.button_post_comment);
        mCommentsRecycler = rootView.findViewById(R.id.recycler_comments);
        iv_piture = rootView.findViewById(R.id.iv_piture);
        likesCount = rootView.findViewById(R.id.post_num_stars);
        like = rootView.findViewById(R.id.like);
        like.setOnClickListener(this);
        download = rootView.findViewById(R.id.download);
        download.setOnClickListener(this);
        share = rootView.findViewById(R.id.share);
        share.setOnClickListener(this);
        mCommentButton.setOnClickListener(this);

        MaterialFancyButton newMem = rootView.findViewById(R.id.btn_add_picture);
        newMem.setOnClickListener(this);

        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getActivity(), R.drawable.devider)));
        mCommentsRecycler.addItemDecoration(itemDecorator);

        iv_piture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    Intent myIntent = new Intent(getActivity(), PictureActivity.class);
                    myIntent.putExtra("photo_url", uriPhoto);
                    getActivity().startActivity(myIntent);
                }
            }
        });

        return rootView;
    }


    private void randomMem() {

        try {
            mPostKey = "";
            mStorageRef = FirebaseStorage.getInstance().getReference();
            int a = 0;
            int b = 1000;
            int c = 10;

            int random = 1 + (int) (Math.random() * c);

            int random_number2 = a + (int) (Math.random() * b);
            System.out.println("2-ое случайное число: " + random_number2);

            Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
                    .orderByChild("likes_count").startAt(random_number2 - random).endAt(random_number2 + random).limitToLast(1);

            mPostReference = imagesQuery.getRef();

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post post = dataSnapshot.getValue(Post.class);

                    if (post != null) {
                        mAuthorView.setText(post.author);
                        if (post.body != null)
                            if (!post.body.isEmpty()) {
                                mBodyView.setText(post.body);
                                mBodyView.setVisibility(View.VISIBLE);
                            } else mBodyView.setText("");
                        if (post.body != null)
                            if (!post.title.isEmpty()) {
                                mTitleView.setText(post.title);
                                mTitleView.setVisibility(View.VISIBLE);
                            } else mTitleView.setText("");
                        if (post.category != null)
                            if (!post.category.isEmpty()) {
                                categ.setText(post.category);
                                categ.setVisibility(View.VISIBLE);
                            } else categ.setText("");

                        likesCount.setText(String.valueOf(post.likes_count));
                        likeCount = post.likes_count;

                        if (post.likes.containsKey(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            like.setImageResource(R.drawable.ic_toggle_star_24);
                            likeActive = true;

                        } else {
                            like.setImageResource(R.drawable.ic_toggle_star_outline_24);
                            likeActive = false;
                        }

                        mPostKey = dataSnapshot.getKey();
                        postUID = post.uid;

                        datePost.setText(Convert.printDifference(post.create_date,
                                Calendar.getInstance().getTime().getTime(), getActivity()));
                        FirebaseDatabase.getInstance().getReference().child("users/" + post.uid + "/uriPhoto").
                                addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() != null) {
                                            String str = snapshot.getValue().toString();
                                            if (str != null && !str.isEmpty()) {
                                                GlideApp.with(Objects.requireNonNull(getActivity()))
                                                        .load(str)
                                                        .into(post_author_photo);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });


                        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                                .child("post-comments").child(mPostKey);

                        mAdapter = new RandomPostFragment.CommentAdapter(getActivity(), mCommentsReference);
                        mCommentsRecycler.setAdapter(mAdapter);

                    }
                    mStorageRef.child("images/" + mPostKey).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    GlideApp.with(Objects.requireNonNull(getActivity()))
                                            .load(uri)
                                            .into(iv_piture);

                                    uriPhoto = uri;
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Load", "" + e);

                        }
                    });

                    if (mPostListener != null)
                        mPostReference.removeEventListener(mPostListener);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            imagesQuery.addChildEventListener(childEventListener);

            mPostListener = childEventListener;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // mPostReference.addValueEventListener(postListener);
        // Keep copy of post listener so we can remove it when app stops

        // Listen for comments
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        randomMem();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.button_post_comment:
                String post = mCommentField.getText().toString().trim();
                if (!post.isEmpty())
                    postComment();
                else
                    Toast.makeText(getActivity(), R.string.enter_tour_comment, Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_add_picture:

                download.setIdle();

                if (mPostListener != null)
                    mPostReference.removeEventListener(mPostListener);
                if (mAdapter != null) mAdapter.cleanupListener();

                randomMem();

                break;


            case R.id.like:

                try {
                    if (!postUID.isEmpty()) {
                        DatabaseReference globalPostRef = FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey);
                        DatabaseReference userPostRef = FirebaseDatabase.getInstance().getReference().child("user-posts").child(postUID).child(mPostKey);
                        onLikeClicked(globalPostRef);
                        onLikeClicked(userPostRef);
                        if (likeActive) {
                            like.setImageResource(R.drawable.ic_toggle_star_outline_24);
                            likeActive = false;
                            likeCount--;
                            likesCount.setText(String.valueOf(likeCount));
                        } else {
                            likeCount++;
                            like.setImageResource(R.drawable.ic_toggle_star_24);
                            likesCount.setText(String.valueOf(likeCount));
                            likeActive = true;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;

            case R.id.share:
                shareImage();
                break;

            case R.id.download:

                /*
                final int a = 0; // Начальное значение диапазона - "от"
                final int b = 1000; // Конечное значение диапазона - "до"

                FirebaseDatabase.getInstance().getReference().child("posts")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    snapshot.getRef().runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            Post p = mutableData.getValue(Post.class);
                                            if (p != null) {

                                                int random_number2 = a + (int) (Math.random() * b); // Генерация 2-го числа
                                                System.out.println("2-ое случайное число: " + random_number2);

                                                p.likes_count = random_number2;
                                                mutableData.setValue(p);
                                            }
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b,
                                                               DataSnapshot dataSnapshot) {
                                            // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                                        }
                                    });


                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

*/
                saveMemas();
                break;

            default:
                break;
        }
    }


    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                if (p == null)
                    return Transaction.success(mutableData);

                if (p.likes.containsKey(id)) {
                    p.likes_count = p.likes_count - 1;
                    p.likes.remove(id);
                } else {
                    p.likes_count = p.likes_count + 1;
                    p.likes.put(id, true);
                }
                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }


    private void shareImage() {
        try {

            if (Build.VERSION.SDK_INT >= 23) {
                int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }

            }

            if (uriPhoto != null)
                FileLoader.with(getActivity())
                        .load(uriPhoto.toString(), true)
                        .fromDirectory(Environment.DIRECTORY_DOWNLOADS, FileLoader.DIR_EXTERNAL_PUBLIC)
                        .asFile(new FileRequestListener<File>() {
                            @Override
                            public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                                try {
                                    File loadedFile = response.getBody();
                                    boolean ileane = loadedFile.renameTo(new File(loadedFile.getPath() + ".jpg"));
                                    if (ileane) {
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("image/*");

                                        Uri uri = (FileProvider.getUriForFile(Objects.requireNonNull(getActivity()),
                                                Objects.requireNonNull(getActivity()).getPackageName() + ".fileProv",
                                                new File(loadedFile.getPath() + ".jpg")));

                                        share.putExtra(Intent.EXTRA_STREAM, uri);
                                        startActivity(Intent.createChooser(share, "Share Image!"));
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(FileLoadRequest request, Throwable t) {
                            }
                        });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveMemas() {

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }

        }

        download.setIndeterminate();
        try {
            Handler handler = new Handler();
            Runnable r = () -> {
                download.setProgress(50);
                if (uriPhoto != null)
                    FileLoader.with(getActivity())
                            .load(uriPhoto.toString(), true)
                            .fromDirectory(Environment.DIRECTORY_DOWNLOADS, FileLoader.DIR_EXTERNAL_PUBLIC)
                            .asFile(new FileRequestListener<File>() {
                                @Override
                                public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                                    File loadedFile = response.getBody();
                                    boolean ileane = loadedFile.renameTo(new File(loadedFile.getPath() + ".jpg"));
                                    if (ileane)
                                        Toast.makeText(getActivity(), "Файл загружен в Downloads/" + loadedFile.getName() + ".jpg",
                                                Toast.LENGTH_LONG).show();
                                    download.setFinish();
                                }

                                @Override
                                public void onError(FileLoadRequest request, Throwable t) {
                                }
                            });
            };
            handler.postDelayed(r, 300);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void postComment() {

        View view = Objects.requireNonNull(getActivity()).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        final String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String authorName = user.username;
                            // Create new comment object
                            String commentText = mCommentField.getText().toString();
                            Comment comment = new Comment(uid, authorName, commentText);
                            // Push the comment, it will appear in the list
                            mCommentsReference.push().setValue(comment);

                            // Clear the field
                            mCommentField.setText(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView authorView;
        TextView bodyView;
        ImageView comment_photo;

        CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
            comment_photo = itemView.findViewById(R.id.comment_photo);
        }
    }


    private static class CommentAdapter extends RecyclerView.Adapter<RandomPostFragment.CommentViewHolder> {

        private final Context mContext;
        private final DatabaseReference mDatabaseReference;
        private final ChildEventListener mChildEventListener;
        private final List<String> mCommentIds = new ArrayList<>();
        private final List<Comment> mComments = new ArrayList<>();


        CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mComments.set(commentIndex, newComment);
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                    String commentKey = dataSnapshot.getKey();
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @NonNull
        @Override
        public RandomPostFragment.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new RandomPostFragment.CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RandomPostFragment.CommentViewHolder holder, int position) {
            final Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            FirebaseDatabase.getInstance().getReference().child("users/" + comment.uid + "/uriPhoto").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                String str = snapshot.getValue().toString();
                                if (!str.isEmpty()) {
                                    GlideApp.with(mContext)
                                            .load(str)
                                            .into(holder.comment_photo);
                                }
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });


            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}
