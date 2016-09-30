package com.example.administrator.bignamemusic20;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Music {
    private String musicname ;
    private String musicimage;
    public Music(){}

    public Music(String musicname) {
        this.musicname = musicname;
    }

    public Music(String musicname, String musicimage) {
        this.musicname = musicname;
        this.musicimage = musicimage;
    }

    public String getMusicname() {
        return musicname;
    }

    public void setMusicname(String musicname) {
        this.musicname = musicname;
    }

    public String getMusicimage() {
        return musicimage;
    }

    public void setMusicimage(String musicimage) {
        this.musicimage = musicimage;
    }
}
