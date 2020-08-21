package com.depplenny.musico;

import java.util.ArrayList;

public class Song {
    /**
     * key:{name:"eg", url:"eg", timestampe:"eg", like:"eg", dislike:"eg"}
     */
    private String key;
    private String name;
    private String url;
    private String timestamp;
    private Long like;
    private Long dislike;

    public Song () {}

    public Song(String name, String url, String timestamp, Long like, Long dislike) {
        this.name = name;
        this.url = url;
        this.timestamp = timestamp;
        this.like = like;
        this.dislike = dislike;
    }

    public String getName() {
        return name;
    }
    public String getUrl() {
        return url;
    }
    public Long getDislike() {
        return dislike;
    }
    public Long getLike() {
        return like;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getKey() {return key;}

    public void setKey(String key) {
        this.key = key;
    }
}
