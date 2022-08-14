package com.kaizokuapps.news13;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int numberOfItems = 13;
    public ArrayList<URL> urls= new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    JSONArray jsonArray = null;

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = " ";

            try{
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while(data!=-1) {
                    result+=(char)data;
                    data=inputStreamReader.read();
                }
                jsonArray = new JSONArray(result);
                Log.i("IDs",jsonArray.toString());

                for(int i = 0; titles.size() < numberOfItems;i++){
                    String articleID = jsonArray.getString(i);
                    String res="";
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleID + ".json?print=pretty");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    inputStream = httpURLConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    data = inputStreamReader.read();
                    while(data!=-1) {
                        res+=(char)data;
                        data=inputStreamReader.read();
                    }
                    JSONObject jsonObject = new JSONObject(res);
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        titles.add(jsonObject.getString("title"));
                        urls.add(new URL(jsonObject.getString("url")));
                    }
                }
                Log.i("Content URLs Count", String.valueOf(titles.size()));
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,titles);
            ((ListView)findViewById(R.id.newsList)).setAdapter(arrayAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            (new DownloadTask()).execute("https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty");
        }catch(Exception e){
            e.printStackTrace();
        }

        ((ListView)findViewById(R.id.newsList)).deferNotifyDataSetChanged();
        ((ListView)findViewById(R.id.newsList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Index", String.valueOf(position));
                Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
                intent.putExtra("URL",urls.get(position).toString());
                startActivity(intent);
            }
        });
    }
}
