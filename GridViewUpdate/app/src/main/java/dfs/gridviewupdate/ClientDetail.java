package dfs.gridviewupdate;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kartik Sayani on 31-Oct-17.
 */

public class ClientDetail extends AppCompatActivity {

    TextView ram, max_storage, free_storage, used_storage, client;
    GridView gv;
    ArrayList<String> files = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_detail);

        ram = (TextView)findViewById(R.id.ram);
        max_storage = (TextView)findViewById(R.id.max_storage);
        free_storage = (TextView)findViewById(R.id.free_storage);
        used_storage = (TextView)findViewById(R.id.used_storage);
        client = (TextView)findViewById(R.id.cliid);

        gv = (GridView) findViewById(R.id.gridView2);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, files);
        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("File:","Selected");
            }
        });
        String cli = (String) getIntent().getSerializableExtra("ClientInfo");
        client.setText(cli);
        File file = new File(
                Environment.getExternalStorageDirectory(),
                cli+"_mem_details.txt");
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
            String line;
            int k = 0;
            while ((line = br.readLine()) != null) {
                switch (k){
                    case 0: ram.setText(line);
                            Log.d("RamReceived",line);
                            k = k+1;
                            break;
                    case 1: max_storage.setText(line);
                            k = k+1;
                            break;
                    case 2: free_storage.setText(line);
                            k = k+1;
                            break;
                    case 3: used_storage.setText(line);
                            k = k+1;
                            break;
                    default:break;
                }
            }
            br.close() ;
        }catch(FileNotFoundException fe){
            fe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        file = new File(Environment.getExternalStorageDirectory(),cli+"_file_details.txt");
        try{
            br = new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine())!=null){
                files.add(line);
            }
        }catch (FileNotFoundException fe){
            fe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }


    }

}
