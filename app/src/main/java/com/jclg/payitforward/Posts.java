package com.jclg.payitforward;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class Posts implements Parcelable{

    // 13 members

    private String requester;
    private String tasker;

    private String id;
    private String title;
    private String description;

    private double latitude;
    private double longitude;

    private int status;
    //    OPEN = 1
    //    INPROGRESS = 2
    //    COMPLETE = 3
    //    INCOMPLETE = 4
    //    EXPIRED = 5

    private int category;
//     POST_INVALID = 0;
//     POST_OTHER = 1;
//     POST_CLUBS = 2;
//     POST_COURSES = 3;
//     POST_SCHOOLLIFE = 4;

    private boolean requester_reviewFlag;
    private boolean tasker_reviewFlag;

    private String[] offers = new String[PifApplication.OFFERS_MAX];
    private int offers_num;

    private Date due_date;


    Posts ( String postRequester, String postTasker,
            String postId, String postTitle, String postContent,
            double postLatitude, double postLongitude,
            int postStatus,
            int postCategory,
            boolean postRequester_ReivewFlag, boolean postTasker_ReivewFlag,
            String[] postOffers, int postOffers_num,
            Date postDueDate)
    {
        requester = postRequester;
        tasker = postTasker;

        id = postId;
        title = postTitle;
        description = postContent;

        latitude = postLatitude;
        longitude = postLongitude;

        status = postStatus;
        category = postCategory;

        requester_reviewFlag = postRequester_ReivewFlag;
        tasker_reviewFlag = postTasker_ReivewFlag;

        offers = postOffers;
        offers_num = postOffers_num;

        due_date = postDueDate;
    }

    // Template
//    public type get() { return member; }
//    public void set( type member) {this.member = member;}


    // Requester and Tasker
    public String getRequester() { return requester; }
    public void setRequester( String requester) {this.requester = requester;}

    public String getTasker() { return tasker; }
    public void setTasker( String tasker) {this.tasker = tasker;}

    // ID, Title, Description, Due Date
    public String getId() { return id; }
    public void setId(String id) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}
    public void setContent(String description) {this.description = description;}

    public Date getDueDate() {return due_date;}
    public void setDueDate(Date due_date) {this.due_date = due_date;}

    // Longitude and Latitude
    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    // Flags
    public int getPostStatus() {return status;}
    public void setPostStatus(int status) {this.status = status;}

    public int getPostCategory() {return category;}
    public void setPostCategory(int category) {this.category = category;}

    public boolean getRequester_Flag() {return requester_reviewFlag;}
    public void setRequester_Flag(boolean requester_flag) {this.requester_reviewFlag = requester_flag;}

    public boolean getTasker_Flag() {return tasker_reviewFlag;}
    public void setTasker_Flag(boolean tasker_flag) {this.tasker_reviewFlag = tasker_flag;}

    // String Array of Offers
    public String[] getOffers() {return offers;}
    public void setOffers(String[] offers) {this.offers = offers;}

    public int getOffersNums() { return offers_num; }
    public void setOffersNums( int offers_num) {this.offers_num = offers_num;}


    @Override
    public String toString() {return this.getTitle();}

    @Override
    public int describeContents() {return 0;}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(requester);
        dest.writeString(tasker);

        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);

        dest.writeDouble(latitude);
        dest.writeDouble(longitude);

        dest.writeInt(status);
        dest.writeInt(category);

        dest.writeInt( requester_reviewFlag ? 1 :0 );
        dest.writeInt(tasker_reviewFlag ? 1 : 0);

        dest.writeInt(offers_num);
        if (offers_num > 0)
            dest.writeStringArray(offers);

        dest.writeSerializable(due_date);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<Posts> CREATOR = new Parcelable.Creator<Posts>() {
        public Posts createFromParcel(Parcel pc) {
            return new Posts(pc);
        }
        public Posts[] newArray(int size) {
            return new Posts[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    public Posts(Parcel dest){
        requester = dest.readString();
        tasker = dest.readString();

        id = dest.readString();
        title = dest.readString();
        description = dest.readString();

        latitude = dest.readDouble();
        longitude = dest.readDouble();

        status = dest.readInt();
        category = dest.readInt();

        requester_reviewFlag = (dest.readInt() == 1);
        tasker_reviewFlag = (dest.readInt() == 1);

        offers_num = dest.readInt();
        if (offers_num > 0)
            dest.readStringArray(offers);
        else
            offers = null;

        due_date = (Date) dest.readSerializable();
    }
}
