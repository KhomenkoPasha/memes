package com.memes.khom.mnews.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;


public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public ImageView iv_piture;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;
    public TextView post_date;
    public LinearLayout comments_lay;

    public PostViewHolder(View itemView) {
        super(itemView);


        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        iv_piture = itemView.findViewById(R.id.iv_piture);
        bodyView = itemView.findViewById(R.id.post_body);
        comments_lay = itemView.findViewById(R.id.comments_lay);
        post_date = itemView.findViewById(R.id.post_date);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        post_date.setText(post.create_date);
        starView.setOnClickListener(starClickListener);
    }
}
