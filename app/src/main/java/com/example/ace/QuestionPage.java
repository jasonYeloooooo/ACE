package com.example.ace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ace.adapters.ChatAdapter;
import com.example.ace.helpers.SendMessageInBg;
import com.example.ace.interfaces.BotReply;
import com.example.ace.models.Message;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QuestionPage extends AppCompatActivity implements BotReply {
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;
    TextToSpeech textToSpeech;
//    ImageView btnBack,btnChinese,btnEnglish;
    String myLanguage= "en_US";
    private Button btnStart;

    //dialogFlow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private  String uuid = UUID.randomUUID().toString();
    private String TAG = "mainactivity";

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private ImageView micButton;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_page);

        chatView  = findViewById(R.id.chatViewQuestion);
        btnStart = findViewById(R.id.btnQuestionStart);
        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);
        micButton = findViewById(R.id.btnQuestionMic);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToBot("askQuestion");
            }
        });

        //bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottomQuestion);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottomQuestion:
                        return true;
                    case R.id.bottomMain:
                        startActivity(new Intent(QuestionPage.this,SecondActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.bottomSetting:
                        startActivity(new Intent(QuestionPage.this,SettingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        //tts initial
        textToSpeech = new TextToSpeech(this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status==TextToSpeech.SUCCESS){
                            if(myLanguage== "en_US")  {int language = textToSpeech.setLanguage(Locale.ENGLISH);}
                            if(myLanguage == "zh-CN") {int language = textToSpeech.setLanguage(Locale.CHINESE);}
                        }
                    }
                });


        setUpBot();
    }

    private void setUpBot(){
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId =  ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            Log.d(TAG, "projectId: " + projectId);

        }catch (Exception e){
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse != null){
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            if(!botReply.isEmpty()){
                textToSpeech.speak(botReply, textToSpeech.QUEUE_FLUSH,null);
                messageList.add(new Message(botReply, true));
                chatAdapter.notifyDataSetChanged();
                chatView.getLayoutManager().scrollToPosition(messageList.size()-1);
            }else{
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "failed to connect", Toast.LENGTH_SHORT).show();
        }
       // Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                speechRecognizer.startListening(speechRecognizerIntent);
//            }
//        },3000);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

}