package cn.rongcloud.im.ui.activity.records;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.rongcloud.im.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PicFragment extends Fragment {

    public static PicFragment newInstance() {

        Bundle args = new Bundle();

        PicFragment fragment = new PicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pic, container, false);
    }

}
