package edu.miami.cs.niceyraiyani.talkingpicturelist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SimpleAdapter.ViewBinder, MediaPlayer.OnCompletionListener, TextToSpeech.OnInitListener,
        ListDialog.StopTalking {

    // Declaring all the global variables
    public static final String DATABASE_NAME = "TalkingPictures.db";

    private MediaPlayer songPlay = null;
    private MediaPlayer voicePlay = null;
    private TextToSpeech textPlay = null;
    private ListDB talkingPicturesDB;
    private Cursor cursorImg = null;


    private byte[] voice = null;
    public static String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissions.launch(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO});
    }
    private ActivityResultLauncher<String[]> getPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> results) {

                    for (String key:results.keySet()) {
                        if (!results.get(key)) {
                            goOnCreating(false);
                        }
                    }
                    goOnCreating(true);
                }
            });
    private void goOnCreating(boolean havePermission) {

        // Sets the UI and then builds the database, setting up the app
        if (havePermission) {
            setContentView(R.layout.activity_main);
            songPlay = randomSong();
            talkingPicturesDB = Room.databaseBuilder(getApplicationContext(),
                    ListDB.class,DATABASE_NAME).allowMainThreadQueries().build();
            fillListView();

            filename = getExternalCacheDir().getAbsolutePath() + "/" +
                    getString(R.string.filename);

            textPlay = new TextToSpeech(this,this);
            textPlay.setOnUtteranceProgressListener(myListener);
        } else {
            Toast.makeText(this,"Need permissions",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void fillListView() {

        // setting up the UI with all the pictures
        String[] displayFields = {"thumbnail","description","recording"};
        int[] displayViews = {R.id.image,R.id.description,R.id.recording};
        ListView list;
        SimpleAdapter adapter;

        list = findViewById(R.id.list);
        adapter = new SimpleAdapter(this,getallPictures(),R.layout.list_item,
                displayFields,displayViews);
        adapter.setViewBinder(this);
        list.setOnItemClickListener(this);
        list.setAdapter(adapter);
    }
    public boolean setViewValue(View view,Object data,String asText) {

        // getting image, descriptions and recording, and showing them in the front page of the app
        switch (view.getId()) {
            case R.id.image:
                ((ImageView)view).setImageBitmap((Bitmap)data);
                break;
            case R.id.description:
                if (data != null) {
                    ((TextView)view).setText((String)data);
                } else {
                    ((TextView)view).setText(getResources().getString(
                            R.string.describeit));
                }
                break;
            case R.id.recording:
                ((CheckBox)view).setChecked(data != null);
                break;
        }
        return(true);
    }
    private MediaPlayer randomSong() {
        // select a random song from the library and start playing it
        Cursor audio;
        MediaPlayer player;
        String[] query= {MediaStore.Audio.Media.DATA};
        int noofSongs;

        audio = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                query,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (audio != null && (noofSongs = audio.getCount()) > 0) {
            audio.moveToPosition((int)(Math.random()*noofSongs));
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                player.setDataSource(audio.getString(
                        audio.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                player.prepare();
            } catch (IOException e) {

            }
            audio.close();
        } else {
            player = MediaPlayer.create(this,R.raw.randomsong);
        }
        player.setLooping(true);
        player.start();
        return(player);
    }

    private ArrayList<HashMap<String,Object>> getallPictures() {

        // fetching pictures from the gallery of the device
        HashMap<String,Object> oneItem;
        ArrayList<HashMap<String,Object>> listItems;
        Bitmap bitmap;
        ListEntity pictureOne;
        String[] query= {MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA};
        int imageID;

        if (cursorImg != null) {
            cursorImg.close();
        }
        listItems = new ArrayList<>();
        cursorImg = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                query,null,null,MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (cursorImg != null && cursorImg.getCount() > 0 && cursorImg.moveToFirst()) {
            do {
                imageID = cursorImg.getInt(cursorImg.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID));
                oneItem = new HashMap<>();
                if ((bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        getContentResolver(),imageID,MediaStore.Images.Thumbnails.MICRO_KIND,null)) != null) {
                    oneItem.put("thumbnail",bitmap);
                    if ((pictureOne =
                            talkingPicturesDB.daoAccess().getTalkingPictureByImageId(imageID)) != null) {
                        oneItem.put("description",pictureOne.getDescription());
                        oneItem.put("recording",pictureOne.getRecording());
                    } else {
                        oneItem.put("description",null);
                        oneItem.put("recording",null);
                    }
                }
                listItems.add(oneItem);
            } while (cursorImg.moveToNext());
        }
        return(listItems);
    }

    public void onItemClick(AdapterView<?> parent,View view,int position,long rowId) {

        // to open the second dialog activity. need to also send in the data for that image
        ListEntity thepicture;
        ListDialog theDialogFragment;
        Bundle bundletofragment;
        int imageID;
        Uri imageUri;
        Intent editIntent;

        cursorImg.moveToPosition(position);
        imageID = cursorImg.getInt(cursorImg.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID));
        imageUri = Uri.parse(cursorImg.getString(cursorImg.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATA)));
        songPlay.pause();
        if ((thepicture = talkingPicturesDB.daoAccess().getTalkingPictureByImageId(
                imageID)) != null) {
            bundletofragment = new Bundle();
            bundletofragment.putParcelable("image_to_display",imageUri);
            theDialogFragment = new ListDialog();
            theDialogFragment.setArguments(bundletofragment);
            theDialogFragment.show(getFragmentManager(),"my_fragment");
            descriptions(((TextView)view.findViewById(R.id.description)).getText().toString(),
                    thepicture.getRecording());
        } else {
            editIntent = new Intent(this,ListEdit.class);
            editIntent.putExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.image_id",imageID);
            editIntent.putExtra("edu.miami.cs.niceyraiyani.talkingpicturelist.image_uri",imageUri);
            startEdit.launch(editIntent);
        }
    }
    ActivityResultLauncher<Intent> startEdit = registerForActivityResult(
            // if description was edited in the dialog then it needs to update
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Intent resultIntent;
                    ListEntity dbEntry;

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        resultIntent = result.getData();
                        dbEntry = new ListEntity();
                        dbEntry.setImageId(resultIntent.getIntExtra(
                                "edu.miami.cs.niceyraiyani.talkingpicturelist.image_id",0));
                        dbEntry.setDescription(resultIntent.getStringExtra(
                                "edu.miami.cs.niceyraiyani.talkingpicturelist.description"));
                        dbEntry.setRecording(resultIntent.getByteArrayExtra(
                                "edu.miami.cs.niceyraiyani.talkingpicturelist.recording"));
                        talkingPicturesDB.daoAccess().addTalkingPicture(dbEntry);
                        fillListView();
                    } else {
                        Toast.makeText(MainActivity.this,"No description returned",Toast.LENGTH_SHORT).show();
                    }
                    songPlay.start();
                }
            });
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(this,"Textto speech is installed",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"TextToSpeech not installed",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void descriptions(String textDescription,byte[] recordedDescription) {
        //descriptions for each image
        voice= recordedDescription;
        if (textDescription != null && textDescription.length() > 0) {
            textPlay.speak(textDescription,TextToSpeech.QUEUE_FLUSH,null,"WHAT_I_SAID");
            if (voice!= null) {
                textPlay.playSilentUtterance(getResources().getInteger(
                        R.integer.pause),TextToSpeech.QUEUE_ADD,"SILENCE");
            }
        } else {
            playRecording();
        }
    }
    private UtteranceProgressListener myListener = new UtteranceProgressListener() {
        // voice listner to the user words
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {

                    if (utteranceId.equals("SILENCE")) {
                        playRecording();
                    }
                }

                @Override
                public void onError(String utteranceId) {
                }
            };
    private void playRecording() {
        // get the stored recording audio in files and play it
        FileOutputStream audioStream;

        if (voice != null) {
            try {
                audioStream = new FileOutputStream(filename);
                audioStream.write(voice);
                audioStream.close();
                voicePlay = new MediaPlayer();
                voicePlay.setOnCompletionListener(this);
                voicePlay.setDataSource(filename);
                voicePlay.prepare();
                voicePlay.start();
            } catch (IOException e) {
            }
        }
    }

    public void stopTalking() {
        //if stop button is pressed then close the voice recorded
        if (textPlay.isSpeaking()) {
            textPlay.stop();
        }
        if (voicePlay != null && voicePlay.isPlaying()) {
            voicePlay.stop();
            voicePlay.release();
            voicePlay = null;
        }
        voice= null;
        songPlay.start();
    }
    public void onCompletion(MediaPlayer mediaPlayer) {
        // end everything and remember to delete the file
        mediaPlayer.release();
        voicePlay = null;
        voice= null;
        (new File(filename)).delete();
    }
    @Override
    public void onDestroy() {
        // on destroy method is classic
        super.onDestroy();
        if (talkingPicturesDB != null) {
            talkingPicturesDB.close();
        }
        if (textPlay != null) {
            textPlay.shutdown();
        }
        if (songPlay != null) {
            songPlay.release();
        }
        if (cursorImg != null) {
            cursorImg.close();
        }
    }

}

