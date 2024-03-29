package com.example.applicationqr.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.applicationqr.MainMenuActivity;
import com.example.applicationqr.R;
import com.example.applicationqr.onFragmentInteractionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment
{

    private static final String TAG = SignUpFragment.class.getName();
    private EditText registerEmail, registerPassword, registerName, registerID;
    private Button submitButton, goBackButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ConstraintLayout loading_panel;

    private onFragmentInteractionListener fragmentInteractionListener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        InitUI(v);
        return v;
    }

    private void InitUI(View v)
    {
        registerEmail = v.findViewById(R.id.register_email_input);
        registerPassword = v.findViewById(R.id.register_password_input);
        registerName = v.findViewById(R.id.register_name_input);
        registerID = v.findViewById(R.id.register_id_input);
        submitButton = v.findViewById(R.id.register_submit_button);
        goBackButton = v.findViewById(R.id.register_go_back);
        loading_panel = getActivity().findViewById(R.id.loading_panel);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fragmentInteractionListener.onFragmentMessage(TAG,null);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String name = registerName.getText().toString();
                String id = registerID.getText().toString();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty() || id.isEmpty())
                {
                    Toast.makeText(getContext(), "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loading_panel.setVisibility(View.VISIBLE);
                    CreateUser(email,password,name,id);
                }
            }
        });
    }

    private void CreateUser(String email, String password, final String name, final String id)
    {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("name", name);
                    data.put("type", 2);
                    data.put("userID",id);

                    db.collection("users").document(user.getUid())
                            .set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });

                    // Start the "main menu" Activity
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainMenuActivity.class);
                    startActivity(intent);
                    getActivity().finish();

                } else
                    {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    loading_panel.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), String.format("Authentication failed. %s", task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentInteractionListener = (onFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement onFragmentInteractionListener");
        }
    }
}
