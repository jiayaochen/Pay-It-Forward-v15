package com.jclg.payitforward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by graceg on 2016-02-17.
 */
public class OffersAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<String> username = new ArrayList<String>();
    private Context context;
    private Posts post;

    public OffersAdapter(Context context, ArrayList<String> list, Posts post) {
        this.username = list;
        this.context = context;
        this.post = post;
    }

    @Override
    public int getCount() {
        return username.size();
    }

    @Override
    public Object getItem(int position) {
        return username.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.adapter_offers, null, true);

        /* Setup Views */

        // Setup TextView and display string from your username
        TextView offers_username = (TextView)rowView.findViewById(R.id.adapterOffers_textView_taskerName);
        offers_username.setText("");
        ImageView offers_picture = (ImageView) rowView.findViewById(R.id.adapterOffers_imageView_image);
        findProfileInParse(username.get(position), offers_username, offers_picture);

        // Setup buttons
        Button Accept_Button = (Button)rowView.findViewById(R.id.adapterOffers_button_accept);
        Button Decline_Button = (Button)rowView.findViewById(R.id.adapterOffers_button_decline);

        // Current User is a Tasker
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        if ( post.getPostStatus() == PostStatus.POST_INPROGRESS ||
                (post.getPostStatus() == PostStatus.POST_OPEN &&
                !post.getRequester().equals(profileId))) {
            Accept_Button.setVisibility(View.INVISIBLE);
            Decline_Button.setVisibility(View.INVISIBLE);
        }

        /* Setup OnClick Listeners */

//        // Open user profile on username item click
//        offers_username.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, ViewUserProfileActivity.class);
//                intent.putExtra("Uid", username.get(position));
//                context.startActivity(intent);
//            }
//        });

        // Open user profile on username item click
        offers_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewUserProfileActivity.class);
                intent.putExtra("Uid", username.get(position));
                context.startActivity(intent);
            }
        });

        // You have accepted the offer, all other offers are removed and handshake commenced
        Accept_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> clearAllUsers = new ArrayList<String>(username);

                acceptOfferInParse(clearAllUsers, position);

                username.clear();

                notifyDataSetChanged();

                ((Activity)context).finish();
            }
        });

        // You have declined the offer, remove this offer
        // TODO do we allow spam? do we allow the same user to offer again
        Decline_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeOfferInParse(username.get(position));

                username.remove(position);

                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    /*
        Parse Functions
     */

    private void acceptOfferInParse(final ArrayList removeAllUsers, final int position) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        // Retrieve the object by id
        query.getInBackground(post.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject updatePost, ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case we add a new user to the
                    // offers array.
                    updatePost.put("Tasker", removeAllUsers.get(position));
                    updatePost.put("PostStatus", PostStatus.POST_INPROGRESS);
                    updatePost.put("OffersNum", 0);
                    updatePost.removeAll("Offers", removeAllUsers);
                    updatePost.saveInBackground();

                    Toast.makeText(context, "Offer Accepted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeOfferInParse(final String removeUser) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        // Retrieve the object by id
        query.getInBackground(post.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject updatePost, ParseException e) {
                if (e == null) {
                    ArrayList<String> temp = new ArrayList<String>();
                    temp.add(removeUser);

                    // Update number of offers
                    int updateOffersNum = updatePost.getInt("OffersNum") - 1;

                    assert(updateOffersNum>=0);

                    // Remove this offer from the offers array
                    updatePost.put("OffersNum", updateOffersNum);
                    updatePost.removeAll("Offers", temp);
                    updatePost.saveInBackground();

                    Toast.makeText(context, "Offer Declined", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findProfileInParse(String profileId_username,
                                    final TextView TextView_username,
                                    final ImageView ImageView_profilePic){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");

        // Retrieve the object by id
        query.getInBackground(profileId_username, new GetCallback<ParseObject>() {
            public void done(ParseObject profile, ParseException e) {
                if (e == null) {
                    System.out.println("OffersAdapter::findProfileInParse query success");
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
                    System.out.println("OffersAdapter::findProfileInParse - query fail");
            }
        });
    }
}
