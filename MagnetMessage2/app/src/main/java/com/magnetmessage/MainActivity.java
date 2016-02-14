package com.magnetmessage;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.User;
import com.magnet.max.android.auth.model.UserRegistrationInfo;
import com.magnet.mmx.client.api.ListResult;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXMessage;
import com.magnet.mmx.protocol.MMXChannel;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String FIRST_NAME = "Sender_fName";
    private static final String LAST_NAME = "Sender_lName";
    private static final String USERNAME = "fName.lName@gmail.com";
    private static final String PASSWORD = "password";

    private static final String MESSAGE_PAYLOAD_KEY = "message";
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String CHANNEL_NAME = "GetStarted";
    private MMXChannel myChatChannel;
    /**
     * The EditText to input message content
     */
    private EditText etMessageContent;
    /**
     * The Button to send message
     */
    private Button btnSend;

    private EventListener messageEventListener = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            showAlertDialog("Received message", mmxMessage.getContent().get(MESSAGE_PAYLOAD_KEY));
            return false;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIXME : replace the resource id of the EditText
        etMessageContent = (EditText) findViewById(R.id.etMessageContent);
        //FIXME : replace the resource id of the Button
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setEnabled(false);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sendMessage();
            }
        });

        registerAndLogin(USERNAME, PASSWORD);
    }

    @Override protected void onResume() {
        super.onResume();

        //Register the message event listener to receive message in this Activity
        MMX.registerListener(messageEventListener);
    }

    @Override protected void onStop() {
        super.onStop();

        //Unregister the message event listener
        MMX.unregisterListener(messageEventListener);
    }

    private void registerAndLogin(final String username, final String password) {
        UserRegistrationInfo userInfo = new UserRegistrationInfo.Builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .userName(username)
                .password(password)
                .build();

        User.register(userInfo, new ApiCallback<User>() {
            @Override
            public void success(User user) {
                login(username, password);
                showToastMessage("User registered");
            }

            @Override
            public void failure(ApiError apiError) {
                if (apiError.getKind() == 409) {
                    //User already exist, login
                    login(username, password);
                } else {
                    showAlertDialog("Error", "Failed to register user due to : " + apiError);
                }
            }
        });
    }

    private void sendMessage() {
        if(null == myChatChannel) {
            showAlertDialog("Error", "Chat is not ready");
            return;
        }

        Map<String, String> payload = new HashMap<String, String>();
        payload.put(MESSAGE_PAYLOAD_KEY, etMessageContent.getText().toString());
        MMXMessage message = new MMXMessage.Builder()
                .content(payload)
                .build();

        myChatChannel.publish(message, new MMXChannel.OnFinishedListener<String>() {
            @Override public void onSuccess(String s) {
                showToastMessage("Message sent");
            }

            @Override public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Log.e(TAG, "Failed to send message", throwable);
                showAlertDialog("Error", "Failed to send message");
            }
        });
    }

    private void login(String username, String password) {
        User.login(username, password, false, new ApiCallback<Boolean>() {
            @Override public void success(Boolean aBoolean) {
                showToastMessage("User login");


                MMX.start();


                findChannel();
            }

            @Override public void failure(ApiError apiError) {
                Log.e(TAG, "Failed to login", apiError);
                showAlertDialog("Error", "Failed to login due to " + apiError);
            }
        });
    }

    private void findChannel() {
        //Find the channel pre-created by server
        MMXChannel.findPublicChannelsByName(CHANNEL_NAME, 1, 0, new MMXChannel.OnFinishedListener<ListResult<MMXChannel>>() {
            @Override public void onSuccess(ListResult<MMXChannel> mmxChannelListResult) {
                if(null != mmxChannelListResult.items && !mmxChannelListResult.items.isEmpty()) {
                    //Find the channel
                    myChatChannel = mmxChannelListResult.items.get(0);

                    //Subscribe to the channel to join the chat
                    myChatChannel.subscribe(new MMXChannel.OnFinishedListener<String>() {
                        @Override public void onSuccess(String s) {
                            //Enable sending message
                            btnSend.setEnabled(true);

                            showToastMessage("Join chat");
                        }

                        @Override
                        public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                            Log.e(TAG, failureCode.toString(), throwable);
                            showAlertDialog("Error", "Failed to join the chat");
                        }
                    });
                } else {
                    showAlertDialog("Error", "Couldn't find chat channel");
                }
            }

            @Override public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Log.e(TAG, failureCode.toString(), throwable);
                showAlertDialog("Error", "Couldn't find chat channel");
            }
        });
    }

    private void showToastMessage(String message) {
        Log.d(TAG, message);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setMessage(message).setCancelable(true);
        if(null != title) {
            builder.setTitle(title);
        }
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}
