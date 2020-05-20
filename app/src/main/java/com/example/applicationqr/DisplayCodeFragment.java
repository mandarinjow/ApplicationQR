package com.example.applicationqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayCodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private QRGenerator codeGenerator;
    private ImageView qrCode;
    // TODO: Rename and change types of parameters
    private User mParam1;
    private String mParam2;

    public DisplayCodeFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisplayCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayCodeFragment newInstance(User param1, String param2)
    {
        DisplayCodeFragment fragment = new DisplayCodeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getParcelable(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        codeGenerator = QRGenerator.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_display_code, container, false);
        qrCode = v.findViewById(R.id.qr_code_display_image);
        qrCode.setImageBitmap(codeGenerator.getQRBitmap(mParam1.getFirebaseUID(),getContext()));
        return v;
    }
}
