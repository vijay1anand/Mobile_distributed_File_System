package dfs.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;

public class MainActivity extends AppCompatActivity {

    EditText editTextAddress;
    Button buttonConnect;
    TextView textPort;
    Socket socket = null;

    static final int SocketServerPORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.address);
        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);
        buttonConnect = (Button) findViewById(R.id.connect);

        buttonConnect.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                ClientRxThread clientRxThread =
                        new ClientRxThread(
                                editTextAddress.getText().toString(),
                                SocketServerPORT);
                buttonConnect.setText("Connection ->");
                clientRxThread.start();
            }});
    }

    public float getRamSize(){
        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        float totalMemory = memInfo.totalMem;
        totalMemory = totalMemory/(1024*1024);
        Log.d("Max Ram", ""+totalMemory);
        return totalMemory;
    }

    public float getTotMem()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        float   Total  = ( (long) statFs.getBlockCountLong() * (long) statFs.getBlockSizeLong());
        return Total/(1024*1024);
    }

    public float getFreeMem()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        float   Free   = (statFs.getAvailableBlocksLong() * (long) statFs.getBlockSizeLong());
        return Free/(1024*1024);
    }

    public float getBusyMem()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        float   Total  = ((long) statFs.getBlockCountLong() * (long) statFs.getBlockSizeLong());
        float   Free   = (statFs.getAvailableBlocksLong()   * (long) statFs.getBlockSizeLong());
        float   Busy   = Total - Free;
        return Busy/(1024*1024);
    }

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            if(socket!=null) {
                Log.d("ADebugTag", "Already Connected ");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Already Connected",
                                Toast.LENGTH_LONG).show();
                    }
                });
                Intent intent = new Intent(MainActivity.this, Connected.class);
                startActivity(intent);
            }
            else{
                try {
                    socket = new Socket(dstAddress, dstPort);
                    File file = new File(
                            Environment.getExternalStorageDirectory(),
                            "split_details.txt");

                    byte[] bytes = new byte[102400];
                    InputStream is = socket.getInputStream();
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int bytesRead = is.read(bytes, 0, bytes.length);
                    bos.write(bytes, 0, bytesRead);
                    bos.close();

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Receiving Finished",
                                    Toast.LENGTH_LONG).show();
                        }});

                    //Send MemInfo

                    String result = getRamSize()+"M\n";
                    result = result + getTotMem()+"M\n";
                    result = result + getFreeMem()+"M\n";
                    result = result + getBusyMem()+"M\n";
                    byte[] bytesi = result.getBytes();
                    file = new File(
                            Environment.getExternalStorageDirectory(),
                            "mem_info.txt");
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos);
                    bos.write(bytesi, 0, bytesi.length);
                    bos.close();

                    OutputStream os = socket.getOutputStream();
                    os.write(bytesi, 0, bytesi.length);
                    Log.d("BytesSent",""+bytesi.length);
                    os.flush();

                    result = "";

                    File folder = new File(Environment.getExternalStorageDirectory()
                            + "/Cloud");
                    if(!folder.isDirectory()) {
                        if (folder.mkdir()) {
                            Log.d("ADebugTag", "Successfully created the parent dir:"
                                    + folder.getName());
                        } else {
                            Log.d("ADebugTag", "Failed to create the parent dir:"
                                    + folder.getName());
                        }
                    }

                    //appending file names
                    File[] listOfFiles = folder.listFiles();

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isFile()) {
                            result+="" + listOfFiles[i].getName()+"\n";
                        } else if (listOfFiles[i].isDirectory()) {
                            //System.out.println("Directory " + listOfFiles[i].getName());
                            continue;
                        }
                    }//till

                    bytesi = result.getBytes();
                    file = new File(
                            Environment.getExternalStorageDirectory(),
                            "file_info.txt");
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos);
                    bos.write(bytesi, 0, bytesi.length);
                    bos.close();

                    os = socket.getOutputStream();
                    os.write(bytesi, 0, bytesi.length);
                    Log.d("BytesSent",""+bytesi.length);
                    os.flush();
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Sending Finished",
                                    Toast.LENGTH_LONG).show();
                        }});
                    ClientRunner cli = new ClientRunner();
                    cli.start();
                    Intent intent = new Intent(MainActivity.this, Connected.class);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                    final String eMsg = "Something wrong: " + e.getMessage();
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    eMsg,
                                    Toast.LENGTH_LONG).show();
                        }});
                }
            }
        }
    }

    public class ClientRunner extends Thread{


        @Override
        public void run() {
            while(true){
                Log.d("Connection", "Working");
                try {
                    if(socket.getInputStream().read()==-1){
                        Log.d("Connection", "Closed");
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try{
                socket.close();
                socket = null;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonConnect.setText("Connect");
                        Toast.makeText(MainActivity.this,
                                "Disconnected",
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }


        }
    }
}