package com.example.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    Button hostButton, connectButton;
    //  TextView chatText;
    Button changeColor;
    ListView chatList;

    Dialog dialog, sec_dialog;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    static final int MESSAGE_READ=1;
    static final String TAG = "DHROOM";

    String textFromFile= "";

    String chatMessage = "";
    String colorCode =  "#*white";

    public static String deviceIP = "";

    Boolean setColor = false;

    private ChatMessage chatAdapter;
    private List<messageItem> ownChatList = new ArrayList<>();
    private List<messageItem> chatFullList = new ArrayList<>();

    private static final String FILE_NAME = "saveChat.txt";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String fullMsg = new String(readBuff, 0, msg.arg1);
                    String[] temp = fullMsg.split("016@74");
                    String tempMsg = temp[0];

                    if(tempMsg.charAt(0) == '#') {
                        if (tempMsg.charAt(1) == '*') {

                            Log.d(TAG, "Color is: " + tempMsg);
                            if (tempMsg.equals("#*white")) {
                                setColor = false;
                                Log.d(TAG, "SetColor is: " + setColor);
                                //chatList.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                chatList.setBackgroundResource(R.drawable.a);
                            } else {
                                setColor = true;
                                Log.d(TAG, "SetColor is: " + setColor);
                               // chatList.setBackgroundColor(Color.parseColor("#07090F"));
                                chatList.setBackgroundResource(R.drawable.b);

                            }

                        }
                        if (tempMsg.charAt(1) == '@') {
                            tempMsg = tempMsg.replace("#@", "");
                            Toast.makeText(getApplicationContext(), "File saved in " + getFilesDir(), Toast.LENGTH_SHORT).show();
                            String fileText = tempMsg;
                            writeToFile("file", fileText, true);
                        }
                    }

                    else {
                        String senderName = temp[1];
                        //chatText.setText(tempMsg);
                        displayChat(tempMsg, senderName);
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

/*        receivePortEditText = findViewById(R.id.receiveEditText);
        targetPortEditText = findViewById(R.id.targetPortEditText);
        messageEditText = findViewById(R.id.messageEditText);
        targetIPEditText = findViewById(R.id.targetIPEditText);*/
        //chatText = findViewById(R.id.chatText);
        messageEditText = findViewById(R.id.messageEditText);

        chatList = findViewById(R.id.list_of_message);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        changeColor = findViewById(R.id.buttonColor);

        //now changeddialog = new Dialog(this);
        sec_dialog = new Dialog(this);

        verifyDataFolder();
        verifyStoragePermissions();

    }

    public void onConnectFriendClicked(View view){
       //now changed dialog.setContentView(R.layout.contact_user);

        //now changedreceivePortEditText = dialog.findViewById(R.id.receiveEditText);
     //   targetPortEditText = dialog.findViewById(R.id.targetPortEditText);
     //   targetIPEditText = dialog.findViewById(R.id.targetIPEditText);

        //now changedhostButton = dialog.findViewById(R.id.hostButton);
    //    connectButton = dialog.findViewById(R.id.connectButton);

/* now changed       hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = receivePortEditText.getText().toString();

                serverClass = new ServerClass(Integer.parseInt(port));
                serverClass.start();
            }
        });*/

/*        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = targetPortEditText.getText().toString();

                clientClass = new ClientClass(targetIPEditText.getText().toString(), Integer.parseInt(port));
                clientClass.start();
            }
        });*/
        serverClass = new ServerClass(Integer.parseInt("8080"));
        serverClass.start();


       //now changed dialog.show();

    }

    public void onConnectServer(View V){
        sec_dialog.setContentView(R.layout.contact_server);


        targetPortEditText = sec_dialog.findViewById(R.id.targetPortEditText);
        targetIPEditText = sec_dialog.findViewById(R.id.targetIPEditText);

        connectButton = sec_dialog.findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = targetPortEditText.getText().toString();

                clientClass = new ClientClass(targetIPEditText.getText().toString(), Integer.parseInt(port));
                clientClass.start();
            }
        });


        sec_dialog.show();

    }


    public void onColorClicked(View v){
        if(setColor == false){
            colorCode = "#*dark";
            setColor = true;
            //chatList.setBackgroundColor(Color.parseColor("#07090F"));
            chatList.setBackgroundResource(R.drawable.b);

        }
        else {
            colorCode = "#*white";
            setColor = false;
            //chatList.setBackgroundColor(Color.parseColor("#FFFFFF"));
            chatList.setBackgroundResource(R.drawable.a);
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

    public void onSaveClicked(View v) {

/*        FileOutputStream fos = null;
        String newline = "\n";

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            // PrintWriter pw = new PrintWriter(fos);
            for (int i = 0; i < chatFullList.size(); i++) {
                // pw.write("It is here");
                String l = chatFullList.get(i).getIp().concat(" : ");
                fos.write(l.getBytes());
                fos.write(chatFullList.get(i).getMsg().getBytes());
                fos.write(newline.getBytes());
                Log.d(TAG, chatFullList.get(i).getIp() + " : " + chatFullList.get(i).getMsg() + "\n");
            }

            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
        //newly added for save conversation in a particular directory

        String path = Environment.getExternalStorageDirectory().toString();
        File file = null;
        String newline = "\n";


        file = new File(path + "/Socket/Save Conversations", FILE_NAME);

        Toast.makeText(this, "Chat conversation is saved successfully", Toast.LENGTH_SHORT).show();

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            for (int i = 0; i < chatFullList.size(); i++) {
                String a ="";
                // pw.write("It is here");
               // String l = chatFullList.get(i).getIp().concat(" : ");
                String l = chatFullList.get(i).getIp();
                if(l.equals("10")){
                    a = a.concat(chatFullList.get(i).getName() + " : ");
                }
                else a = a.concat(chatFullList.get(i).getName() + " : ");
                stream.write(a.getBytes());
                stream.write(chatFullList.get(i).getMsg().getBytes());
                stream.write(newline.getBytes());
                Log.d(TAG, chatFullList.get(i).getIp() + " : " + chatFullList.get(i).getMsg() + "\n"); }
                stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/*
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
*/
    public void onSendTextFileClicked(View v){
        generateFileManagerWindow();
    }

    private void generateFileManagerWindow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 7);
    }


    public void onSendClicked(View v){
        //String msg=messageEditText.getText().toString();
        chatMessage=messageEditText.getText().toString();
        Log.d(TAG,"Message is: "+ chatMessage);

        //added 3 lines
        //ownChatList.add(new messageItem(chatMessage));

        //ownAdapter = new OwnMessage(this, ownChatList);
        // chatList.setAdapter(ownAdapter);
        Long time= System.currentTimeMillis();
        String timeMill = " "+time.toString();

        chatFullList.add(new messageItem(chatMessage, "10", deviceIP, timeMill));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //String msg = "Hello Soumik";
                    //String name = "Abser";

                    String name = "Soumik";

                    chatMessage = chatMessage.concat("016@74"+deviceIP);

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
                //Toast.makeText(getApplicationContext(), "Server Started........waiting for client", Toast.LENGTH_SHORT).show();
                socket=serverSocket.accept();
               //Toast.makeText(getApplicationContext(), "Connection established from server", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Connection established from server");
                Log.d(TAG, "From Server: "+socket.getLocalAddress().toString());
                deviceIP = socket.getLocalAddress().toString();
                deviceIP = deviceIP.replace("/","");

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
                //Toast.makeText(getApplicationContext(), "Client is connected to server", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Client is connected to server");
                Log.d(TAG, "From Client: "+socket.getLocalAddress().toString());
                deviceIP = socket.getLocalAddress().toString();
                deviceIP = deviceIP.replace("/","");
                //sendReceive=new SendReceive(socket);
                sendReceive = new SendReceive(socket);
                sendReceive.start();


            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Can't connect from client/n"+e);
            }
        }
    }

    public void displayChat(String m, String name){
        Long time= System.currentTimeMillis();
        String timeMill = " "+time.toString();

        chatFullList.add(new messageItem(m ,"11",  name,timeMill));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);
    }

    //added here for file send receive

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {

            Uri uri = data.getData();
            String fileText = getTextFromUri(uri);
            textFromFile = "#@"+fileText;

            new Thread(new Runnable() {
                @Override
                public void run() {


                    //String msg = "124@@@"+fileText;

                    Log.d(TAG, textFromFile);

                    sendReceive.write(textFromFile.getBytes());
                }
            }).start();

        }


    }

    public String getTextFromUri(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append("\n"+line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }
    private void verifyDataFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Socket");
        File folder1 = new File(folder.getPath() + "/Save Conversations");
        File folder2 = new File(folder.getPath() + "/Files");
        if(!folder.exists() || !folder.isDirectory()) {
            folder.mkdir();
            folder1.mkdir();
            folder2.mkdir();
        }
        else if(!folder1.exists())
            folder1.mkdir();
        else if(!folder2.exists())
            folder2.mkdir();
    }
    private void writeToFile(String fileName, String data, boolean timeStamp) {

        Long time= System.currentTimeMillis();
        String timeMill = " "+time.toString();
        String path = Environment.getExternalStorageDirectory().toString();
        File file = null;
        if(timeStamp)
            file = new File(path+"/Socket/Files", fileName+timeMill+".txt");
        else
            file = new File(path+"/Socket/Save Conversations", fileName);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(data.getBytes());
            stream.close();
            // showToast("file saved in: "+file.getPath());
            Toast.makeText(this, "Saving Your Conversation.....", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

}
