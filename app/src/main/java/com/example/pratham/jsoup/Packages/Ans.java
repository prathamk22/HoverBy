package com.example.pratham.jsoup.Packages;

public class Ans {
    public String Time, ans,user,upvote,downvote,id,pic,ansId;

    public Ans(String time, String ans,String user,String upvote,String downvote,String id,String pic,String ansId) {
        Time = time;
        this.id = id;
        this.ans = ans;
        this.user = user;
        this.upvote = upvote;
        this.downvote = downvote;
        this.pic = pic;
        this.ansId = ansId;
    }
}
