package com.memes.khom.mnews.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.memes.khom.memsnews.R;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.utils.Convert;

import java.util.Calendar;


public class PosTopViewHolder extends RecyclerView.ViewHolder {

    private final TextView titleView;
    private final TextView authorView;
    TextView numStarsView;
    private final TextView bodyView;
    private final TextView post_date;
    private final TextView categ;
    public TextView post_rate;
    public LinearLayout comments_lay;
    public ImageView iv_piture;
    ImageView likeView, share_img;
    RelativeLayout lvHeaderPost;
    public RelativeLayout youtube;
    DownloadButtonProgress downloadButton;
    public YouTubeThumbnailView youtube_thumbnail;

    PosTopViewHolder(View itemView) {
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
        post_rate = itemView.findViewById(R.id.post_rate);
        categ = itemView.findViewById(R.id.categ);
        youtube_thumbnail = itemView.findViewById(R.id.youtube_thumbnail);
        lvHeaderPost = itemView.findViewById(R.id.relativ_header);
        share_img = itemView.findViewById(R.id.share_img);
        downloadButton = itemView.findViewById(R.id.download);
    }

    void bindToPost(Post post, View.OnClickListener starClickListener, Context cnx) {
        titleView.setText(post.title);
        authorView.setText("Опубликовал - " + post.author);
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
