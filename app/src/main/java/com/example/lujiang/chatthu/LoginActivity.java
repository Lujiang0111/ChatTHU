package com.example.lujiang.chatthu;

import android.graphics.LinearGradient;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    Handler mLoginHandler;
    Socket socketToServer;

    InputStream inputStream;
    String response;

    OutputStream outputStream;

    Button btnLogin, btnSend, btnReceive, btnLogout;
    TextView tvReturn;
    EditText edtTxtLogin, edtTxtServerIP, edtTxtServerPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnReceive = (Button) findViewById(R.id.btnReceive);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        tvReturn = (TextView) findViewById(R.id.tvReturn);
        edtTxtLogin = (EditText) findViewById(R.id.edtTxtLogin);
        edtTxtServerIP = (EditText) findViewById(R.id.edtTxtServerIP);
        edtTxtServerPort = (EditText) findViewById(R.id.edtTxtServerPort);
        mLoginHandler = new MyLoginHandler(this);

        // 监听登陆按钮事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socketToServer = new Socket(edtTxtServerIP.getText().toString(), Integer.parseInt(edtTxtServerPort.getText().toString()));
                            System.out.println("Socket连接状态：" + socketToServer.isConnected());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        // 监听发送按钮事件
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            outputStream = socketToServer.getOutputStream();
                            outputStream.write(edtTxtLogin.getText().toString().getBytes("utf-8") );
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        // 监听接收按钮事件
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            inputStream = socketToServer.getInputStream();
                            while (inputStream.available() > 0) {
                                byte[] buf = new byte[inputStream.available()];
                                inputStream.read(buf);
                                String strBuf = new String(buf,"utf-8");
                                response = strBuf;

                                Message msg = Message.obtain();
                                msg.what = 0;
                                mLoginHandler.sendMessage(msg);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        // 监听登出按钮事件
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            outputStream.close();
                            socketToServer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    static class MyLoginHandler extends Handler {
        WeakReference<LoginActivity> loginActivityWeakReference;
        public MyLoginHandler(LoginActivity activity) {
            loginActivityWeakReference = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity loginActivity = loginActivityWeakReference.get();
            switch (msg.what) {
                case 0:
                    loginActivity.tvReturn.setText(loginActivity.response);
                    break;
            }
        }
    }


}
