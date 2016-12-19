package cn.edu.pku.wangjie.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ArrayAdapter<String> adapter;
    private String[] data;
    private String[] backupData;

    private List<City> list = new ArrayList();
    private HashMap<String,String> cityMap = new HashMap<>();   //city为键
    private HashMap<String,String> cityMap2 = new HashMap<>();  //number为键
    private EditText m_EditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mTitleNameTv = (TextView)findViewById(R.id.title_name);
        mBackbtn = (ImageView)findViewById(R.id.title_back);
        mBackbtn.setOnClickListener(this);

        //记录下原来的citycode，以防用户进入选择城市页面后不点击任何城市，selectCityCode为空
        selectCityCode = this.getIntent().getStringExtra("cityCode");
        Log.d("myWeather","原cityCode：" + selectCityCode);
        myApplication = (MyApplication)getApplication();
        list = myApplication.getCityList();

        for(int i = 0; i < list.size(); i++){
            cityName.add(list.get(i).getCity());
            cityMap.put(list.get(i).getCity(),list.get(i).getNumber());
            cityMap2.put(list.get(i).getNumber(),list.get(i).getCity());
        }
        mTitleNameTv.setText("当前城市：" + cityMap2.get(selectCityCode));

        data = new String[list.size()];
        backupData = new String[list.size()];
        data = cityName.toArray(data);
        System.arraycopy(data,0,backupData,0,data.length);
        mListView = (ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(
                SelectCity.this,android.R.layout.simple_list_item_1,data
        );
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                String s = (String)parent.getItemAtPosition(i);
                Log.d("ListView",s);
                selectCityCode = cityMap.get(s);
                mTitleNameTv.setText("当前城市：" + cityMap2.get(selectCityCode));
                Toast.makeText(SelectCity.this,"您选择了：" + cityMap2.get(selectCityCode),Toast.LENGTH_SHORT).show();
            }
        });

        m_EditText = (EditText)findViewById(R.id.search_edit);
        m_EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("EditText","beforeTextChanged" );
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String searchContent = s.toString();
                    ArrayList<String> newDataList = new ArrayList<String>();
                    for(int i = 0; i < backupData.length; i++){
                        Log.d("EditText",backupData[i].substring(0,searchContent.length()));
                        if (backupData[i].substring(0,searchContent.length()).equals(searchContent)){
                            newDataList.add(backupData[i]);
                        }
                    }
                    int j = 0;
                    for (; j < newDataList.size(); j++){
                        data[j] = newDataList.get(j);
                    }
                    for (;j < data.length; j++){
                        data[j]="";
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("EditText","onTextChanged" + s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

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
