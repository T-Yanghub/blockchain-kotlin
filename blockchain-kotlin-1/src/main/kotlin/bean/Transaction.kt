package bean

import rsautil.SignatureUtils
import utils.RSAUtil


/**
 * ClassName:Transaction
 * Description:
 */
class Transaction(var sinatureData:String,var senderPublicKey:String,var receivePublic:String,var content:String) {

    fun transactionVerify():Boolean{
        val publicKey = RSAUtil.createPublicKey(senderPublicKey)

       return SignatureUtils.verifySignature(content,publicKey,sinatureData);
    }

}