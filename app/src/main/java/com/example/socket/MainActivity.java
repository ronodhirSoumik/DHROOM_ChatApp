package com.example.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    EditText receivePortEditText, targetPortEditText, messageEditText, targetIPEditText;
    //  TextView chatText;
    Button changeColor;
    ListView chatList;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    static final int MESSAGE_READ=1;
    static final String TAG = "yourTag";

    String chatMessage = "";
    String colorCode =  "#green";

    Boolean setColor = false;

    private ChatMessage chatAdapter;
    private List<messageItem> ownChatList = new ArrayList<>();
    private List<messageItem> chatFullList = new ArrayList<>();

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    if(tempMsg.charAt(0) == '#'){
                        Log.d(TAG,"Color is: "+ tempMsg);
                        if(tempMsg.equals("#green")){
                            setColor = false;
                            Log.d(TAG, "SetColor is: "+setColor);
                            chatList.setBackgroundColor(Color.parseColor("#008577"));
                        }
                        else {
                            setColor = true;
                            Log.d(TAG, "SetColor is: "+setColor);
                            chatList.setBackgroundColor(Color.parseColor("#00574B"));

                        }

                    }
                    else {
                        //chatText.setText(tempMsg);
                        displayChat(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receivePortEditText = findViewById(R.id.receiveEditText);
        targetPortEditText = findViewById(R.id.targetPortEditText);
        messageEditText = findViewById(R.id.messageEditText);
        targetIPEditText = findViewById(R.id.targetIPEditText);
        //chatText = findViewById(R.id.chatText);

        chatList = findViewById(R.id.list_of_message);

        changeColor = findViewById(R.id.buttonColor);


    }


    public void onColorClicked(View v){
        if(setColor == false){
            colorCode = "#dark";
            setColor = true;
            chatList.setBackgroundColor(Color.parseColor("#00574B"));

        }
        else {
            colorCode = "#green";
            setColor = false;
            chatList.setBackgroundColor(Color.parseColor("#008577"));
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //String msg = "Hello Soumik";

                    sendReceive.write(colorCode.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }



    public void onStartServerClicked(View v){
        String port = receivePortEditText.getText().toString();

        serverClass = new ServerClass(Integer.parseInt(port));
        serverClass.start();
    }

    public void onConnectClicked(View v){
        String port = targetPortEditText.getText().toString();

        clientClass = new ClientClass(targetIPEditText.getText().toString(), Integer.parseInt(port));
        clientClass.start();
    }

    public void onSendClicked(View v){
        //String msg=messageEditText.getText().toString();
        chatMessage=messageEditText.getText().toString();
        Log.d(TAG,"Message is: "+ chatMessage);

        //added 3 lines
        //ownChatList.add(new messageItem(chatMessage));

        //ownAdapter = new OwnMessage(this, ownChatList);
        // chatList.setAdapter(ownAdapter);

        chatFullList.add(new messageItem(chatMessage, "10"));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //String msg = "Hello Soumik";

                    sendReceive.write(chatMessage.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        //sendReceive.write(msg.getBytes());
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;
        int port;

        public ServerClass(int port) {

            this.port = port;
        }

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(port);
                Log.d(TAG, "Waiting for client...");
                socket=serverSocket.accept();
                Log.d(TAG, "Connection established from server");
                //sendReceive=new SendReceive(socket);
                sendReceive= new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ERROR/n"+e);
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;


        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();

                //added here 2 lines
                //output = new PrintWriter(socket.getOutputStream());
                //input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);

                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] msgbytes){

            try {
                //Add here
                outputStream.write(msgbytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        int port;

        public  ClientClass(String hostAddress, int port)
        {
            this.port = port;
            this.hostAdd = hostAddress;
        }

        @Override
        public void run() {
            try {

                socket=new Socket(hostAdd, port);
                Log.d(TAG, "Client is connected to server");
                //sendReceive=new SendReceive(socket);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Can't connect from client/n"+e);
            }
        }
    }

    public void displayChat(String m){

        chatFullList.add(new messageItem(m ,"11"));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);
    }

}
