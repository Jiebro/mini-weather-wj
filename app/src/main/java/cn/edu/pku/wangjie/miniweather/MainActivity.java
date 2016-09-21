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
        super.onCreate(saveInstanceState);
        setContentView(R.layout.weather_info);
    }
}
