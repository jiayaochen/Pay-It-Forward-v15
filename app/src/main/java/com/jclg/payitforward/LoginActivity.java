package com.jclg.payitforward;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
	
	protected EditText usernameEditText;
	protected EditText passwordEditText;
	protected Button loginButton;
	
	protected TextView signUpTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);
		
		signUpTextView = (TextView)findViewById(R.id.login_textView_signup);
		usernameEditText = (EditText)findViewById(R.id.User_editText_username);
		passwordEditText = (EditText)findViewById(R.id.User_editText_password);
		loginButton = (Button)findViewById(R.id.login_button_login);
		
		signUpTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
			startActivity(intent);
			}
		});
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				
				username = username.trim();
				password = password.trim();

				if (username.isEmpty() || password.isEmpty()) {
                    // Error - Username or Password not filled
					AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
					builder.setMessage(R.string.login_error_message)
						.setTitle(R.string.login_error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				else {
                    // Username and Password Obtained

					setProgressBarIndeterminateVisibility(true);
					
					ParseUser.logInInBackground(username, password, new LogInCallback() {
						@Override
						public void done(ParseUser user, ParseException e) {
                            // Check login successful

                            setProgressBarIndeterminateVisibility(false);

                            if (e == null) {
                                // Successful login

                                boolean isverified = user.getBoolean("emailVerified");
                                if (isverified){
                                    // Successful email verfication

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                else {
                                    // Failed email verfication

                                    Toast.makeText(getApplicationContext(), "Please Verify ur email", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                // Failed login
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.login_error_title)
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
}
