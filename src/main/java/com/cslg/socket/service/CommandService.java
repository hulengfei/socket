package com.cslg.socket.service;

import com.cslg.socket.listener.SocketListener;
import com.cslg.socket.model.Command;
import com.cslg.socket.utils.CodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



/**
 * 用来发送命令给硬件设备
 * 新开一个socket供web连接
 */
public class CommandService extends AbstractService{
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    private Socket socket=null;

    public CommandService(String sign){
        setSign(sign);
    }

    public CommandService(){

    }

    public void setSocket(Socket socket){
        this.socket=socket;
    }

    public Integer analysisCmd(String data){
        data=data.toUpperCase();
        if (data.length()!=2){
            return null;
        }
        if (data.charAt(1)>='0'&&data.charAt(1)<='9'){
            return data.charAt(1)-'0';
        }else if (data.charAt(1)>='A'&&data.charAt(1)<='F'){
            return data.charAt(1)-'A'+10;
        }
        return null;
    }

    public void dealCommand() throws IOException {
        InputStream inputStream=socket.getInputStream();
        OutputStream outputStream=socket.getOutputStream();
        byte[] bytes=new byte[1];
        while(true){
            //读web发来的指令
            try {
                try {
                    inputStream.read(bytes);
                }catch (IOException io){
                    return;
                }

                String data= CodeUtil.encode(bytes);
                //解析指令
                Integer flag=analysisCmd(data);
                //发送指令
                String cmd= Command.getCmd(flag);
                Socket controlledSocket=SocketListener.getControlledSocketMap().get("AE");
                if (controlledSocket==null){
                    outputStream.write(CodeUtil.hex2byte("03"));//设备不在线
                    continue;
                }
                OutputStream controlledOutputStream=controlledSocket.getOutputStream();
                controlledOutputStream.write(CodeUtil.hex2byte(cmd));
                outputStream.write(CodeUtil.hex2byte("01"));//成功
                logger.info("发送指令： "+cmd+"成功--------、");
            }catch (IOException e){
                try {
                    if (socket.isClosed()){
                        return;
                    }

                    if (SocketListener.getControlledSocketMap().get("AE").isClosed()){
                        //设备不在线，移除
                        SocketListener.getControlledSocketMap().remove("AE");
                        outputStream.write(CodeUtil.hex2byte("03"));//发送失败
                        continue;
                    }
                    outputStream.write(CodeUtil.hex2byte("02"));//发送失败
                    continue;
                }catch (IOException e1){
                    e1.printStackTrace();
                }
                return;
            }
        }

    }

    @Override
    public boolean readData(String... str) {
        return false;
    }

    @Override
    public boolean writeData() {
        return false;
    }

    @Override
    public void handleMessage() {

    }
}
