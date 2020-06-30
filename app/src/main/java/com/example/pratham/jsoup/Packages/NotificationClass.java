package com.example.pratham.jsoup.Packages;

import java.io.Serializable;

public class NotificationClass implements Serializable {
    private String Time,upvote,downvote,ProfilePic,Name,Ans;

    public NotificationClass() {
    }

    public String getTime() {
        return Time;
    }

    public String getUpvote() {
        return upvote;
    }

    public String getDownvote() {
        return downvote;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public String getName() {
        return Name;
    }

    public String getAns() {
        return Ans;
    }

    public void setTime(String time) {
        Time = time;
    }

    public void setUpvote(String upvote) {
        this.upvote = upvote;
    }

    public void setDownvote(String downvote) {
        this.downvote = downvote;
    }

    public void setProfilePic(String ProfilePic) {
        this.ProfilePic = ProfilePic;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setAns(String Ans) {
        this.Ans = Ans;
    }
}
