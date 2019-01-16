package com.cslg.socket.model;

/**
 * 用来控制设备开关
 */
public class Command {
    private static final String[] commandList={
        "010F0000000801FFBED5",//全孔开
        "010F000000080100FE95",//全空关
        "01050000FF008C3A",//1号开
        "010500000000CDCA",//1号关
        "01050001FF00DDFA",//2号开
        "0105000100009C0A",//2号关
        "01050002FF002DFA",//3号开
        "0105000200006C0A",//3号关
        "01050003FF007C3A",//4号开
        "0105000300003DCA",//4号关
        "01050004FF00CDFB",//5号开
        "0105000400008C0B",//5号关
        "01050005FF009C3B",//6号开
        "010500050000DDCB",//6号关
        "01050006FF006C3B",//7号开
        "0105000600002DCB",//7号关
        "01050007FF003DFB",//8号开
        "0105000700007C0B" //8号关
    };

    public static String getCmd(int index){
        return commandList[index];
    }
}
