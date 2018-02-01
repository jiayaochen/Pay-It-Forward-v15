package com.jclg.payitforward;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by graceg on 2016-03-13.
 */
public class ReviewAdapter extends ArrayAdapter<String> {

    private final Activity context;

    private final List<String> title;
    private final List<String> username;
    private final List<String> text;
    private final List<Float> rating;

//    private TextView review_username;

    public ReviewAdapter(Activity context, List<String> title, List<String> UserName, List<String> text, List<Float> rating) {
        super(context, R.layout.adapter_comment, UserName);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.title = title;
        this.username = UserName;
        this.text = text;
        this.rating = rating;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.adapter_review, null, true);

        TextView review_username = (TextView) rowView.findViewById(R.id.adapterReview_textView_username);
        review_username.setText("");
        ImageView review_picture = (ImageView) rowView.findViewById(R.id.adapterReview_ImageView_image);
        findProfileInParse(username.get(position), review_username, review_picture);

        review_username.setVisibility(View.GONE);

        TextView usertitle = (TextView) rowView.findViewById(R.id.adapterReview_textView_title);
        TextView userreview = (TextView) rowView.findViewById(R.id.adapterReview_textView_description);
        RatingBar userrating = (RatingBar) rowView.findViewById(R.id.adapterReview_ratingBar_starRating);

        usertitle.setText(title.get(position));
        userreview.setText(text.get(position));
        userrating.setRating(rating.get(position));

        userrating.setStepSize(0.1f);

        // Open user profile on list item click
        review_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewUserProfileActivity.class);
                intent.putExtra("Uid", username.get(position));
                context.startActivity(intent);
            }
        });

        return rowView;
    };

    private void findProfileInParse(String profileId_username,
                                    final TextView TextView_username,
                                    final ImageView ImageView_profilePic){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");

        // Retrieve the object by id
        query.getInBackground(profileId_username, new GetCallback<ParseObject>() {
            public void done(ParseObject profile, ParseException e) {
                if (e == null) {
                    System.out.println("ReviewAdapter::findProfileInParse query success");
                    TextView_username.setText(profile.getString("username"));

                    ParseFile file = (ParseFile) profile.get("profilePicture");
                    System.out.println("file: " + file);

                    file.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                // Decode the Byte[] into Bitmap
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                int x = 10000;
                                while (x-- > 0) ;
                                ImageView_profilePic.setImageBitmap(bmp);
                            } else {
                                Log.d("test", "There was a problem downloading the data.");
                            }
                        }
                    });
                } else
                    System.out.println("ReviewAdapter::findProfileInParse - query fail");
            }
        });
    }

}
