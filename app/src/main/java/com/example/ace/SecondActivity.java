package com.example.ace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.Objects;
import java.util.UUID;

public class SecondActivity extends AppCompatActivity implements BotReply {
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;
    TextToSpeech textToSpeech;
    ImageView btnBack,btnChinese,btnEnglish;
    public static String myLanguage= "en_US";
    public boolean isMicOn = false;

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
        setContentView(R.layout.activity_second);
        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);
        micButton = findViewById(R.id.imageBtn2);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        btnBack = findViewById(R.id.ivBack);
        btnChinese = findViewById(R.id.ivChinese);
        btnEnglish = findViewById(R.id.ivEnglish);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottomMain);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottomQuestion:
                        textToSpeech.stop();
                        speechRecognizer.destroy();
                        startActivity(new Intent(SecondActivity.this,QuestionPage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.bottomMain:
                        return true;
                    case R.id.bottomSetting:
                        textToSpeech.stop();
                        speechRecognizer.destroy();
                        startActivity(new Intent(SecondActivity.this,SettingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        //touch function on btn Back
        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent=new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
                return false;
            }
        });

        //change the language
        btnChinese.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Toast.makeText(SecondActivity.this,"change to chinese",Toast.LENGTH_SHORT).show();
                myLanguage= "zh-CN";
                int language = textToSpeech.setLanguage(Locale.CHINESE);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguage);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguage);

                return false;
            }
        });
        //change to EN
        btnEnglish.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Toast.makeText(SecondActivity.this,"change to English",Toast.LENGTH_SHORT).show();
                myLanguage= "en_US";
                textToSpeech.setLanguage(Locale.ENGLISH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguage);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguage);
                return false;
            }
        });

        //tts initial
        textToSpeech = new TextToSpeech(this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status==TextToSpeech.SUCCESS){
                            if(myLanguage == "en_US")  {int language = textToSpeech.setLanguage(Locale.ENGLISH);}
                            if(myLanguage == "zh-CN") {int language = textToSpeech.setLanguage(Locale.CHINESE);}
                        }
                    }
                });

        //check the mic is good?
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
       // speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguage);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguage);
      //  speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editMessage.setText("");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editMessage.setText(data.get(0));
                String message = editMessage.getText().toString();
                if (!message.isEmpty()){
                    messageList.add(new Message(message,false));
                    editMessage.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size()-1);
                }else{
                    Toast.makeText(SecondActivity.this, "Please enter text", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMicOn = !isMicOn;
                if (isMicOn){
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);

                }else{
                    speechRecognizer.stopListening();
                    micButton.setImageResource(R.drawable.ic_mic_black_off);
                }
            }
        });

/*

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                    micButton.setImageResource(R.drawable.ic_mic_black_off);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }

                return false;
            }
        });
*/


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()){
                    messageList.add(new Message(message,false));
                    editMessage.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size()-1);
                }else{
                    Toast.makeText(SecondActivity.this, "Please enter text", Toast.LENGTH_SHORT).show();
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
        QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(message).setLanguageCode(myLanguage)).build();

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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speechRecognizer.startListening(speechRecognizerIntent);
                micButton.setImageResource(R.drawable.ic_mic_black_24dp);
            }
        },3000);
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