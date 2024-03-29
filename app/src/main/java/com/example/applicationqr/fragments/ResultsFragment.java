package com.example.applicationqr.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.applicationqr.R;
import com.example.applicationqr.model.MySingleton;
import com.example.applicationqr.model.User;
import com.example.applicationqr.onFragmentInteractionListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultsFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ResultsFragment.class.getName();

    private TextView resultLabel;
    private Button confirmButton;
    private View loadingpanel;
    private RequestQueue requestQueue;
    private onFragmentInteractionListener fragmentInteractionListener;

    // TODO: Rename and change types of parameters
    private User currentUser;
    private String url;

    public ResultsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResultsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResultsFragment newInstance(User param1, String param2)
    {
        ResultsFragment fragment = new ResultsFragment();
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
        if (getArguments() != null)
        {
            currentUser = getArguments().getParcelable(ARG_PARAM1);
            url = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_results, container, false);
        InitUI(v);
        return v;
    }

    private void InitUI(View v)
    {
        confirmButton = v.findViewById(R.id.confirm_button);
        resultLabel = v.findViewById(R.id.result_label);
        loadingpanel = v.findViewById(R.id.loading_panel);

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadingpanel.setVisibility(View.INVISIBLE);
                fragmentInteractionListener.onFragmentMessage(TAG,null);
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        requestQueue = MySingleton.getInstance(getContext()).getRequestQueue();
        confirmButton.setEnabled(false);
        fetchJsonResponse(url);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void fetchJsonResponse(String qrString)
    {

        String requestUrl;
        if(currentUser.getType() == 1) // type 1 is teacher
        {
            String[] arr = qrString.split("#",2);
            // Log.d(TAG, "fetchJsonResponse: " + qrString);

            // http://url/api/?cID=123&uID=123 (example url)
            requestUrl = String.format("%s/register?cID=%s&uID=%s", getString(R.string.cloud_functions), arr[0], arr[1]);
            Log.d(TAG, "fetchJsonResponse: " + requestUrl);
        }
        else // student
        {
            requestUrl = String.format("%s&uID=%s", qrString, currentUser.getFirebaseUID());

        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            loadingpanel.setVisibility(View.INVISIBLE);
                            String result = "Message: " + response.getString("message");
                            Log.d(TAG, String.format("onResponse: %s", result));
                            Toast.makeText(getContext(),response.getString("message"),Toast.LENGTH_SHORT).show();
                            resultLabel.setText(result);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        confirmButton.setEnabled(true);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        loadingpanel.setVisibility(View.INVISIBLE);
                        resultLabel.setText("Code is not valid");
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.d("Error.Response", String.valueOf(error));
                        confirmButton.setEnabled(true);
                    }
                });
        /* Add your Requests to the RequestQueue to execute */
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        requestQueue.add(req);
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
}
