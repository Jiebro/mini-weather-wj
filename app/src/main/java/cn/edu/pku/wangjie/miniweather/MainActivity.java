package cn.edu.pku.wangjie.miniweather;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by admin on 2016/9/21.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);//项目中的任何活动都应该重写Activity的onCreate方法
        setContentView(R.layout.weather_info);
    }
}
