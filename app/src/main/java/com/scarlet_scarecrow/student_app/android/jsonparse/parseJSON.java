package com.scarlet_scarecrow.student_app.android.jsonparse;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class parseJSON extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_parse_json, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            update();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    ArrayAdapter<String> dataAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        dataAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item,
                        R.id.list_item_textview,
                        new ArrayList<String>());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_id);
        listView.setAdapter(dataAdapter);




        //added the intent for displaying the detail class
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String data = dataAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), Detail.class)
                        .putExtra(Intent.EXTRA_TEXT, "SKU: "+ data +"\n Amount: "+amount[position]+ "\n Currency: "+currency[position]);
                intent.putExtra("position", Integer.toString(position));
                startActivity(intent);
            }
        });


        return rootView;

    }
    //to fetch data as soon as the app starts
    public void onStart() {
        super.onStart();
        update();

    }

    //to fetch new data when refresh is pressed
    public void update(){
        FetchData start = new FetchData();
        start.execute();
    }

    String[] amount ;
    String[] currency;


    public class FetchData extends AsyncTask<String, Void, String[]> {
        public FetchData(){

        }
        private final String LOG_TAG = FetchData.class.getSimpleName();

        public String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonStr = null;

            /*noNetwork defined to print suitable statement when device is not connected to internet*/
            String[] noNetwork = {"No network found","Please connect to Internet"};
            try {
                final String Url = "http://quiet-stone-2094.herokuapp.com/transactions.json";
                URL url = new URL(Url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();//converting to string
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
                //e.printStackTrace();
                return noNetwork;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.d("J", "Error closing stream");
                    }
                }
            }

            try {
                return parseJSONData(JsonStr)[0];
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing the data", e);
                //Log.d("D", JsonStr);
                e.printStackTrace();
                return null;
            }
        }


        public String[][] parseJSONData(String Jdata)
                throws JSONException {
            try
            {
                JSONArray array = new JSONArray(Jdata);
                String[] sku = new String[array.length()];
                String[] amt = new String[array.length()];
                String[] curr = new String[array.length()];
                for(int i=0; i<array.length();i++){
                    JSONObject s = array.getJSONObject(i);
                    sku[i] = s.getString("sku");
                    amt[i] = s.getString("amount");
                    curr[i] = s.getString("currency");
                }
                String[][] s = {sku,amt,curr};
                amount = s[1];
                currency = s[2];
                return s;
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error parseJSONData", e);
                e.printStackTrace();
                return null;
            }
        }
        //adding the sku data to the adapter
        protected void onPostExecute(String[] s){
            if (s != null) {
                dataAdapter.clear();
                for(String i : s) {
                    dataAdapter.add(i);
                }
            }
        }
    }
}
