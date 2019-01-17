package com.cslg.socket.service;

import com.cslg.socket.dao.SaveData;
import com.cslg.socket.listener.SocketListener;
import com.cslg.socket.model.ControllableSocketInfo;
import com.cslg.socket.utils.CodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * 可控插座,获取总电压，总电流，总功率
 */
public class ControllableSocketService extends AbstractService<ControllableSocketInfo> {
    private Logger logger = LoggerFactory.getLogger(ControllableSocketService.class);
    //三个命令，分别为获取总电压，总电流，总功率

    private static final Map<String,String> ORDERMAP;
    static {
        ORDERMAP=new HashMap<>();
        ORDERMAP.put("总电压","010400020001900A");
        ORDERMAP.put("总电流","010400030001C1CA");
        ORDERMAP.put("总功率","010400040001700B");
    }

    public ControllableSocketService(String sign) {
        setSign(sign);
    }

    public ControllableSocketService(){

    }

    public static Integer conversionData(String data){
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
        return sum;
    }

    @Override
    public boolean readData(String... str) {
        String name=str[0];
        //读取数据，并解析
        byte[] bytes=new byte[1];
        StringBuilder stringBuilder=new StringBuilder();
        int k=0;
        int signSum=0;

        try {
            while (k!=7){
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

        String data=stringBuilder.toString();
        if (data.length()!=14){
            return true;
        }
        //根据name设置值
        if (name.equals("总电压")){
            getObject().setVoltage(conversionData(data)/10.0);
        }else if (name.equals("总电流")){
            getObject().setCurrent(conversionData(data)/100.0);
        }else if (name.equals("总功率")){
            getObject().setElectricPower(conversionData(data));
        }else {
            return true;
        }
        return false;
    }

    @Override
    public boolean writeData() {
        try {
            setObject(new ControllableSocketInfo());

            //将指令依次发送给设备
            for (String key:ORDERMAP.keySet()){
                getOutputStream().write(CodeUtil.hex2byte(ORDERMAP.get(key)));
                if (readData(key)){
                    return true;
                }
            }
           logger.info("-------------------------------------工作线程有{}个", SocketListener.sum.get());
           //经过一组循环，获取到了一组数据，存入数据库
           handleMessage();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("指令写入流中出错", e);
            //让该工作线程回收
            return true;
        }
        return false;
    }

    @Override
    public void handleMessage() {
        //保存可控插座的数据到数据库
        SaveData.saveControllableSocketInfo(getObject());
    }
}
