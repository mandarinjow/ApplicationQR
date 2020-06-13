package com.example.applicationqr.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.applicationqr.R;
import com.example.applicationqr.model.ClassSession;
import com.example.applicationqr.model.Classroom;
import com.example.applicationqr.onFragmentInteractionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionListFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = SessionListFragment.class.getName();

    // TODO: Rename and change types of parameters
    private String classroomFireStoreID;
    private String mParam2;
    private ArrayList<ClassSession> classSessions;

    private TextView nothingHere;
    private RecyclerView recyclerViewSession;
    private FloatingActionButton addButtonSession;
    private FirebaseFirestore db;

    private onFragmentInteractionListener fragmentInteractionListener;


    public SessionListFragment()
    {
        db = FirebaseFirestore.getInstance();
        classSessions = new ArrayList<>();

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SessionListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionListFragment newInstance(String param1, String param2)
    {
        SessionListFragment fragment = new SessionListFragment();
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
        if (getArguments() != null)
        {
            classroomFireStoreID = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_session_list, container, false);
        InitUI(v);
        getClassSessions();
        return v;
    }

    private void InitUI(View v)
    {
        // Change title on the App Bar
        Toolbar mainMenuToolbar = getActivity().findViewById(R.id.main_menu_toolbar);
        mainMenuToolbar.setTitle("Sessions");

        nothingHere = v.findViewById(R.id.nothing_here);
        recyclerViewSession = v.findViewById(R.id.recyclerView_session);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewSession.getContext(), layoutManager.getOrientation());
        recyclerViewSession.addItemDecoration(dividerItemDecoration);
        recyclerViewSession.setLayoutManager(layoutManager);

        addButtonSession = v.findViewById(R.id.add_session_button);

        addButtonSession.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Map<String,Object> data = new HashMap<>();
                data.put("sessiontime", FieldValue.serverTimestamp());
                data.put("attended", new ArrayList<>());

                CollectionReference sessionRef = db.collection("classes").document(classroomFireStoreID).collection("sessions");
                sessionRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                });
                // fragmentInteractionListener.onFragmentMessage(TAG, null);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        try
        {
            fragmentInteractionListener = (onFragmentInteractionListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + "must implement onFragmentInteractionListener");
        }
    }

    private void getClassSessions()
    {
        ArrayList<ClassSession> tempcollection = new ArrayList<>();
        Source source = Source.DEFAULT;

        CollectionReference sessionRef = db.collection("classes").document(classroomFireStoreID).collection("sessions");
        sessionRef.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    if(task.getResult().isEmpty())
                    {
                        nothingHere.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onComplete: Here!");
                    }
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
}
