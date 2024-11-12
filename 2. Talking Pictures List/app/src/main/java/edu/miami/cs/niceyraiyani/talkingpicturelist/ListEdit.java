package edu.miami.cs.niceyraiyani.talkingpicturelist;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
public class ListEdit extends AppCompatActivity {
    private int imageID;
    private MediaRecorder voiceRecorder;
    private byte[] audioBytes;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // method specifically for editing each image recordings
        // EXTRA CREDIT
        Uri imageUri;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        audioBytes = null;
        imageID = getIntent().getIntExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.image_id",0);
        imageUri = getIntent().getParcelableExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.image_uri");
        ((ImageView)findViewById(R.id.pictures)).setImageURI(imageUri);
    }
    public void myClickHandler(View view) {

        Intent returnIntent;
        File audioFile;
        FileInputStream audioStream;
        String description;

        switch (view.getId()) {
            case R.id.record:
                try {
                    // starting the voice recording
                    voiceRecorder = new MediaRecorder();
                    voiceRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    voiceRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    voiceRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    voiceRecorder.setOutputFile(MainActivity.filename);
                    voiceRecorder.prepare();
                    voiceRecorder.start();
                } catch (IOException e) {
                    Toast.makeText(this,"Can nt record",Toast.LENGTH_LONG).show();                }
                break;
            case R.id.stop:
                // stop the voice recording
                voiceRecorder.stop();
                voiceRecorder.release();
                audioFile = new File(MainActivity.filename);
                audioBytes = new byte[(int)audioFile.length()];
                try {
                    audioStream = new FileInputStream(audioFile);
                    audioStream.read(audioBytes);
                    audioStream.close();
                } catch (IOException e) {
                    Toast.makeText(this,"Could not save recording",Toast.LENGTH_LONG).show();
                    audioBytes = null;
                }
                audioFile.delete();
                break;
            case R.id.save:
                // save the voice recording
                returnIntent = new Intent();
                description = ((EditText)findViewById(R.id.description)).getText().toString();
                Log.i("IN","Description is ==="+description+"=== and audio is "+(audioBytes==null? "null":"not null"));
                if (!description.isEmpty() || audioBytes != null) {
                    returnIntent.putExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.image_id",imageID);
                    returnIntent.putExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.description",
                            description);
                    returnIntent.putExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.recording",
                            audioBytes);
                    setResult(RESULT_OK,returnIntent);
                } else {
                    setResult(RESULT_CANCELED,returnIntent);
                }
                finish();
                break;
            default:
                break;
        }
    }
}