package com.example.sameer.myapplication;


import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private TextToSpeech myTTS;
    private SpeechRecognizer mySpeechRecognizer;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        tv = findViewById(R.id.tv);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/trebucbd.ttf");
        tv.setTypeface(customFont);

        initializeTextToSpeech();
        initializeSpeechRecognizer();

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                tv.setText("Listening...");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                mySpeechRecognizer.startListening(intent);
            }
        });

    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle params) {
                }

                public void onBeginningOfSpeech() {
                }

                public void onRmsChanged(float rmsdB) {
                }

                public void onBufferReceived(byte[] buffer) {
                }

                public void onEndOfSpeech() {
                }

                public void onError(int error) {
                }

                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(results.get(0));
                }

                public void onPartialResults(Bundle partialResults) {
                }

                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    private void processResult(String command) {
        command = command.toLowerCase();
        tv.setText(command);
        if (command.indexOf("what") != -1) {
            if (command.indexOf("your name") != -1) {
                speak("my name is eL");
                tv.setText("my name is L");
            } else if (command.indexOf("time now") != -1) {
                Date now = new Date();
                String time = DateUtils.formatDateTime(this, now.getTime(), DateUtils.FORMAT_SHOW_TIME);
                speak("the time now is " + time);
                tv.setText("The time now is " + time);
            }

        } else if (command.indexOf("who") != -1) {
            String last = command.substring(6);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/" + last));
            startActivity(i);
        } else if (command.indexOf("alarm") != -1) {
            int min, hr;
            int value = Integer.parseInt(command.replaceAll("[^0-9]", ""));
            tv.setText(command);
            if (command.contains("o'clock")) {
                hr = value;
                min = 0;
                createAlarm(command, hr, min);
            } else if (value <= 23) {
                hr = value;
                min = 0;
                createAlarm(command, hr, min);
            } else {
                hr = value / 100;
                min = value % 100;
                createAlarm(command, hr, min);
            }

        } else if (command.indexOf("search for") != -1) {
            searchWeb(command);
        } else if (command.indexOf("call") != -1) {
            dialPhoneNumber(command);
        } else if (command.indexOf("where is") != -1) {
            String last = command.substring(8);
            String temp = "geo:0,0?q=" + (last.replaceAll("\\s", "+"));
            Uri location = Uri.parse(temp);
            showMap(location);
        } else if (command.indexOf("open camera") != -1) {
            int REQUEST_IMAGE_CAPTURE = 0;
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            speak("that command doesn't work");
        }
    }

    public void dialPhoneNumber(String command) {
        String phoneNumber = command.replaceAll("[^0-9]", "");
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(intent);
        }
    }


    public void createAlarm(String message, int hour, int minutes) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    public void searchWeb(String query) {
        int n = query.length();
        query = query.substring(11, n);
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    private void initializeTextToSpeech() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (myTTS.getEngines().size() == 0) {
                    Toast.makeText(MainActivity.this, "there is no text to speech in your engine", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    myTTS.setLanguage(Locale.US);
                    speak("hello! I am ready");

                }
            }
        });
    }

    private void speak(String message) {
        if (Build.VERSION.SDK_INT >= 21) {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
    }
}
