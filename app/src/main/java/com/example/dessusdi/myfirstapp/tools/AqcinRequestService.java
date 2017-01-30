package com.example.dessusdi.myfirstapp.tools;

import android.app.Activity;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dessusdi.myfirstapp.R;
import com.example.dessusdi.myfirstapp.model.GlobalObject;
import com.google.gson.Gson;

/**
 * Created by dessusdi on 30/01/2017.
 */
public class AqcinRequestService {

    private Activity mApplicationContext;

    public AqcinRequestService(Activity context) {
        mApplicationContext = context;
    }

    public void sendRequestWithUrl(String url) {
        final TextView mTextView = (TextView) this.mApplicationContext.findViewById(R.id.textViewAPI);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this.mApplicationContext);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        GlobalObject global = gson.fromJson(response, GlobalObject.class);
                        mTextView.setText(global.getRxs().getObs().get(0).getMsg().getCity().getName());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                mTextView.setText("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
