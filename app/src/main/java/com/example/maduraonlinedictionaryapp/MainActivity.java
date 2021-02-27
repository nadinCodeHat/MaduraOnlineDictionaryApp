package com.example.maduraonlinedictionaryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";
    private EditText keyword_txt;
    private TextView textView_display;
    private Button translate_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keyword_txt = (EditText) findViewById(R.id.keyword);
        textView_display = (TextView) findViewById(R.id.textDisplay);
        translate_btn = (Button) findViewById(R.id.translateBtn);
        translate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringUrl = "https://www.maduraonline.com/?find=" + keyword_txt.getText().toString();
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(stringUrl);
                } else {
                    textView_display.setText("No network connection available.");
                }
            }
        });
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            ArrayList<String> words = new ArrayList<>();
            Pattern pattern = Pattern.compile("<td class=\"td\">(.*?)</td>");
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                words.add(matcher.group(1));
                for (int i=1; i<words.size()-1; i++) {
                    textView_display.setText(textView_display.getText() + "\n" + matcher.group(1));
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        private String downloadUrl(String myUrl) throws IOException {
            InputStream is = null;
            int len = 4000;
            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();
                String contentAsString = readIt(is, len);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
}