package cn.edu.pku.wangjie.miniweather.pku.ss.wj;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.edu.pku.wangjie.miniweather.pku.ss.wj.bean.TodayWeather;

/**
 * Created by admin on 2016/11/9.
 */

public class MyHandler extends DefaultHandler{

    private String currentTag = null;   //当前读取的XML结点
    private String currentValue = null; //当前XML结点的文本值
    private TodayWeather todayWeather = null;

    int fengxiangCount = 0;
    int fengliCount = 0;
    int dateCount = 0;
    int highCount = 0;
    int lowCount = 0;
    int typeCount = 0;

    public MyHandler(){

    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        todayWeather = new TodayWeather();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        currentTag = localName;
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        currentValue = new String(ch,start,length);

            switch (currentTag) {
                case "city":
                    todayWeather.setCity(currentValue);
                    break;
                case "updatetime":
                    todayWeather.setUpdatetime(currentValue);
                    break;
                case "shidu":
                    todayWeather.setShidu(currentValue);
                    break;
                case "wendu":
                    todayWeather.setWendu(currentValue);
                    break;
                case "pm25":
                    todayWeather.setPm25(currentValue);
                    break;
                case "quality":
                    todayWeather.setQuality(currentValue);
                    break;
                case "fengxiang":
                    if (fengxiangCount == 0) {
                        todayWeather.getWeatherDetails(0).setFengxiang(currentValue);
                        fengxiangCount++;
                    }
                    break;
                case "fengli":
                    if (fengliCount == 0) {
                        todayWeather.getWeatherDetails(0).setFengli(currentValue);
                        fengliCount++;
                    }
                    break;
                case "date":
                    if (dateCount == 0) {
                        todayWeather.getWeatherDetails(0).setDate(currentValue);
                        dateCount++;
                    }
                    break;
                case "high":
                    if (highCount == 0) {
                        todayWeather.getWeatherDetails(0).setHigh(currentValue);
                        highCount++;
                    }
                    break;
                case "low":
                    if (lowCount == 0) {
                        todayWeather.getWeatherDetails(0).setLow(currentValue);
                        lowCount++;
                    }
                    break;
                case "type":
                    if (typeCount == 0) {
                        todayWeather.getWeatherDetails(0).setType(currentValue);
                        typeCount++;
                    }
                    break;
                default:
                    break;
            }
        }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    public TodayWeather getTodayWeather(){
        return todayWeather;
    }
}
