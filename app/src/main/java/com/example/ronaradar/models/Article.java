package com.example.ronaradar.models;

public class Article {
    public String title;
    public String byline;
    public String article_abstract;
    public String url;
    public String thumbnail;
    public String published_date;

    public Article(String title, String byline, String article_abstract, String url, String thumbnail,
                   String published_date) {
        this.title = title;
        this.byline = byline;
        this.article_abstract = article_abstract;
        this.url = url;
        this.thumbnail = thumbnail;
        this.published_date = published_date;
    }
}
