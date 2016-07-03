package cl.cadcc.folio.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cl.cadcc.folio.R;


public class WelcomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.startNfcReader();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.startNfcReader();
        mListener.onNfcDetectorChange();
    }

    public void onPause() {
        super.onResume();
        mListener.stopNfcReader();
        mListener.onNfcDetectorChange();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.stopNfcReader();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void startNfcReader();
        public void stopNfcReader();
        public void onNfcDetectorChange();
    }
}
