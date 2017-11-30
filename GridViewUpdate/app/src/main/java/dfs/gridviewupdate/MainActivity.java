package dfs.gridviewupdate;

import android.content.Intent;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    String fileList = "";
	TextView textView1, infoip;
	GridView gv;
	Button showBtn, deleteBtn, distributeBtn;
	ArrayList<String> names = new ArrayList<String>();
	ArrayAdapter<String> adapter;
    public ServerSocket serverSocket;
	ServerDispatcher serverDispatcher = new ServerDispatcher();
	ServerSocketThread serverSocketThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		infoip = (TextView) findViewById(R.id.infoip);
		infoip.setText(getIpAddress());
		textView1 = (TextView) findViewById(R.id.textView1);
		gv = (GridView) findViewById(R.id.gridView1);
		deleteBtn = (Button) findViewById(R.id.deleteBtn);
		showBtn = (Button) findViewById(R.id.showBtn);
        distributeBtn = (Button)findViewById(R.id.distribute);
        Log.d("ADebugTag", "ReCreated");
		try{
			serverSocket = new ServerSocket(8080);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}

		serverSocketThread = new ServerSocketThread();
		serverSocketThread.start();

		//Adapter
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, names);
		gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.print("Hello");
            }
        });

		//Handle button clicks
		showBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				show();
			}
		});

		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				delete();
			}
		});

        distributeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                distribute();
            }
        });



	}

	//IP Address
	private String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "SiteLocalAddress: "
								+ inetAddress.getHostAddress() + "\n";
					}

				}

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}

		return ip;
	}

	//Add
	private void add(String name){
		if(!name.isEmpty() && name.length()>0){
			//Add
			adapter.add(name);
			//Refresh
			adapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(),"Added "+name, Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(), "Nothing to add!", Toast.LENGTH_SHORT).show();
		}
	}

	//Show
	private void show(){
		int pos = gv.getCheckedItemPosition();
        Log.d("ADebugTag", "Checked Pos: " + Integer.toString(pos));
		String name = names.get(pos);
        Log.d("ADebugTag", "Checked Pos name: " + names.get(pos));
		if(!name.isEmpty() && name.length()>0) {
            Toast.makeText(getApplicationContext(), "This will show info for " + name, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ClientDetail.class);
            intent.putExtra("ClientInfo", serverDispatcher.clients.get(pos).mSocket.getInetAddress().toString());
            startActivity(intent);
        }
		else
			Toast.makeText(getApplicationContext(),"Nothing to show",Toast.LENGTH_SHORT).show();

	}

	//Delete
	private void delete(){
		//Get Item position
		int pos = gv.getCheckedItemPosition();
        Log.d("ADebugTag", "POS: " + Integer.toString(pos));
		if(pos > -1){
			//Remove
			adapter.remove(names.get(pos));
			//Update
			adapter.notifyDataSetChanged();
			serverDispatcher.deleteClient(serverDispatcher.clients.get(pos));
			Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getApplicationContext(),"Nothing to delete!",Toast.LENGTH_SHORT).show();
		}
	}

	//Distribute split files among clients
    private void distribute(){
        Toast.makeText(getApplicationContext(),"This will distribute files over the cluster", Toast.LENGTH_SHORT).show();
    }

	//Split all large files to pieces.
	public void splitCLoud(){

        File folder = new File(Environment.getExternalStorageDirectory()+"/Cloud");
        File[] listOfFiles = folder.listFiles();
        FileSplitter fsplit = new FileSplitter();
        for(int i=0; i<listOfFiles.length; i++){
            if(!listOfFiles[i].isDirectory()){
                if(!listOfFiles[i].getName().startsWith("_")){
                    fsplit.split(listOfFiles[i]);
                    fileList = fileList + listOfFiles[i].getName() + "\n";
                }
            }
        }

    }

	public class ServerSocketThread extends Thread implements Serializable{

		@Override
		public void run(){

            splitCLoud();
            File file = new File(
                    Environment.getExternalStorageDirectory(),
                    "split_details.txt");
            byte[] bytes = fileList.toString().getBytes();
            try{
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                bos.write(bytes, 0, bytes.length);
                bos.close();
            }
            catch (FileNotFoundException fe){
                fe.printStackTrace();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }


//            File largeFile = new File(Environment.getExternalStorageDirectory()+"/Cloud","music.mp3");
//            FileSplitter f = new FileSplitter();
//            f.split(largeFile);
//            Log.d("FileList", ""+fileList);



			//Socket socket = null;
			serverDispatcher.start();
			while(true){
				try{
					final Socket socket = serverSocket.accept();
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							add(socket.getInetAddress().getHostAddress());
							Toast.makeText(MainActivity.this,
									"Somebody got connected",
									Toast.LENGTH_SHORT).show();
						}});
					FileTxThread fileTxThread = new FileTxThread(socket);
					fileTxThread.start();
					ClientInfo clientInfo = new ClientInfo();
					clientInfo.mSocket = socket;
					ClientListener clientListener =
							new ClientListener(clientInfo, serverDispatcher);
					clientInfo.mClientListener = clientListener;
					clientListener.start();
					serverDispatcher.addClient(clientInfo);
				}
				catch(IOException ioe){
					ioe.printStackTrace();
				}
			}

		}
	}

	public class ServerDispatcher extends Thread{
		public ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();

		public synchronized void addClient(ClientInfo aclient){
			clients.add(aclient);
            Log.d("ADebugTag", "Client Size init: " + Integer.toString(clients.size()));
		}

		public synchronized void deleteClient(ClientInfo aclient){
			final int index = clients.indexOf(aclient);
            Log.d("ADebugTag", "ValueIndex: " + Integer.toString(index));
			if (index != -1){
				try{
					clients.get(index).mSocket.close();
				}catch (IOException ioe){
					ioe.printStackTrace();
				}
				clients.remove(index);
                Log.d("ClientRemoval", "Just removed a client");
			}
            Log.d("ADebugTag", "Client Size: " + Integer.toString(clients.size()));
		}
		@Override
		public void run(){
			while (true){
			}

		}
    }

	public class ClientInfo implements Serializable{
		public Socket mSocket = null;
		public ClientListener mClientListener = null;
	}

	public class ClientListener extends Thread{
		private ServerDispatcher mServerDispatcher;
		private ClientInfo mClientInfo;

		public ClientListener(ClientInfo aClientInfo, ServerDispatcher aServerDispatcher)
				throws IOException
		{
			mClientInfo = aClientInfo;
			mServerDispatcher = aServerDispatcher;
			Socket socket = aClientInfo.mSocket;
		}

		@Override
		public void run()
		{
            Log.d("ADebugTag", "Client aaya ");

            while(true){
                try{
                    if(mClientInfo.mSocket==null){
                        Log.d("Error", "Somebody started me");
                        break;
                    }
                    if(mClientInfo.mSocket.getInputStream().read()==-1){
                        Log.d("ClientConnection", "Client Disconnected");

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int pos = serverDispatcher.clients.indexOf(mClientInfo);
                                if(pos>-1) {
                                    adapter.remove(names.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        mServerDispatcher.deleteClient(mClientInfo);
                        break;
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                    Log.d("Oops","Client Disconnected and deleted");
                    break;
                }
            }

		}
	}

    public class FileTxThread extends Thread {
		Socket socket;

		FileTxThread(Socket socket){
			this.socket= socket;
		}

		@Override
		public void run() {

            byte[] bytes = fileList.toString().getBytes();

			BufferedInputStream bis;
			try {
				//Send test.txt
                OutputStream os = socket.getOutputStream();
				os.write(bytes, 0, bytes.length);
				os.flush();

				final String sentMsg = "File sent to: " + socket.getInetAddress();
				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								sentMsg,
								Toast.LENGTH_SHORT).show();
					}});

				//Receive MemDetails and FileDetails

				File file = new File(
						Environment.getExternalStorageDirectory(),
                        socket.getInetAddress().toString()+"_mem_details.txt");
				bytes = new byte[4096];

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

                InputStream is = socket.getInputStream();
                int bytesRead = is.read(bytes, 0, bytes.length);
                Log.d("BytesRead", ""+bytesRead);
				bos.write(bytes, 0, bytesRead);
				bos.close();

                file = new File(
                        Environment.getExternalStorageDirectory(),
                        socket.getInetAddress().toString()+"_file_details.txt");
                bytes = new byte[4096];

                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);

                is = socket.getInputStream();
                bytesRead = is.read(bytes, 0, bytes.length);
                Log.d("BytesRead", ""+bytesRead);
                bos.write(bytes, 0, bytesRead);
                bos.close();

				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								"Receiving Finished",
								Toast.LENGTH_SHORT).show();
					}});
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

    public class FileSplitter{

        public void split(File f){
            int partCounter = 0;
            int sizeOfFiles = 50*1024 * 1024;
            byte[] buffer = new byte[sizeOfFiles];
            String fileName = f.getName();
            String splits="";
            File split_info = new File(f.getParent(),"_"+f.getName()+"_splitinfo.txt");
            if(split_info.exists() && !split_info.isDirectory()) {
                Log.d("SplitInfo", "It does exist");
            }
            else{
                Log.d("SplitInfo", "It don't exist");
                try{
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    int bytesAmount = 0;
                    while((bytesAmount = bis.read(buffer)) > 0){
                        String filePartName = String.format("%s_%s.%03d","_split", fileName, partCounter++);
                        File newFile = new File(f.getParent(), filePartName);
                        FileOutputStream out = new FileOutputStream(newFile);
                        out.write(buffer, 0, bytesAmount);
                        out.close();
                        splits = splits + filePartName+"\n";
                    }
                    bis.close();
                    fis.close();
                    FileOutputStream fos = new FileOutputStream(split_info);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    fos = new FileOutputStream(split_info);
                    bos = new BufferedOutputStream(fos);
                    bos.write(splits.getBytes(), 0, splits.getBytes().length);
                    bos.close();

                }catch(FileNotFoundException fe){
                    fe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        }
    }

}