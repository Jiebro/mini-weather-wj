package cn.edu.pku.wangjie.miniweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.wangjie.miniweather.R;
import cn.edu.pku.wangjie.miniweather.pku.ss.wj.app.MyApplication;
import cn.edu.pku.wangjie.miniweather.pku.ss.wj.bean.City;

/**
 * Created by admin on 2016/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackbtn;
    private ListView mListView;
    private TextView mTitleNameTv;

    private String selectCityCode;
    private MyApplication myApplication;
    private ArrayList<String> cityName = new ArrayList<>();
    private ArrayList<String> cityCodeStr = new ArrayList<>();

    private List<City> list = new ArrayList();
    private Map<String,String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mTitleNameTv = (TextView)findViewById(R.id.title_name);
        mBackbtn = (ImageView)findViewById(R.id.title_back);
        mBackbtn.setOnClickListener(this);

        myApplication = (MyApplication)getApplication();
        //data =  myApplication.getCityString(map);
        list = myApplication.getCityList();
        for(int i = 0; i < list.size(); i++){
            cityName.add(list.get(i).getCity());
            cityCodeStr.add(list.get(i).getNumber());
        }
        String[] data = new String[list.size()];
        data = cityName.toArray(data);
        mListView = (ListView)findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                SelectCity.this,android.R.layout.simple_list_item_1,data
        );
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                //cityCode = map.get(data[i]);
                //Toast.makeText(SelectCity.this,"你单击了：" + map.get(data[i]),Toast.LENGTH_LONG).show();
                Log.d("myapp",cityCodeStr.get(i));
                selectCityCode = cityCodeStr.get(i);
                mTitleNameTv.setText("当前城市：" + cityName.get(i));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                Intent i = new Intent();
                Log.d("myapp",selectCityCode);
                i.putExtra("cityCode",selectCityCode);
                setResult(RESULT_OK,i);
                finish();
                break;

            default:
                break;
        }
    }
}
