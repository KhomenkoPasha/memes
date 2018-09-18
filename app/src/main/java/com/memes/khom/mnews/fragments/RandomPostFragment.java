package com.memes.khom.mnews.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Comment;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.models.User;
import com.memes.khom.mnews.utils.Convert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.rilixtech.materialfancybutton.MaterialFancyButton.TAG;


public class RandomPostFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private RandomPostFragment.CommentAdapter mAdapter;
    private StorageReference mStorageRef;
    EmojIconActions emojIcon;
    private TextView mAuthorView;
    private TextView datePost;
    private ImageView post_author_photo;
    private TextView mTitleView;
    private TextView mBodyView, categ;
    private ImageView iv_piture;
    private EmojiconEditText mCommentField;
    ImageView emojiButton;
    private RecyclerView mCommentsRecycler;

    public RandomPostFragment() {
        // Required empty public constructor
    }

    public static RandomPostFragment newInstance() {
        RandomPostFragment fragment = new RandomPostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
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
        LinearLayout linearLayoutCard = rootView.findViewById(R.id.linearLayoutInfo);
        mCommentButton.setOnClickListener(this);

        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getActivity(), R.drawable.devider)));
        mCommentsRecycler.addItemDecoration(itemDecorator);

        emojiButton = rootView.findViewById(R.id.emoji_btn);
        emojIcon = new EmojIconActions(getActivity(), rootView.findViewById(R.id.comment_form), mCommentField, emojiButton);
        emojIcon.ShowEmojIcon();
        emojIcon.setUseSystemEmoji(false);

        mAdapter = new RandomPostFragment.CommentAdapter(getActivity(), mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            mPostKey = "-LMcb3AZXxIb0PcIfe1R";
            if (mPostKey == null) {
                throw new IllegalArgumentException("L6X0chkIXdFu0SAn2WP");
            }
            mStorageRef = FirebaseStorage.getInstance().getReference();
            // Initialize Database
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    Post post = dataSnapshot.getValue(Post.class);
                    // [START_EXCLUDE]
                    if (post != null) {
                        mAuthorView.setText(post.author);

                        if (!post.body.isEmpty()) {
                            mBodyView.setText(post.body);
                            mBodyView.setVisibility(View.VISIBLE);
                        }

                        if (!post.title.isEmpty()) {
                            mTitleView.setText(post.title);
                            mTitleView.setVisibility(View.VISIBLE);
                        }
                        if (!post.category.isEmpty()) {
                            categ.setText(post.category);
                            categ.setVisibility(View.VISIBLE);
                        }


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

                    }
                    mStorageRef.child("images/" + mPostKey).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    GlideApp.with(Objects.requireNonNull(getActivity()))
                                            .load(uri)
                                            .into(iv_piture);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Load", "" + e);

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Getting Post failed, log a message
                }
            };

            mPostReference.addValueEventListener(postListener);
            // Keep copy of post listener so we can remove it when app stops
            mPostListener = postListener;
            // Listen for comments

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            default:
                break;
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

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;
        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();


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

                    // [START_EXCLUDE]
                    // Update RecyclerView
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
                                if (str != null && !str.isEmpty()) {
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
