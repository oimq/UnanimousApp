package com.unanimous;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.unanimous.MyDB.MyDBHandler;

import java.util.ArrayList;
import java.util.List;

import static com.unanimous.MyUtils.str;

public class MainActivity extends AppCompatActivity {

    private final short MAIN_GLVIEW = 0;
    private final short MAIN_WEBVIEW = 1;
    private final short SUB_MAPVIEW = 0;
    private final short SUB_PATHVIEW = 1;

    private LinearLayout linearLayout;
    private MyGLSurfaceView gLView;
    private WebView webView;
    private boolean isWebHit = false;
    // What view is mainly showing?
    private short mainview = MAIN_GLVIEW;
    private short subview = SUB_MAPVIEW;
    private View subMapInflater, subPathInflater;
    LayoutInflater inflater;

    private LinearLayout.LayoutParams mainlparams;
    private LinearLayout.LayoutParams sublparams;

    MyDBHandler myDBHandler;
    MyMapHandler myMapHandler;

    private TextView start_text, goal_text, progress_text;

    private String selectedMapName = "";

    private ListView listview;
    private ArrayAdapter<String> adapter;
    private List<String> items;

    private int height, width;

    Communicator communicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.currentTimeMillis();

        // Layout Inflaters
        inflater = this.getLayoutInflater();
        subMapInflater = inflater.inflate(R.layout.l_tab, null);
        subPathInflater = inflater.inflate(R.layout.l_path, null);

        // Layout Components
        start_text = (TextView)subPathInflater.findViewById(R.id.l_text_start_pos);
        goal_text = (TextView)subPathInflater.findViewById(R.id.l_text_goal_pos);
        progress_text = (TextView)subPathInflater.findViewById(R.id.l_tab_button_back);
        listview = (ListView)subMapInflater.findViewById(R.id.l_tab_list_maps);

        // Prepare DBHandler
        myDBHandler = new MyDBHandler(this, null);
        myMapHandler = new MyMapHandler(this, myDBHandler);

        // map load
        myMapHandler.loadMapRawToFile(R.raw.navvis, "navvis");
        myMapHandler.loadMapRawToFile(R.raw.hankyoungjik, "hankyoungjik");
        myMapHandler.loadMapRawToFile(R.raw.floor4, "floor4");
        myMapHandler.loadMapFileToDB("navvis", "v");
        myMapHandler.loadMapFileToDB("hankyoungjik", "v");
        myMapHandler.loadMapFileToDB("floor4", "v");

        // preference, get height and width.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;
        int ratio_width = 8;

        // Set layouts size
        mainlparams = new LinearLayout.LayoutParams(width/ratio_width*(ratio_width-1), height);
        sublparams = new LinearLayout.LayoutParams(width/ratio_width, height);

        // first, set the linear layout
        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        setContentView(linearLayout);

        // second, we add the views to linear layout
        gLView = new MyGLSurfaceView(this, myMapHandler);
        linearLayout.addView(gLView, 0, mainlparams);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        loadWebPage("http://222.112.207.112:9000/view/?load=/static/data/indoor2.pcd");

        // third, we add the bar
        linearLayout.addView(subMapInflater, 1, sublparams);

        // save maps to list
        items = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listview.setAdapter(adapter);
        additems();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMapName = items.get(i);
            }
        });

        // Set Communicator for path findings.
        communicator = new Communicator();

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        long difference = System.currentTimeMillis() - startTime;
        Log.e("MainActivity", "onCreate - "+"Total Creation Time : "+
                String.valueOf(difference)+"ms.");
    }

    public void additems() {
        adapter.add("navvis");
        adapter.add("floor4");
        adapter.add("hankyoungjik");
        adapter.add("empty");
        adapter.notifyDataSetChanged();
    }

    public void loadWebPage(String url) {
        if (isWebHit) return;
        webView.loadUrl(url);
        isWebHit = webView.getHitTestResult().getType() != 0;
        // we should add sorry site...
    }

    public void refreshBar(short barview) {
        linearLayout.removeViewAt(1);
        switch (barview) {
            case SUB_MAPVIEW :
                linearLayout.addView(subMapInflater, 1, sublparams);
            case SUB_PATHVIEW :
                linearLayout.addView(subPathInflater, 1, sublparams);
        }
    }

    public void refreshView(short view) {
        switch (view) {
            case MAIN_GLVIEW :
                linearLayout.removeViewAt(0);
                gLView =  new MyGLSurfaceView(this, myMapHandler);
                linearLayout.addView(gLView, 0, mainlparams);
                mainview = MAIN_GLVIEW;
                break;
            case MAIN_WEBVIEW :
                linearLayout.removeViewAt(0);
                linearLayout.addView(webView, 0, mainlparams);
                mainview = MAIN_WEBVIEW;
                break;
        }
    }

    // 맵으로 돌아가기
    public void button_back_event(View v) {
        refreshBar(SUB_MAPVIEW);
        return;
    }

    // 길 찾기 버튼
    public void button_path_goto_event (View v) {
        if (myMapHandler.getMap_cnt() == 0) {
            Toast.makeText(this, "Please load the map!", Toast.LENGTH_LONG);
            return;
        }
        gLView.reset_move();
        refreshBar(SUB_PATHVIEW);
        button_update_map_event(v);
        return;
    }

    // 맵 업데이트 버튼
    public void button_update_map_event (View v) {
        if (selectedMapName.equals("empty"))
            myMapHandler.clear();
        else
            myMapHandler.loadMapDBToArray("'"+selectedMapName+"'");
        refreshView(MAIN_GLVIEW);
        return;
    }

    // 맵 다운로드
    public void button_map_download_event (View v) {
        ArrayList<String> arr = communicator.requestMap(selectedMapName);
        myMapHandler.loadMapArrayToDB(arr, selectedMapName, "v");
        return;
    }

    // 위치 선택
    private boolean flag = true;
    private float start_x = 0.0f;
    private float start_y = 0.0f;
    private float goal_x = 0.0f;
    private float goal_y = 0.0f;
    public void button_pos_event (View v) {
        float scor[] = gLView.get();
        if (flag) {
            flag = false;
            //(vec[i]-mlist[i])/abs(bias[i][0]-mlist[i])
            start_x = -scor[0];
            start_y = -scor[1];
            Log.e("button_path_goto_event", "%%% "+str(start_x)+", "+str(start_y));
            start_text.setText("Start Pos\n------"+
                    "\nstart_x\n"+str(start_x)+"\n------\nstart_y\n"+str(start_y));
        }
        else {
            flag = true;
            goal_x = -scor[0];
            goal_y = -scor[1];
            Log.e("button_path_goto_event", "%%% "+str(goal_x)+", "+str(goal_y));
            goal_text.setText("Goal Pos\n------"+
                    "\ngoal_x\n"+str(goal_x)+"\n------\ngoal_y\n"+str(goal_y));
        }

        return;
    }

    // 길 찾기 실행
    public void button_run_event (View v) {
        ArrayList<String> arr;
        myMapHandler.clear_ptr();
        progress_text.setText("찾는 중");
        if (start_x != 0 && start_y != 0 && goal_x != 0 && goal_y != 0) {
            arr = communicator.requestPath(selectedMapName,
                    start_x, start_y, goal_x, goal_y);
            myMapHandler.loadMapArrayToDB(arr, "path", "p");
            myMapHandler.loadMapDBToArray("'path'");
            refreshView(MAIN_GLVIEW);
        }
        progress_text.setText("끝내기");
        return;
    }

}
