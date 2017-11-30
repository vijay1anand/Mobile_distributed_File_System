package dfs.client;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Kartik Sayani on 31-Oct-17.
 */

public class Connected extends AppCompatActivity {

    GridView gv;
    ArrayList<String> files = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected);

        gv = (GridView) findViewById(R.id.files);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, files);
        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("File:","Selected");
            }
        });

        File file = new File(Environment.getExternalStorageDirectory(),"split_details.txt");
        try{
            Scanner s = new Scanner(file);
            while(s.hasNext()){
                adapter.add(s.next());
                adapter.notifyDataSetChanged();
            }

        }catch (FileNotFoundException fe){
            fe.printStackTrace();
        }


    }
}
