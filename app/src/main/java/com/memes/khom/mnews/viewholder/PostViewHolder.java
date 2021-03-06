package com.memes.khom.mnews.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.utils.Convert;

import java.util.Calendar;


public class PostViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;
    private TextView authorView;
    public TextView numStarsView;
    private TextView bodyView;
    private TextView post_date;
    private TextView categ;
    public ImageView post_author_photo;
    public LinearLayout comments_lay;
    public ImageView iv_piture;
    public ImageView likeView,share_img;
    public RelativeLayout lvHeaderPost;
    public RelativeLayout youtube;
    public DownloadButtonProgress downloadButton;
    public YouTubeThumbnailView youtube_thumbnail;

    PostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        likeView = itemView.findViewById(R.id.like);
        youtube = itemView.findViewById(R.id.youtube);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        iv_piture = itemView.findViewById(R.id.iv_piture);
        bodyView = itemView.findViewById(R.id.post_body);
        comments_lay = itemView.findViewById(R.id.comments_lay);
        post_date = itemView.findViewById(R.id.post_date);
        post_author_photo = itemView.findViewById(R.id.post_author_photo);
        categ = itemView.findViewById(R.id.categ);
        youtube_thumbnail = itemView.findViewById(R.id.youtube_thumbnail);
        lvHeaderPost = itemView.findViewById(R.id.relativ_header);
        share_img  = itemView.findViewById(R.id.share_img);
        downloadButton = itemView.findViewById(R.id.download);
    }

    void bindToPost(Post post, View.OnClickListener starClickListener, Context cnx) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.likes_count));

        if (post.body != null && !post.body.isEmpty()) {
            bodyView.setText(post.body);
            bodyView.setVisibility(View.VISIBLE);
        }
        post_date.setText(Convert.printDifference(post.create_date,
                Calendar.getInstance().getTime().getTime(), cnx));
        categ.setText(post.category);
        likeView.setOnClickListener(starClickListener);
    }
}
