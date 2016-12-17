package cn.edu.pku.wangjie.miniweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.edu.pku.wangjie.miniweather.pku.ss.wj.MyHandler;
import cn.edu.pku.wangjie.miniweather.pku.ss.wj.ViewPagerAdapter;
import cn.edu.pku.wangjie.miniweather.pku.ss.wj.bean.TodayWeather;
import cn.edu.pku.wangjie.miniweather.pku.ss.wj.util.NetUtil;

/**
 * Created by admin on 2016/9/21.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private ImageView mCitySelect;
    private ImageView mUpdateBtn;
    private ProgressBar mUpdateProgressBar;
    private TextView title_cityTv,cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
                     cur_temperatureTv,temperatureTv, climateTv, windTv;
    private ImageView weatherImg, pmImg;

    private String code = "101010100";

    private IntentFilter intentFilter;
    BReceiver mReceiver;

    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    private LayoutInflater inflater;

    class BReceiver extends BroadcastReceiver{  //广播接收器 内部类

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MyService","BroadcastReceiver ... ");
        queryWeatherCode(code); //更新天气
     }
    }

    private static final int UPDATE_TODAY_WEATHER = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);//项目中的任何活动都应该重写Activity的onCreate方法
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mUpdateProgressBar = (ProgressBar)findViewById(R.id.title_update_progress);

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK",Toast.LENGTH_LONG).show();
        }
        else {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show();
        }
        initView();

        startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver = new BReceiver();
        intentFilter = new IntentFilter();  //创建广播过滤器
        intentFilter.addAction("UPDATE_TODAY_WEATHER");
        registerReceiver(mReceiver,intentFilter);   //注册
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,MyService.class));
        unregisterReceiver(mReceiver);
    }

    private void startService(){
        Intent mIntent = new Intent(this,MyService.class);
        startService(mIntent);
    }

    //初始化ViewPager
    private void initViewPager(){
        inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.first,null));
        views.add(inflater.inflate(R.layout.second,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vpAdapter.notifyDataSetChanged();
        vp = (ViewPager)findViewById(R.id.viewPager);
        vp.setAdapter(vpAdapter);

    }
    private void initView() {
        //初始化ViewPager
        initViewPager();

        title_cityTv = (TextView)findViewById(R.id.title_city_name);
        cityTv = (TextView)findViewById(R.id.city);
        timeTv = (TextView)findViewById(R.id.time);
        humidityTv = (TextView)findViewById(R.id.humidity);
        weekTv = (TextView)findViewById(R.id.week_today);
        pmDataTv = (TextView)findViewById(R.id.pm_data);
        pmQualityTv = (TextView)findViewById(R.id.pm2_5_quality);
        cur_temperatureTv = (TextView)findViewById(R.id.cur_temperature);
        temperatureTv = (TextView)findViewById(R.id.temperature);
        climateTv = (TextView)findViewById(R.id.climate);
        windTv = (TextView)findViewById(R.id.wind);
        weatherImg = (ImageView)findViewById(R.id.weather_img);
        pmImg = (ImageView)findViewById(R.id.pm2_5_img);


        title_cityTv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        weekTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        cur_temperatureTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    //更新五天天气
    private void update5dayWeather(TodayWeather todayWeather){
        vp.removeAllViews();
        views.clear();
        View page_one = inflater.inflate(R.layout.first,null);
        View page_two = inflater.inflate(R.layout.second,null);

        //——昨日天气——
        ((TextView)page_one.findViewById(R.id.yesterday_weatherType)).setText(todayWeather.getYesterdayType());
        ((TextView)page_one.findViewById(R.id.yesterday_date)).setText(todayWeather.getYesterdayDate());
        ((TextView)page_one.findViewById(R.id.yesterday_high_low)).setText(
                todayWeather.getYesterdayHigh() + "~" + todayWeather.getYesterdayLow());
        ((TextView)page_one.findViewById(R.id.yesterday_fengli)).setText(todayWeather.getYesterdayFengli());
        setWeatherTypeImage(todayWeather.getYesterdayType(),
                ((ImageView)page_one.findViewById(R.id.yesterday_weatherImg)));

        //——五日天气——1
        String day1_Date = todayWeather.getWeatherDetails(0).getDate();
        ((TextView)page_one.findViewById(R.id.day1_week)).setText(day1_Date.substring(day1_Date.length()-3,day1_Date.length()));
        ((TextView)page_one.findViewById(R.id.day1_weatherTv)).setText(todayWeather.getWeatherDetails(0).getType());
        ((TextView)page_one.findViewById(R.id.day1_temperature)).setText(
                todayWeather.getWeatherDetails(0).getHigh() + "~" + todayWeather.getWeatherDetails(0).getLow());
        ((TextView)page_one.findViewById(R.id.day1_fengli)).setText(todayWeather.getWeatherDetails(0).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(0).getType(),
                ((ImageView)page_one.findViewById(R.id.day1_weather)));

        //——五日天气——2
        String day2_Date = todayWeather.getWeatherDetails(1).getDate();
        ((TextView)page_one.findViewById(R.id.day2_week)).setText(day2_Date.substring(day2_Date.length()-3,day2_Date.length()));
        ((TextView)page_one.findViewById(R.id.day2_weatherTv)).setText(todayWeather.getWeatherDetails(1).getType());
        ((TextView)page_one.findViewById(R.id.day2_temperature)).setText(
                todayWeather.getWeatherDetails(1).getHigh() + "~" + todayWeather.getWeatherDetails(1).getLow());
        ((TextView)page_one.findViewById(R.id.day2_fengli)).setText(todayWeather.getWeatherDetails(1).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(1).getType(),
                ((ImageView)page_one.findViewById(R.id.day2_weather)));

        //——五日天气——3
        String day3_Date = todayWeather.getWeatherDetails(2).getDate();
        ((TextView)page_two.findViewById(R.id.day3_week)).setText(day3_Date.substring(day3_Date.length()-3,day3_Date.length()));
        ((TextView)page_two.findViewById(R.id.day3_weatherTv)).setText(todayWeather.getWeatherDetails(2).getType());
        ((TextView)page_two.findViewById(R.id.day3_temperature)).setText(
                todayWeather.getWeatherDetails(2).getHigh() + "~" + todayWeather.getWeatherDetails(2).getLow());
        ((TextView)page_two.findViewById(R.id.day3_fengli)).setText(todayWeather.getWeatherDetails(2).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(2).getType(),
                ((ImageView)page_two.findViewById(R.id.day3_weather)));

        //——五日天气——4
        String day4_Date = todayWeather.getWeatherDetails(3).getDate();
        ((TextView)page_two.findViewById(R.id.day4_week)).setText(day4_Date.substring(day4_Date.length()-3,day4_Date.length()));
        ((TextView)page_two.findViewById(R.id.day4_weatherTv)).setText(todayWeather.getWeatherDetails(3).getType());
        ((TextView)page_two.findViewById(R.id.day4_temperature)).setText(
                todayWeather.getWeatherDetails(3).getHigh() + "~" + todayWeather.getWeatherDetails(3).getLow());
        ((TextView)page_two.findViewById(R.id.day4_fengli)).setText(todayWeather.getWeatherDetails(3).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(3).getType(),
                ((ImageView)page_two.findViewById(R.id.day4_weather)));

        //——五日天气——5
        String day5_Date = todayWeather.getWeatherDetails(4).getDate();
        ((TextView)page_two.findViewById(R.id.day5_week)).setText(day5_Date.substring(day5_Date.length()-3,day5_Date.length()));
        ((TextView)page_two.findViewById(R.id.day5_weatherTv)).setText(todayWeather.getWeatherDetails(4).getType());
        ((TextView)page_two.findViewById(R.id.day5_temperature)).setText(
                todayWeather.getWeatherDetails(4).getHigh() + "~" + todayWeather.getWeatherDetails(4).getLow());
        ((TextView)page_two.findViewById(R.id.day5_fengli)).setText(todayWeather.getWeatherDetails(4).getFengli());
        setWeatherTypeImage(todayWeather.getWeatherDetails(4).getType(),
                ((ImageView)page_two.findViewById(R.id.day5_weather)));

        views.add(page_one);
        views.add(page_two);
        vpAdapter.notifyDataSetChanged();
    }
    private void updateTodayWeather(TodayWeather todayWeather) {

        //更新五天天气
        update5dayWeather(todayWeather);

        title_cityTv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        if (null == todayWeather.getWeatherDetails(0)){
            Log.d("five","null");
        }
        weekTv.setText(todayWeather.getWeatherDetails(0).getDate());
        pmDataTv.setText(todayWeather.getPm25());

        //质量检测
        if (todayWeather.getQuality() == null){
            pmQualityTv.setText("N/A");
        }
        else{
            pmQualityTv.setText(todayWeather.getQuality());
        }

        //pmQualityTv.setText(todayWeather.getQuality());
        cur_temperatureTv.setText("温度：" + todayWeather.getWendu() + "℃");
        temperatureTv.setText(todayWeather.getWeatherDetails(0).getHigh() + "~" + todayWeather.getWeatherDetails(0).getLow());
        climateTv.setText(todayWeather.getWeatherDetails(0).getType());
        windTv.setText("风力：" + todayWeather.getWeatherDetails(0).getFengli());

        //设置PM2.5图片
        setPM25(todayWeather.getPm25());
        //设置天气图片
        setWeatherTypeImage(todayWeather.getWeatherDetails(0).getType(),weatherImg);


        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_LONG).show();

        //恢复更新按钮
        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProgressBar.setVisibility(View.GONE);
    }

    //设置pm2.5图片
    private void setPM25(String pm25Value){
        if(pm25Value == null){ //该地方没有pm2.5信息
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }
        else {
            int pm25 = Integer.parseInt(pm25Value);
            if (pm25 >= 0 && pm25 <= 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25 >= 51 && pm25 <= 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25 >= 101 && pm25 <= 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25 >= 151 && pm25 <= 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25 >= 201 && pm25 <= 300) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            } else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }
    }

    //设置天气图片
    private void setWeatherTypeImage(String weatherType, ImageView imageView){
        switch (weatherType) {
            case "暴雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                imageView.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                imageView.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("cityCode",code);
            startActivityForResult(i,1);
        }
        if(view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code",code);
            Log.d("myWeather",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather","网络OK");

                queryWeatherCode(cityCode);
            }
            else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            code = newCityCode;
            Log.d("myWeather","选择的城市的代码为：" + newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }
            else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(this,"网络挂了",Toast.LENGTH_LONG).show();
            }
        }
    }

    //SAX 解析 XML文件 预处理
    private void saxQueryWeatherCode(final String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection  connection = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();

                    Log.d("myWeather","prepare parse");
                    todayWeather = saxParseXml(in);

                    if (todayWeather == null)
                        Log.d("myWeather","todayweather is null");
                    if (todayWeather != null) {
                        Log.d("myapp2",todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //SAX 解析 XML
    private TodayWeather saxParseXml(InputStream in) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            MyHandler myHandler = new MyHandler();
            parser.parse(in, myHandler);
            TodayWeather todayWeather = myHandler.getTodayWeather();
            in.close();
            return todayWeather;
        }
        catch (Exception e){

        }
        return null;
    }
    public void queryWeatherCode(final String cityCode){

        //设置旋转更新按钮
        mUpdateBtn.setVisibility(View.INVISIBLE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);

        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        //final String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection  connection = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);
                    Thread.sleep(1000); //为了更新按钮旋转使天气查询延迟一秒
                    todayWeather = parseXML(responseStr);
                    //todayWeather = parseJson(responseStr);
                    if (todayWeather != null) {
                        //Log.d("myapp2",todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }


    private TodayWeather parseXML(String xmlDate) {
        TodayWeather todayWeather = null;
        try {
            int fengxiangCount = 0;
            int fengliCount = 0;
            int typeCount = 0;
            int yesterdayFX = 0;
            int yesterdayFL = 0;
            int yesterdayType = 0;
            int i = 0;  //五天天气索引
            boolean isFirstFengli = true; //XML文件中第一次出现风力风向的信息忽略，统一处理五天天气
            boolean isFirstFengxiang = true;

            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlDate));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            boolean isDay = true;
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                String[] time = xmlPullParser.getText().split(":");
                                isDay = isDayFun(time[0]);
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang")) {
                                eventType = xmlPullParser.next();
                                if ( !isFirstFengxiang) {
                                    if (isDay) {    //如果是白天，直接记下遇到的第一个风向数据
                                        if(fengxiangCount == 0) {
                                            todayWeather.getWeatherDetails(i).setFengxiang(xmlPullParser.getText());
                                            Log.d("five", i + ":" + xmlPullParser.getText());
                                            fengxiangCount++;
                                        }
                                        else {
                                            fengxiangCount = 0;
                                        }
                                    }
                                    else if(fengxiangCount == 0){   //是夜晚的话，就跳过前一个白天的风向数据
                                        fengxiangCount++;
                                    }else{
                                        todayWeather.getWeatherDetails(i).setFengxiang(xmlPullParser.getText());
                                        Log.d("five",i + ":" + xmlPullParser.getText());
                                        fengxiangCount = 0;
                                    }
                                }
                                isFirstFengxiang = false;
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                eventType = xmlPullParser.next();
                                if ( !isFirstFengli) {
                                    if (isDay) {    //如果是白天，直接记下遇到的第一个风向数据
                                        if (fengliCount == 0) {
                                            todayWeather.getWeatherDetails(i).setFengli(xmlPullParser.getText());
                                            Log.d("five", i + ":" + xmlPullParser.getText());
                                            i++;
                                            fengliCount++;
                                        }
                                        else{
                                            fengliCount = 0;
                                        }
                                    }
                                    else if(fengliCount == 0){   //是夜晚的话，就跳过前一个白天的风向数据
                                        fengliCount++;
                                    }else{
                                        todayWeather.getWeatherDetails(i).setFengli(xmlPullParser.getText());
                                        fengliCount = 0;

                                        Log.d("five",i + ":" +xmlPullParser.getText());
                                        i++;    //风力是五天天气每组数据的最后一个值
                                    }
                                }
                                isFirstFengli = false;

                            } else if (xmlPullParser.getName().equals("date")) {
                                eventType = xmlPullParser.next();
                                todayWeather.getWeatherDetails(i).setDate(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high") ) {
                                eventType = xmlPullParser.next();
                                todayWeather.getWeatherDetails(i).setHigh(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low")) {
                                eventType = xmlPullParser.next();
                                todayWeather.getWeatherDetails(i).setLow(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("type") ) {
                                eventType = xmlPullParser.next();
                                if(isDay) {
                                    if (typeCount == 0) {
                                        todayWeather.getWeatherDetails(i).setType(xmlPullParser.getText());
                                        Log.d("five", i + ":" + xmlPullParser.getText());
                                        typeCount++;
                                    }
                                    else{
                                        typeCount = 0;
                                    }
                                }else if (typeCount == 0){
                                    typeCount++;
                                }else {
                                    todayWeather.getWeatherDetails(i).setType(xmlPullParser.getText());
                                    Log.d("five",i + ":" + xmlPullParser.getText());
                                    typeCount = 0;
                                }
                            }else if(xmlPullParser.getName().equals("date_1")){
                                eventType = xmlPullParser.next();
                                String date1 = xmlPullParser.getText();
                                todayWeather.setYesterdayDate(date1.substring(date1.length() - 3,date1.length()).trim());
                            }else if (xmlPullParser.getName().equals("high_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayHigh(xmlPullParser.getText().substring(2).trim());
                            }else if (xmlPullParser.getName().equals("low_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayLow(xmlPullParser.getText().substring(2).trim());
                            }else if (xmlPullParser.getName().equals("type_1")){
                                eventType = xmlPullParser.next();
                                if(isDay) {
                                        todayWeather.setYesterdayType(xmlPullParser.getText());
                                        yesterdayType++;
                                }else if (yesterdayType == 0){
                                    yesterdayType++;
                                }else {
                                    todayWeather.setYesterdayType(xmlPullParser.getText());
                                }
                            }else if (xmlPullParser.getName().equals("fl_1")){
                                eventType = xmlPullParser.next();
                                if(isDay) {
                                    todayWeather.setYesterdayFengli(xmlPullParser.getText());
                                    yesterdayFL++;
                                }else if (yesterdayFL == 0){
                                    yesterdayFL++;
                                }else {
                                    todayWeather.setYesterdayFengli(xmlPullParser.getText());
                                }
                            }else if (xmlPullParser.getName().equals("fx_1")){
                                eventType = xmlPullParser.next();
                                if(isDay) {
                                    todayWeather.setYesterdayFengxiang(xmlPullParser.getText());
                                    yesterdayFX++;
                                }else if (yesterdayFX == 0){
                                    yesterdayFX++;
                                }else {
                                    todayWeather.setYesterdayFengxiang(xmlPullParser.getText());
                                }
                            }


                        }
                            break;
                            case XmlPullParser.END_TAG:
                                break;
                    }
                eventType = xmlPullParser.next();
                }

            } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return todayWeather;
    }

    //解析json文件
//    private TodayWeather parseJson(String jsonData){
//        TodayWeather todayWeather = new TodayWeather();
//        try {
//                JSONObject jsonObject = new JSONObject(jsonData);
//                JSONObject dataObject = jsonObject.getJSONObject("data");
//
//                todayWeather.setWendu(dataObject.getString("wendu"));
//                JSONArray forecastArray = new JSONArray(dataObject.getString("forecast"));
//                JSONObject today = forecastArray.getJSONObject(0);
//                todayWeather.setFengli(today.getString("fengli"));
//                todayWeather.setFengxiang(today.getString("fengxiang"));
//                todayWeather.setHigh(today.getString("high"));
//                todayWeather.setLow(today.getString("low"));
//                todayWeather.setType(today.getString("type"));
//                todayWeather.setDate(today.getString("date"));
//                todayWeather.setPm25(dataObject.getString("aqi"));
//                todayWeather.setCity(dataObject.getString("city"));
//                return todayWeather;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    //判断是白天还是晚上
    public boolean isDayFun(String updateTime){
        int time = Integer.valueOf(updateTime);
        if((time>=0 && time<6) ||(time >=18 && time<24)){
            return false;
        } else {
            return true;
        }
    }


}


