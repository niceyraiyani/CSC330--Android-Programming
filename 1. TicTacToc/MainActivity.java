package edu.miami.cs.geoff.talkingpicturelist;

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

//=================================================================================================
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
SimpleAdapter.ViewBinder, MediaPlayer.OnCompletionListener, TextToSpeech.OnInitListener,
TalkingPictureListDialog.StopTalking {
//-------------------------------------------------------------------------------------------------
    public static final String DATABASE_NAME = "TalkingPictures.db";

    private TalkingPictureListDB talkingPicturesDB;
    private Cursor imagesCursor = null;

    private MediaPlayer songPlayer = null;
    private MediaPlayer voicePlayer = null;
    private TextToSpeech textTTSSpeaker = null;
    private byte[] recordedVoiceToPlay = null;
    public static String recordFileName;
//-------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissions.launch(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
Manifest.permission.RECORD_AUDIO});
    }
//-------------------------------------------------------------------------------------------------
    private void goOnCreating(boolean havePermission) {

        if (havePermission) {
            setContentView(R.layout.activity_main);
            songPlayer = startRandomSong();
            talkingPicturesDB = Room.databaseBuilder(getApplicationContext(),
TalkingPictureListDB.class,DATABASE_NAME).allowMainThreadQueries().build();
            fillList();

            recordFileName = getExternalCacheDir().getAbsolutePath() + "/" +
getString(R.string.record_file_name);

            textTTSSpeaker = new TextToSpeech(this,this);
            textTTSSpeaker.setOnUtteranceProgressListener(myListener);
        } else {
            Toast.makeText(this,"Need permission",Toast.LENGTH_LONG).show();
            finish();
        }
    }
//-------------------------------------------------------------------------------------------------
    private MediaPlayer startRandomSong() {

        Cursor audioCursor;
        MediaPlayer newPlayer;
        String[] queryFields = {MediaStore.Audio.Media.DATA};
        int numberOfSongs;

        audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
queryFields,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (audioCursor != null && (numberOfSongs = audioCursor.getCount()) > 0) {
            audioCursor.moveToPosition((int)(Math.random()*numberOfSongs));
            newPlayer = new MediaPlayer();
            newPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                newPlayer.setDataSource(audioCursor.getString(
audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                newPlayer.prepare();
            } catch (IOException e) {
                //----Should do something here
            }
            audioCursor.close();
        } else {
            newPlayer = MediaPlayer.create(this,R.raw.chin_chin_choo);
        }
        newPlayer.setLooping(true);
        newPlayer.start();
        return(newPlayer);
    }
//-------------------------------------------------------------------------------------------------
    private void fillList() {

        String[] displayFields = {"thumbnail","description","recording"};
        int[] displayViews = {R.id.image,R.id.description,R.id.recording};
        ListView theList;
        SimpleAdapter listAdapter;

        theList = findViewById(R.id.the_list);
        listAdapter = new SimpleAdapter(this,fetchAllTalkingPictures(),R.layout.list_item,
displayFields,displayViews);
        listAdapter.setViewBinder(this);
        theList.setOnItemClickListener(this);
        theList.setAdapter(listAdapter);
    }
//-------------------------------------------------------------------------------------------------
    private ArrayList<HashMap<String,Object>> fetchAllTalkingPictures() {

        HashMap<String,Object> oneItem;
        ArrayList<HashMap<String,Object>> listItems;
        Bitmap thumbnailBitmap;
        TalkingPictureListEntity oneTalkingPicture;
        String[] queryFields = {MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA};
        int imageID;

        if (imagesCursor != null) {
            imagesCursor.close();
        }
        listItems = new ArrayList<>();
        imagesCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
queryFields,null,null,MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (imagesCursor != null && imagesCursor.getCount() > 0 && imagesCursor.moveToFirst()) {
            do {
                imageID = imagesCursor.getInt(imagesCursor.getColumnIndexOrThrow(
MediaStore.Images.Media._ID));
                oneItem = new HashMap<>();
                if ((thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(
getContentResolver(),imageID,MediaStore.Images.Thumbnails.MICRO_KIND,null)) != null) {
                    oneItem.put("thumbnail",thumbnailBitmap);
                    if ((oneTalkingPicture =
talkingPicturesDB.daoAccess().getTalkingPictureByImageId(imageID)) != null) {
                        oneItem.put("description",oneTalkingPicture.getDescription());
                        oneItem.put("recording",oneTalkingPicture.getRecording());
                    } else {
                        oneItem.put("description",null);
                        oneItem.put("recording",null);
                    }
                }
                listItems.add(oneItem);
            } while (imagesCursor.moveToNext());
        }
        return(listItems);
    }
//-------------------------------------------------------------------------------------------------
//----Needed because SimpleAdapter cannot do Bitmap or byte[] (the recording)
    public boolean setViewValue(View view,Object data,String asText) {

        switch (view.getId()) {
            case R.id.image:
                ((ImageView)view).setImageBitmap((Bitmap)data);
                break;
            case R.id.description:
                if (data != null) {
                    ((TextView)view).setText((String)data);
                } else {
                    ((TextView)view).setText(getResources().getString(
R.string.no_description_text));
                }
                break;
            case R.id.recording:
                ((CheckBox)view).setChecked(data != null);
                break;
        }
        return(true);
    }
//-------------------------------------------------------------------------------------------------
    public void onItemClick(AdapterView<?> parent,View view,int position,long rowId) {

        TalkingPictureListEntity theTalkingPicture;
        TalkingPictureListDialog theDialogFragment;
        Bundle bundleToFragment;
        int imageID;
        Uri imageUri;
        Intent editIntent;

        imagesCursor.moveToPosition(position);
        imageID = imagesCursor.getInt(imagesCursor.getColumnIndexOrThrow(
MediaStore.Images.Media._ID));
        imageUri = Uri.parse(imagesCursor.getString(imagesCursor.getColumnIndexOrThrow(
MediaStore.Images.Media.DATA)));
        songPlayer.pause();
        if ((theTalkingPicture = talkingPicturesDB.daoAccess().getTalkingPictureByImageId(
imageID)) != null) {
            bundleToFragment = new Bundle();
            bundleToFragment.putParcelable("image_to_display",imageUri);
            theDialogFragment = new TalkingPictureListDialog();
            theDialogFragment.setArguments(bundleToFragment);
            theDialogFragment.show(getFragmentManager(),"my_fragment");
            speakDescriptions(((TextView)view.findViewById(R.id.description)).getText().toString(),
theTalkingPicture.getRecording());
        } else {
            editIntent = new Intent(this,TalkingPictureListEdit.class);
            editIntent.putExtra("edu.miami.cs.geoff.talkingpicturelist.image_id",imageID);
            editIntent.putExtra("edu.miami.cs.geoff.talkingpicturelist.image_uri",imageUri);
            startEdit.launch(editIntent);
        }
    }
//-------------------------------------------------------------------------------------------------
    ActivityResultLauncher<Intent> startEdit = registerForActivityResult(
new ActivityResultContracts.StartActivityForResult(),
new ActivityResultCallback<ActivityResult>() {
    @Override
    public void onActivityResult(ActivityResult result) {

        Intent resultIntent;
        TalkingPictureListEntity dbEntry;

        if (result.getResultCode() == Activity.RESULT_OK) {
            resultIntent = result.getData();
            dbEntry = new TalkingPictureListEntity();
            dbEntry.setImageId(resultIntent.getIntExtra(
"edu.miami.cs.geoff.talkingpicturelist.image_id",0));
            dbEntry.setDescription(resultIntent.getStringExtra(
"edu.miami.cs.geoff.talkingpicturelist.description"));
            dbEntry.setRecording(resultIntent.getByteArrayExtra(
"edu.miami.cs.geoff.talkingpicturelist.recording"));
            talkingPicturesDB.daoAccess().addTalkingPicture(dbEntry);
//----Lazy way to update listview. Could do just that element.
            fillList();
        } else {
            Toast.makeText(MainActivity.this,"No description returned",Toast.LENGTH_SHORT).show();
        }
        songPlayer.start();
    }
});
//-------------------------------------------------------------------------------------------------
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(this,"Now you can talk",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"You need to install TextToSpeech",Toast.LENGTH_LONG).show();
            finish();
        }
    }
//-------------------------------------------------------------------------------------------------
    private void speakDescriptions(String textDescription,byte[] recordedDescription) {

        recordedVoiceToPlay = recordedDescription;
        if (textDescription != null && textDescription.length() > 0) {
            textTTSSpeaker.speak(textDescription,TextToSpeech.QUEUE_FLUSH,null,"WHAT_I_SAID");
            if (recordedVoiceToPlay != null) {
                textTTSSpeaker.playSilentUtterance(getResources().getInteger(
R.integer.pause_length),TextToSpeech.QUEUE_ADD,"SILENCE");
//            } else {
//                textTTSSpeaker.playSilentUtterance(getResources().getInteger(
//R.integer.pause_length),TextToSpeech.QUEUE_ADD,"NOTHING");
//                textTTSSpeaker.speak(getResources().getString(R.string.nothing_to_say),
//TextToSpeech.QUEUE_ADD,null,"NOTHING");
            }
        } else {
            playRecording();
        }
    }
//-------------------------------------------------------------------------------------------------
    private UtteranceProgressListener myListener =
new UtteranceProgressListener() {
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
//-------------------------------------------------------------------------------------------------
    private void playRecording() {

        FileOutputStream audioStream;

        if (recordedVoiceToPlay != null) {
            try {
                audioStream = new FileOutputStream(recordFileName);
                audioStream.write(recordedVoiceToPlay);
                audioStream.close();
                voicePlayer = new MediaPlayer();
                voicePlayer.setOnCompletionListener(this);
                voicePlayer.setDataSource(recordFileName);
                voicePlayer.prepare();
                voicePlayer.start();
            } catch (IOException e) {
                // Should do something here
            }
        }
    }
//-------------------------------------------------------------------------------------------------
    public void onCompletion(MediaPlayer mediaPlayer) {

        mediaPlayer.release();
        voicePlayer = null;
        recordedVoiceToPlay = null;
        (new File(recordFileName)).delete();
    }
//-------------------------------------------------------------------------------------------------
    public void stopTalking() {

        if (textTTSSpeaker.isSpeaking()) {
            textTTSSpeaker.stop();
        }
        if (voicePlayer != null && voicePlayer.isPlaying()) {
            voicePlayer.stop();
            voicePlayer.release();
            voicePlayer = null;
        }
        recordedVoiceToPlay = null;
        songPlayer.start();
    }
//-------------------------------------------------------------------------------------------------
    @Override
    public void onDestroy() {

        super.onDestroy();
        if (talkingPicturesDB != null) {
            talkingPicturesDB.close();
        }
        if (textTTSSpeaker != null) {
            textTTSSpeaker.shutdown();
        }
        if (songPlayer != null) {
            songPlayer.release();
        }
        if (imagesCursor != null) {
            imagesCursor.close();
        }
    }
//-------------------------------------------------------------------------------------------------
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
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================