package com.jclg.payitforward;

/**
 * Created by Jeff on 2/20/2016.
 */
public class Review {
    private String id;

    private String requester; // Used to determine if reviewer/reviewee is requester or tasker
    private String title;
    private String description;
    private boolean isComplete;

    private float rating;

    private String reviewer;
    private String reviewee;


    Review( String id,

            String requester, // Used to determine if reviewer/reviewee is requester or tasker
            String title,
            String description,
            boolean isComplete,

            float rating,

            String reviewer,
            String reviewee )
    {
        this.id = id;

        this.requester = requester; // Used to determine if reviewer/reviewee is requester or tasker
        this.title = title;
        this.description = description;
        this.isComplete = isComplete;

        this.rating = rating;

        this.reviewer = reviewer;
        this.reviewee = reviewee;
    }

    public String getId() { return id; }
    public void setId( String id) {this.id = id;}

    public String getRequester() { return requester; }
    public void setRequester( String requester) {this.requester = requester;}

    public String getTitle() { return title; }
    public void setTitle( String title) {this.title = title;}

    public String getDescription() { return description; }
    public void setDescription( String description) {this.description = description;}

    public String getReviewer() { return reviewer; }
    public void setReviewer( String reviewer) {this.reviewer = reviewer;}

    public String getReviewee() { return reviewee; }
    public void setReviewee( String reviewee) {this.reviewee = reviewee;}

    public boolean getIsComplete() { return isComplete; }
    public void setIsComplete( boolean isComplete) {this.isComplete = isComplete;}

    public float getRating() { return rating; }
    public void setRating( float rating) {this.rating = rating;}

}
