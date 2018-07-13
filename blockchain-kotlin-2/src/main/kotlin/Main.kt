
import bean.Massege
import bean.Notebook
import bean.Transaction
import com.alibaba.fastjson.JSON
import io.ktor.application.call
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import webSocket.MyClient
import webSocket.MyServer

/**
 * ClassName:Main
 * Description:
 */

val server = MyServer(70);
val  notebook = Notebook();
//客户端地址
val clientList = listOf(MyClient("ws://localhost:80"))
fun main(args: Array<String>) {
    //启动websocket服务端
    Thread(server).start()
    //客户端连接服务器
    Thread {
        Thread.sleep(10000)
        clientList.forEach { it.connect() }
    }.start()




    val server1 = embeddedServer(Netty, port = 71) {
        routing {

            static("static") {
                //路由地址
                resources("static")//静态界面放在static文件夹下
            }
            post("addGenesis") {

                val genesis = call.receiveParameters().get("genesis")
                val s = notebook.addGenesis(genesis!!)

                call.respondText(s.toString())
            }
            post("addNote") {

                val content = call.receiveParameters().get("content")
                val s = notebook.addNote(content!!)
                call.respondText(s)

            }
            get("check") {

                val check = notebook.check()

                call.respondText(check)

            }
            //串改数据
            post("modify") {
                //串改那一条数据(index)? 串改的数据(content)
                val params = call.receiveParameters()//只能从管道里面获取一次
                val index = params.get("index")!!.toInt()
                val content = params.get("content")!!
                //修改笔记本数据
                notebook.modiry(index, content)

                //返回修改成功
                call.respondText("篡改数据成功")
            }
            post("/transaction") {
                /*获取页面数据*/
               val transtr = call.receive<String>();
                /*把页面数据转换transaction对象*/
                val transaction = JSON.parseObject(transtr, Transaction::class.java);
                /*把交易广播出去 让节点开始挖矿*/
                val msg= Massege(1,1,transaction,null);
                val msgStr = JSON.toJSONString(msg)
                server.broadcast(msgStr);
                if (transaction.transactionVerify()){
                       notebook.addNote(transaction.content);
                    val massege= Massege(2,1,null,notebook.blocks)

                    server.broadcast(JSON.toJSONString(massege));
                }else{
                    println("数据有问题");
                }

                call.respondText("请求成功");
                println("请求成功");
                //  call.respondText("")

            }



        }

    }
    server1.start(wait = true)
}