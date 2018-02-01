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
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by xinlan on 2/17/2016.
 */
public class CommentsAdapter extends ArrayAdapter<String> {

    private final Activity context;

    private final List<String> UserName;
    private final List<String> text;

    public CommentsAdapter(Activity context, List<String> UserName, List<String> text) {
        super(context, R.layout.adapter_comment, UserName);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.UserName=UserName;
        this.text = text;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.adapter_comment, null, true);

        TextView comment_username = (TextView) rowView.findViewById(R.id.list_Comment_Name);
        TextView user_comment = (TextView) rowView.findViewById(R.id.list_Comment_body);
        ImageView user_picture = (ImageView) rowView.findViewById(R.id.list_Comment_image);

        comment_username.setText("");

        findProfileInParse(UserName.get(position), comment_username, user_picture);

        user_comment.setText(text.get(position));

//        // Open user profile on username item click
//        comment_username.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, ViewUserProfileActivity.class);
//                intent.putExtra("Uid", UserName.get(position));
//                context.startActivity(intent);
//            }
//        });

        // Open user profile on username item click
        user_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewUserProfileActivity.class);
                intent.putExtra("Uid", UserName.get(position));
                context.startActivity(intent);
            }
        });

        return rowView;
    };

    /*
        Parse Functions
     */

    private void findProfileInParse(String profileId_username,
                                    final TextView TextView_username,
                                    final ImageView ImageView_profilePic){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");

        // Retrieve the object by id
        query.getInBackground(profileId_username, new GetCallback<ParseObject>() {
            public void done(ParseObject profile, ParseException e) {
                if (e == null) {
                    System.out.println("CommentsAdapter::findProfileInParse - query success");
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
                            }
                            else {
                                Log.d("test", "There was a problem downloading the data.");
                            }
                        }
                    });
                }
                else
                    System.out.println("CommentsAdapter::findProfileInParse - query fail");
            }
        });
    }

}
