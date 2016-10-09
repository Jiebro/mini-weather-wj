package cn.edu.pku.wangjie.miniweather;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import cn.edu.pku.wangjie.miniweather.pku.ss.wj.util.NetUtil;

/**
 * Created by admin on 2016/9/21.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private ImageView mUpdateBtn;
    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);//项目中的任何活动都应该重写Activity的onCreate方法
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);

            }
            else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show();
            }
            queryWeatherCode(cityCode);
        }
    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address);

//        final String address1 = "http://wthrcdn.etouch.cn/WeatherApi?citykey=";
//        final String address2 = URLEncoder.encode(cityCode);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection  connection = null;
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
                    parseXML(responseStr);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void parseXML(String xmlDate) {
        try {
            int fengxiangCount = 0;
            int fengliCount = 0;
            int dateCount = 0;
            int highCount = 0;
            int lowCount = 0;
            int typeCount = 0;
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlDate));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                    if (xmlPullParser.getName().equals("city")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","city:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("updatetime")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","updatetime:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("shidu")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","shidu:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("wendu")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","wendu:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("pm25")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","pm2.5:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("quality")) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","quality:  " + xmlPullParser.getText());
                    }
                    else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","fengxiang:  " + xmlPullParser.getText());
                        fengxiangCount++;
                    }
                    else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","fengli:  " + xmlPullParser.getText());
                        fengliCount++;
                    }
                    else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","date:  " + xmlPullParser.getText());
                        dateCount++;
                    }
                    else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","high:  " + xmlPullParser.getText());
                        highCount++;
                    }
                    else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","low:  " + xmlPullParser.getText());
                        lowCount++;
                    }
                    else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                        eventType = xmlPullParser.next();
                        Log.d("myapp2","type:  " + xmlPullParser.getText());
                        typeCount++;
                    }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

