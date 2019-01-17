package com.cslg.socket.service;

import com.cslg.socket.dao.SaveData;
import com.cslg.socket.listener.SocketListener;
import com.cslg.socket.model.TemperatureSensorInfo;
import com.cslg.socket.utils.CodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class TemperatureSensorService extends AbstractService<TemperatureSensorInfo> {
    private static final String ORDER="010300000002C40B";

    private Logger logger = LoggerFactory.getLogger(TemperatureSensorService.class);

    public TemperatureSensorService(String sign){
        setSign(sign);
    }

    public TemperatureSensorService(){

    }
    //转换温度数据
    private static Integer conversionTemperatureData(String data){
        data=data.substring(6,10).toUpperCase();
        int sum=0;
        for (int i=0;i<data.length();i++){
            sum*=16;
            if (data.charAt(i)>'9'){
                sum+=((data.charAt(i)-'A'+10));
            }else {
                sum+=((data.charAt(i)-'0'));
            }
        }
        if (data.charAt(0)=='F'&&data.charAt(1)=='F'){
            return sum-65535;
        }
        return sum;
    }

    //湿度转换
    private static Integer conversionHumidityData(String data){
        data=data.substring(10,14).toUpperCase();
        int sum=0;
        for (int i=0;i<data.length();i++) {
            sum*=16;
            if (data.charAt(i)>'9'){
                sum+=((data.charAt(i)-'A'+10));
            }else {
                sum+=((data.charAt(i)-'0'));
            }
        }
        return sum;
    }

    @Override
    public boolean readData(String... str) {
        //读取数据，并解析
        byte[] bytes=new byte[1];
        StringBuilder stringBuilder=new StringBuilder();
        int k=0;
        int signSum=0;

        try {
            while (k!=9){
                if (signSum >= 6) {
                    return true;
                }
                getInputStream().read(bytes);
                String data=CodeUtil.encode(bytes);
                if (getSign().equals(data)) {
                    logger.info("心跳标志返回: {}", data);
                    signSum++;
                    continue;
                }else{
                    signSum=0;
                }
                stringBuilder.append(data);
                k++;
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            logger.error("read()超时线程即将退出");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("从流中读取数据异常", e);
            return true;
        }

        //解析16进制数据
        if (stringBuilder.length()>0){
            String data=stringBuilder.toString();
            if (!data.startsWith("010304")){
                return true;//接受到的数据有误
            }
//            //设置温度和湿度，（使用伪造数据）
            getObject().setTemperature(conversionTemperatureData(data)/10.0);
            getObject().setHumidity(conversionHumidityData(data)/10.0);
        }
        return false;
    }

    @Override
    public boolean writeData() {
        try {
            setObject(new TemperatureSensorInfo());
            getOutputStream().write(CodeUtil.hex2byte(ORDER));
            readData();
            logger.info("-------------------------------------工作线程有{}个", SocketListener.sum.get());
            handleMessage();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("指令写入流中出错", e);
            //让该工作线程回收
            return true;
        }
        return false;
    }
    //写入数据库
    @Override
    public void handleMessage() {
        //先写死了，不管啦
        if (getSign().equals("BA")){
            getObject().setLocal("能源大楼b210");
            getObject().setSensorName("室内温度传感器1");
            getObject().setFlag(0);
        }else if (getSign().equals("AD")){
            getObject().setLocal("能源大楼B210");
            getObject().setSensorName("室内温度传感器2");
            getObject().setFlag(0);
        }else if (getSign().equals("AF")){
            getObject().setLocal("能源大楼B210");
            getObject().setSensorName("室外温度传感器1");
            getObject().setFlag(1);
        }
        SaveData.saveTemperatureSensorInfo(getObject());
    }
}
