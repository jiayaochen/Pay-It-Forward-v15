package com.jclg.payitforward;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class SignUpActivity extends AppCompatActivity {
	
	protected EditText usernameEditText;
	protected EditText passwordEditText;
	protected EditText firstNameEditText;
	protected EditText lastNameEditText;
	protected Button signUpButton;
	protected String emailDomainString;
	protected Spinner emailDomains;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		/* Setup Views */

		usernameEditText = (EditText)findViewById(R.id.User_editText_username);
		passwordEditText = (EditText)findViewById(R.id.User_editText_password);
		firstNameEditText = (EditText)findViewById(R.id.signup_editText_firstName);
		lastNameEditText = (EditText)findViewById(R.id.signup_editText_lastName);
		signUpButton = (Button)findViewById(R.id.signup_button_signup);

		// Setup email domain spinner
		emailDomains = (Spinner)findViewById(R.id.signup_spinner_emailDomain);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.signup_stringArray_emailDomain, android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		emailDomains.setAdapter(adapter);

		/* Setup On Click Listener */

		emailDomains.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Object s = parent.getItemAtPosition(position);
				emailDomainString = s.toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//System.out.println("nothing selected :");
			}
		});

		signUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String firstName = firstNameEditText.getText().toString();
				String lastName = lastNameEditText.getText().toString();
				String email;

				username = username.trim();
				password = password.trim();
				firstName = firstName.trim();
				lastName = lastName.trim();
				if (username.isEmpty() || password.isEmpty() || firstName.isEmpty()|| lastName.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
					builder.setMessage(R.string.signup_error_message)
						.setTitle(R.string.signup_error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				else {
					setProgressBarIndeterminateVisibility(true);
					email = firstName.toLowerCase() + "." + lastName.toLowerCase() + emailDomainString;
					final ParseObject profile = new ParseObject("Profile");
					ParseUser newUser = new ParseUser();

					profile.put("username", username);
					profile.put("avgRating", 0);
					profile.put("bibliography", "");
					profile.saveInBackground(new SaveCallback() {
						public void done(ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							if (e == null) {
								System.out.println(profile + " saved");
							} else {
								// The post failed.
								Toast.makeText(getApplicationContext(), "Failed to Post", Toast.LENGTH_SHORT).show();
								Log.d(getClass().getSimpleName(), "User update error: " + e);
							}
						}
					});

					newUser.setUsername(username);
					newUser.setPassword(password);
					newUser.setEmail(email);
					newUser.put("profileId", profile.getObjectId());
					newUser.signUpInBackground(new SignUpCallback() {
						@Override
						public void done(ParseException e) {
							setProgressBarIndeterminateVisibility(false);

							if (e == null) {
								// Set up Default Profile Picture
								Drawable defaultPic;
								Random random = new Random();
								int randomnum = showRandomInteger(1, 6, random);

								switch (randomnum) {
									case 1:
										defaultPic = getResources().getDrawable(R.drawable.female_one);
										break;
									case 2:
										defaultPic = getResources().getDrawable(R.drawable.female_two);
										break;
									case 3:
										defaultPic = getResources().getDrawable(R.drawable.female_three);
										break;
									case 4:
										defaultPic = getResources().getDrawable(R.drawable.male_one);
										break;
									case 5:
										defaultPic = getResources().getDrawable(R.drawable.male_two);
										break;
									case 6:
										defaultPic = getResources().getDrawable(R.drawable.male_three);
										break;
									default:
										defaultPic = getResources().getDrawable(R.drawable.female_one);
										break;
								}

								assert ((BitmapDrawable) defaultPic) != null;
								Bitmap bitmap = ((BitmapDrawable) defaultPic).getBitmap();
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
								byte[] profilepic = stream.toByteArray();

								// Create the ParseFile
								ParseFile file = new ParseFile("default.png", profilepic);
								// Upload the image into Parse Cloud
								file.saveInBackground();
								profile.put("profilePicture", file);
								profile.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											// Success!
											Toast.makeText(SignUpActivity.this, "profile pic saved to profile", Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(SignUpActivity.this, "wrong...", Toast.LENGTH_SHORT).show();
										}
									}
								});

								Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
								startActivity(intent);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
								builder.setMessage(e.getMessage())
										.setTitle(R.string.signup_error_title)
										.setPositiveButton(android.R.string.ok, null);
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
					});
				}
			}
		});
	}

	private int showRandomInteger(int aStart, int aEnd, Random aRandom){
		if (aStart > aEnd) {
			throw new IllegalArgumentException("Start cannot exceed End.");
		}
		//get the range, casting to long to avoid overflow problems
		long range = (long)aEnd - (long)aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long)(range * aRandom.nextDouble());
		int randomNumber =  (int)(fraction + aStart);
		return randomNumber;
	}
}
