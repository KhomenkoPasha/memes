package com.memes.khom.mnews.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PostViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;
    private TextView authorView;
    private TextView numStarsView;
    private TextView bodyView;
    private TextView post_date;
    private TextView categ;
    public ImageView post_author_photo;
    public LinearLayout comments_lay;
    public ImageView iv_piture;
    public ImageView starView;

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
        post_author_photo = itemView.findViewById(R.id.post_author_photo);
        categ = itemView.findViewById(R.id.categ);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        categ.setText(String.format("Категория: %s", post.category));
        starView.setOnClickListener(starClickListener);
        try {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
            Date input = inputFormat.parse(post.create_date);
            String finaldate = inputFormat.format(input);
            post_date.setText(finaldate);
        }catch (Exception ex) {
          ex.printStackTrace();
        }

    }
}
