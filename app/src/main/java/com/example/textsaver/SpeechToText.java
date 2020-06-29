package com.example.textsaver;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class SpeechToText extends Activity {

    public static SpeechToText speechToText = new SpeechToText();
    ImageView speakNowButton;
    private final  int REQ_CODE =100;

    private SearchView inputText;

    public void SpeakToText(ImageView speakButton, SearchView inputTextString){
        this.speakNowButton = speakButton;

        inputText = inputTextString;

        speakNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH  );
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to Speak");
                try{
                    startActivityForResult(intent,REQ_CODE);
                    Log.e("Speech Text", "reached to start activity");
                }catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(), "Sorry Your Device is not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_CODE:{
                if(requestCode == RESULT_OK&& data!=null){
                    ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputText.setQuery((CharSequence) result.get(0),true);

                }
                break;
            }
        }

    }
}
