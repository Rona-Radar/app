package com.example.ronaradar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.ronaradar.models.Article;
import com.example.ronaradar.models.GsonArrayRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class FeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        // logic goes here
        String url = "https://rona-radar-310320.uk.r.appspot.com/";
        RelativeLayout feedLayout = findViewById(R.id.feed_layout);

        RequestQueue queue = Volley.newRequestQueue(this);
        GsonArrayRequest<Article> gsonArrayRequest = new GsonArrayRequest<>(url, Article.class, null,
                (articleArrayList) -> {
                    articleArrayList.forEach((article) -> {
                        MaterialCardView cardView = new MaterialCardView(new ContextThemeWrapper(this, R.style.CardViewStyle));
                        RelativeLayout relativeLayout = new RelativeLayout(new ContextThemeWrapper(this, R.style.CardContent));
                        TextView title = new TextView(new ContextThemeWrapper(this, R.style.TextViewTitleStyle));
                        title.setText(article.title);
                        TextView article_abstract = new TextView(new ContextThemeWrapper(this, R.style.TextViewBodyStyle));
                        article_abstract.setText(article.article_abstract);
                        feedLayout.addView(cardView);
                        cardView.addView(relativeLayout);
                        relativeLayout.addView(title);
                        relativeLayout.addView(article_abstract);
                    });
                }, (error) -> {
                    Log.e("FeedActivity", "Oh no", error);
        });
        queue.add(gsonArrayRequest);

        progressBar.setVisibility(View.INVISIBLE);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.feed);
        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.feed:
                    return true;
                case R.id.maps:
                    startActivity(new Intent(this, MapsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }
}