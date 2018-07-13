package webSocket

import bean.Massege
import com.alibaba.fastjson.JSON
import notebook
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import server
import java.lang.Exception
import java.net.URI


/**
 * ClassName:MyClient
 * Description:
 */
class MyClient(path:String) : WebSocketClient(URI(path)) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        println("节点1客户端连接打开,连接服务器的端口:${uri.port}")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("节点1客户端连接关闭")
    }

    override fun onMessage(message: String?) {
     /*把收到的数据解析成一个Massege对象*/
        val msg = JSON.parseObject(message, Massege::class.java)
        /*判断msg的类型*/
        if(msg.type==1){
            /*任务广播*/
            val transaction = msg.transaction;
            val result = transaction?.transactionVerify()
            if (result!!){
                /*添加到集合*/
                notebook.addNote(transaction.content);
                var m= Massege(2,1,null,notebook.blocks);
                /*把数据广播出去供其他节点同步*/
                server.broadcast(JSON.toJSONString(m));
            }
        }
        if (msg.type==2){
            /*节点同步*/
notebook.updateNote(msg.list!!);
        }

    }

    override fun onError(ex: Exception?) {
        println("节点1客户端连接失败")
    }
}