package cn.edu.pku.wangjie.miniweather.pku.ss.wj;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/12/6.
 */

public class ViewPagerAdapter extends PagerAdapter {
    private List<View> views = new ArrayList<>();
    private Context context;
    public ViewPagerAdapter(List<View> views, Context context){
        this.views = views;
        this.context = context;
    }
    @Override
    public int getCount() { //返回要滑动的视图的个数
        return views.size();
    }

    @Override
    //用于创建position所在位置的视图
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return  views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}
