package edu.miami.cs.geoff.talkingpicturelist;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

//=================================================================================================
public class TalkingPictureListDialog extends DialogFragment implements View.OnClickListener {
//-------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
Bundle savedInstanceState) {

        View dialogView;

        dialogView = inflater.inflate(R.layout.dialog,container);
        ((ImageView)dialogView.findViewById(R.id.the_image)).setImageURI(
this.getArguments().getParcelable("image_to_display"));
        (dialogView.findViewById(R.id.dismiss)).setOnClickListener(this);
        return(dialogView);
    }
//-------------------------------------------------------------------------------------------------
    public void onClick(View view) {

        ((StopTalking)getActivity()).stopTalking();
        dismiss();
    }
//-------------------------------------------------------------------------------------------------
    public interface StopTalking {

        public void stopTalking();
    }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================
