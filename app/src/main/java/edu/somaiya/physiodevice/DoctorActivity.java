package edu.somaiya.physiodevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.min;

public class DoctorActivity extends AppCompatActivity {

    public static int doctorid = -1;
    public Context context;
    ListView patientListView;
    TextView doctorInfo;
    List<Patient> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        doctorInfo = findViewById(R.id.doctorinfotext);
        patientListView = findViewById(R.id.patientlist);
        PatientArrayAdapter adapter = new PatientArrayAdapter(this, R.layout.patient_list_layout, patientList);
        patientListView.setAdapter(adapter);
        patientList = new ArrayList<>();
        context = this;
        String doctorName = getIntent().getStringExtra("doctorname");
        doctorInfo.setText("Dr. " + doctorName);
        doctorInfo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        doctorid = getIntent().getIntExtra("doctorid", doctorid);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://" + MainActivity.server_ip_address + "/getpatients/" + doctorid, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("patientresponse", response);
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        String name, description, sex;
                        int age, patientid;
                        name = jsonObject.getString("name");
                        description = jsonObject.getString("description");
                        sex = jsonObject.getString("sex");
                        age = jsonObject.getInt("age");
                        patientid = jsonObject.getInt("patientid");
                        Patient patient = new Patient(patientid, name, description, sex, age);
                        patientList.add(patient);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    public class Patient {

        private String name, description, sex;
        private int patientid, age;

        public Patient(int patientid, String name, String description, String sex, int age) {
            this.name = name;
            this.description = description;
            this.patientid = patientid;
            this.age = age;
            this.sex = sex;
        }

        public String getName() {
            return this.name;
        }

        public String getSex() {
            return this.sex;
        }

        public String getDescription() {
            return this.description;
        }

        public int getPatientID() {
            return this.patientid;
        }

        public int getAge() {
            return this.age;
        }
    }

    public class PatientAdapter extends BaseAdapter {

        private Activity activity;
        private LayoutInflater layoutInflater;
        private List<Patient> patientList;

        public PatientAdapter(Activity activity, List<Patient> patientList) {
            this.activity = activity;
            this.patientList = patientList;
        }

        @Override
        public int getCount() {
            return patientList.size();
        }

        @Override
        public Object getItem(int i) {
            return patientList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (layoutInflater == null)
                layoutInflater = (LayoutInflater) activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (view == null)
                view = layoutInflater.inflate(R.layout.patient_list_layout, null);

            TextView patientName = view.findViewById(R.id.patientlisttext_name);
            TextView patientDescription = view.findViewById(R.id.patientlisttext_desc);
            Button viewPatient = view.findViewById(R.id.patientlistbutton);

            // getting movie data for the row
            Patient patient = patientList.get(i);

            // name
            patientName.setText(patient.getName());

            // description
            patientDescription.setText(patient.getDescription());

            return view;
        }
    }

    public class PatientArrayAdapter extends ArrayAdapter<Patient> {

        List<Patient> patientList;

        Context context;

        int resource; // For each item layout

        public PatientArrayAdapter(@NonNull Context context, int resource, List<Patient> patientList) {
            super(context, resource);
            this.context = context;
            this.resource = resource;
            this.patientList = patientList;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //we need to get the view of the xml for our list item
            //And for this we need a layoutinflater
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            //getting the view
            View view = layoutInflater.inflate(resource, null, false);

            //getting the view elements of the list from the view
            TextView patientName = view.findViewById(R.id.patientlisttext_name);
            TextView patientDescription = view.findViewById(R.id.patientlisttext_desc);
            Button expandPatientProfile = view.findViewById(R.id.patientlistbutton);

            //getting the patient of the specified position
            Patient patient = patientList.get(position);

            //adding values to the list item
            patientName.setText(patient.getName());
            patientDescription.setText(patient.getDescription().substring(0, min(140, patient.getDescription().length())) + "...");

            //adding a click listener to the button to remove item from the list
            expandPatientProfile.setOnClickListener(view1 -> {
                Intent intent = new Intent(context, PatientActivity.class);
                intent.putExtra("patientid", patientList.get(position).getPatientID());
                intent.putExtra("patientname", patientList.get(position).getName());
                intent.putExtra("patientage", patientList.get(position).getAge());
                intent.putExtra("patientsex", patientList.get(position).getSex());
                intent.putExtra("patientdescription", patientList.get(position).getDescription());
            });

            //finally returning the view
            return view;

        }
    }
//        this.context = this;
//        this.refreshButton = (Button) findViewById(R.id.refresh_button);
//        this.calibrateButton = (Button) findViewById(R.id.calibrate_button);
//        this.profilesRadioGroup = (RadioGroup) findViewById(R.id.profile_radio_group);
//        this.trainButton = (Button) findViewById(R.id.train_button);
//        this.deleteAllProfilesButton = (Button) findViewById(R.id.delete_profiles_button);
//        this.deleteProfileButton = (Button) findViewById(R.id.delete_profile_button);
//
//        calibrateButton.setOnClickListener((view) -> {
//            new AsyncCalibrateTask().execute();
//        });
//
//        refreshButton.setOnClickListener((view) -> {
//                new AsyncRefreshTask().execute(this);
//        });
//
//        trainButton.setOnClickListener((view) -> {
//            if(profilesRadioGroup.getCheckedRadioButtonId() == -1)
//            {
//            } else {
//                RadioButton radioButton = (RadioButton) findViewById(profilesRadioGroup.getCheckedRadioButtonId());
//                // new AsyncStartTrainingTask().execute(Integer.parseInt(radioButton.getText().toString().trim()), MainActivity.global_rep);
//            }
//        });
//
//        deleteProfileButton.setOnClickListener((view) -> {
//            if(profilesRadioGroup.getCheckedRadioButtonId() == -1)
//            {
//            } else {
//                RadioButton radioButton = (RadioButton) findViewById(profilesRadioGroup.getCheckedRadioButtonId());
//                new AsyncDeleteProfileTask().execute(Integer.parseInt(radioButton.getText().toString().trim()));
//            }
//        });
//
//        deleteAllProfilesButton.setOnClickListener((view) -> {
//            new AsyncDeleteProfileTask().execute(0);
//        });
//    }
//
//    private class AsyncCalibrateTask extends AsyncTask<Void, Void, Void> {
//
//        public static final String REQUEST_METHOD = "GET";
//        public static final int READ_TIMEOUT = 15000;
//        public static final int CONNECTION_TIMEOUT = 15000;
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            String urlString = "http://" + MainActivity.server_ip_address + "/start_calibration";
//            Log.d("CALIBURL", urlString);
//            String result;
//            try {
//                URL myUrl = new URL(urlString);
//
//                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
//
//                connection.setRequestMethod(REQUEST_METHOD);
//                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//                connection.setReadTimeout(READ_TIMEOUT);
//
//                connection.connect();
//
//                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuilder = new StringBuilder();
//                String inputLine;
//
//                while ((inputLine = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(inputLine);
//                }
//
//                bufferedReader.close();
//                inputStreamReader.close();
//
//                result = stringBuilder.toString();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//    private class AsyncRefreshTask extends AsyncTask<Context, Void, Void> {
//
//        public static final String REQUEST_METHOD = "GET";
//        public static final int READ_TIMEOUT = 15000;
//        public static final int CONNECTION_TIMEOUT = 15000;
//        public JSONObject jsonObject;
//        public Context context;
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            try {
//                profilesRadioGroup.removeAllViews();
//                String[] profileNames = jsonObject.getJSONArray("profile_nos").join(",").split(",");
//                int i = 0;
//                int len = profileNames.length;
//                if(len == 1 && profileNames[0] == "") return;
//                for(int j = len - 1; j>=0; j--) {
//                    RadioButton radioButton = new RadioButton(this.context);
//                    radioButton.setText(profileNames[j]);
//                    radioButton.setId(12345 + i++);
//                    profilesRadioGroup.addView(radioButton);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Context... contexts) {
//            this.context = contexts[0];
//            String urlString = "http://" + MainActivity.server_ip_address + "/get_profiles";
//            Log.d("CALIBURL", urlString);
//            String result = "{\"profile_nos\": [1, 2, 3, 4]}";
//            try {
//                URL myUrl = new URL(urlString);
//
//                HttpURLConnection connection =  (HttpURLConnection) myUrl.openConnection();
//
//                connection.setRequestMethod(REQUEST_METHOD);
//                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//                connection.setReadTimeout(READ_TIMEOUT);
//
//                connection.connect();
//
//                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuilder = new StringBuilder();
//                String inputLine;
//
//                while((inputLine = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(inputLine);
//                }
//
//                bufferedReader.close();
//                inputStreamReader.close();
//
//                result = stringBuilder.toString();
//                Log.d("REFRESH", result);
//                try {
//                    jsonObject = new JSONObject(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//    private class AsyncStartTrainingTask extends AsyncTask<Integer, Void, Void> {
//
//        public static final String REQUEST_METHOD = "GET";
//        public static final int READ_TIMEOUT = 15000;
//        public static final int CONNECTION_TIMEOUT = 15000;
//
//        @Override
//        protected Void doInBackground(Integer... params) {
//            String urlString = "http://" + MainActivity.server_ip_address + "/start_training/" + params[0] + "/" + params[1];
//            Log.d("TRAINURL", urlString);
//            String result = "";
//            try {
//                URL myUrl = new URL(urlString);
//
//                HttpURLConnection connection =  (HttpURLConnection) myUrl.openConnection();
//
//                connection.setRequestMethod(REQUEST_METHOD);
//                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//                connection.setReadTimeout(READ_TIMEOUT);
//
//                connection.connect();
//
//                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuilder = new StringBuilder();
//                String inputLine;
//
//                while((inputLine = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(inputLine);
//                }
//
//                bufferedReader.close();
//                inputStreamReader.close();
//
//                result = stringBuilder.toString();
//                Log.d("TRAINRES", result);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//    private class AsyncDeleteProfileTask extends AsyncTask<Integer, Void, Void> {
//
//        public static final String REQUEST_METHOD = "GET";
//        public static final int READ_TIMEOUT = 15000;
//        public static final int CONNECTION_TIMEOUT = 15000;
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            new AsyncRefreshTask().execute(context);
//        }
//
//        @Override
//        protected Void doInBackground(Integer... params) {
//            String urlString = "http://" + MainActivity.server_ip_address + "/delete_profile/" + params[0];
//            Log.d("DPURL", urlString);
//            String result = "";
//            try {
//                URL myUrl = new URL(urlString);
//
//                HttpURLConnection connection =  (HttpURLConnection) myUrl.openConnection();
//
//                connection.setRequestMethod(REQUEST_METHOD);
//                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//                connection.setReadTimeout(READ_TIMEOUT);
//
//                connection.connect();
//
//                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuilder = new StringBuilder();
//                String inputLine;
//
//                while((inputLine = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(inputLine);
//                }
//
//                bufferedReader.close();
//                inputStreamReader.close();
//
//                result = stringBuilder.toString();
//                Log.d("DPRES", result);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//    /*class ProfileAdapter extends BaseAdapter implements ListAdapter {
//
//        Context context;
//        ArrayList<String> arrayList;
//
//        ProfileAdapter(Context context, ArrayList<String> arrayList) {
//            this.context = context;
//            this.arrayList = arrayList;
//        }
//
//        public void add(String s) {
//            arrayList.add(s);
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public int getCount() {
//            return arrayList.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return arrayList.get(i);
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int i, View view, ViewGroup viewGroup) {
//            if(view == null) {
//                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                view = layoutInflater.inflate(R.layout.patient_list_layout, null);
//            }
//            if(i >= arrayList.size()) {
//                return view;
//            }
//            Button button = (Button) findViewById(R.id.profile_button);
//            if(button == null) return view;
//            button.setText((String)arrayList.get(i));
//            button.setOnClickListener((view1) -> {
//                new AsyncStartTrainingTask().execute(Integer.parseInt(button.getText().toString().trim()), MainActivity.global_rep);
//            });
//
//            return view;
//        }
//    }*/
}
