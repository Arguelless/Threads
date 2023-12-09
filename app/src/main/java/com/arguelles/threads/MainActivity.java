package com.arguelles.threads;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String error = ""; // camp de cadena
    private static final String TAG = "MainActivity"; // etiqueta de registre

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        TextView textView = findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://randomfox.ca/floof/";
                getDataFromUrl(url);
            }
        });
    }


    private void getDataFromUrl(String demoIdUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String result = null;
                String urlString = null;
                int resCode;
                InputStream in;
                try {
                    URL url = new URL(demoIdUrl);
                    URLConnection urlConn = url.openConnection();

                    HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
                    httpsConn.setAllowUserInteraction(false);
                    httpsConn.setInstanceFollowRedirects(true);
                    httpsConn.setRequestMethod("GET");
                    httpsConn.connect();
                    resCode = httpsConn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = httpsConn.getInputStream();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                in, StandardCharsets.ISO_8859_1), 8);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        in.close();
                        result = sb.toString();
                        try {
                            JSONObject jsonObject = new JSONObject(sb.toString());
                            urlString = jsonObject.getString("image");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "Data received: " + result);

                        Bitmap bitmap = null;
                        try {
                            InputStream inp = new java.net.URL(urlString).openStream();
                            bitmap = BitmapFactory.decodeStream(inp);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }

                        // Aquí puedes realizar tareas adicionales en segundo plano si es necesario.

                        // Ahora, realiza tareas en la interfaz gráfica (GUI) utilizando Handler.
                        Handler handler = new Handler(Looper.getMainLooper());
                        String finalResult = result;
                        Bitmap finalBitmap = bitmap;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = findViewById(R.id.textView);
                                textView.setText(finalResult);
                            
                                ImageView imageView = findViewById(R.id.imageView);
                                imageView.setImageBitmap(finalBitmap);}
                        });
                    } else {
                        error += resCode;
                        Log.e(TAG, "Error: " + error);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
