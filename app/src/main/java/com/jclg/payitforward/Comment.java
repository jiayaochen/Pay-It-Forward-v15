package com.jclg.payitforward;

/**
 * Created by xinlan on 2/17/2016.
 */
public class Comment {
    private String Userid;
    private String Objectid;
    private String Text;
    private String Parentid;


    Comment(String userid, String objectid, String text, String parentid) {
        Userid = userid;
        Objectid = objectid;
        Text = text;
        Parentid = parentid;
    }

    public String getUserid() {
        return Userid;
    }
    public void setUserid(String userid) {
        this.Userid = userid;
    }
    public String getObjectid() {
        return Objectid;
    }
    public void setObjectid(String objectid) {
        this.Objectid = objectid;
    }
    public String getText() {
        return Text;
    }
    public void setText(String text) {
        this.Text = text;
    }
    public String getParentid() {
        return Parentid;
    }
    public void setParentid(String parentid) {
        this.Parentid = parentid;
    }

}




