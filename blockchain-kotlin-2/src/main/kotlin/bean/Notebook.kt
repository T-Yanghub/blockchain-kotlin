package bean

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import utils.SHA256Utils
import java.io.File


/**
 * ClassName:Notebook
 * Description:
 */
class Notebook {
    val path = "note.txt";
    /*保存block的集合*/
    var blocks = ArrayList<Block>();

    init {
        /*创建时就从本地加载*/
        loadFromDesk();
    }

    /*添加封面*/

    fun addGenesis(genesis: String): Boolean {
        try {
            require(blocks.size == 0 && genesis != null, { "只能有一个封面" });
            /*首页的preHash*/
            var preHash = "0000000000000000000000000000000000000000000000000000000000000000";
            var noce = mine(genesis, preHash);
            var hash = SHA256Utils.getSHA256StrJava("${genesis}${preHash}${noce}");
            val block = Block(blocks.size, genesis, hash, preHash, noce);
            blocks.add(block);
            saveToDesk(blocks);
            return true;
        } catch (e: Exception) {
            return false
        }
    }
    /*添加node*/

    fun addNote(content: String): String {
        if (blocks.size < 1 || content == null) {
            return "添加失败"

        } else {
            var preHash = blocks.last().hash
            val nonce = mine(content, preHash)
            val hash = SHA256Utils.getSHA256StrJava("${content}${preHash}${nonce}")
            val id = blocks.size
            val block = Block(id, content, hash, preHash, nonce)
            blocks.add(block);
            saveToDesk(blocks)
            return "添加成功"
        }

    }

    /*数据的检查*/
    fun check(): String {
        var sb = StringBuffer();
        blocks.forEachIndexed { index, block ->
            if (index == 0) {

            } else {
                var nonce = block.nonce
                var content = block.content
                var preHash = block.preHash
                var hash = block.hash
                var currHash = SHA256Utils.getSHA256StrJava("${content}${preHash}${nonce}")
                if (hash.equals(currHash) || preHash.equals(blocks[index - 1].hash)) {
                    sb.append("第${index}条记录正确")
                }else{
                    sb.append("第${index}条记录正确")
                }

            }


        }
        return sb.toString()
    }

    /**
     * 篡改数据
     */
    fun modiry(index: Int, content: String) {
        trueModity(index, content)

        //修改后面所有的节点
        //1.是否是最后一个节点
        if (index + 1 < blocks.size) {//下一条不是最后一条
            //满足条件
            ((index + 1)..blocks.size - 1).forEach {
                trueModity(it, blocks[it].content)
            }
        }
        //保存到本地
        saveToDesk(blocks)
    }

    private fun trueModity(index: Int, content: String) {
        //获取数据
        val block = blocks[index]
        //修改preHash  注意
        if (index > 0) {
            block.preHash = blocks[index - 1].hash
        }
        //工作量证明
        val noce = mine(block.preHash,content)
        //篡改hash值
        val hash = SHA256Utils.getSHA256StrJava("${noce}${block.preHash}${content}")
        //修改数据
        block.content = content
        //修改hash
        block.hash = hash
        //修改工作量证明
        block.nonce = noce

    }

    /*挖矿*/
    fun mine(content: String, preHash: String): Int {
        (0..Int.MAX_VALUE).forEach {
            val hash = SHA256Utils.getSHA256StrJava("${content}${preHash}${it}")
            if (hash.startsWith("0000")) {
                return it;
            }
        }
        throw Exception("挖矿失败");
    }

    /*展示集合*/
    fun listAll(): ArrayList<Block> {
        return blocks;
    }

    /*保存到本地*/
    fun saveToDesk(blocks: ArrayList<Block>) {
        val str = JSON.toJSONString(this.blocks);
        var file = File(path);
        file.writeText(str);

    }

    /*从本地加载*/
    fun loadFromDesk() {
        var file = File(path);
        /*如果文件夹不存在就结束*/
        if (!(file.exists() || file.length() > 0)) return
        val text = file.readText()
        /*把字符串转数组换成数组*/
        var list = JSON.parseObject(text, object : TypeReference<ArrayList<Block>>() {});
        blocks = list;

    }


    fun updateNote(list:ArrayList<Block>){
        if(list.size> this.blocks.size){
            //更新自己的节点
            this.blocks.clear()
            this.blocks.addAll(list)
        }
    }
}