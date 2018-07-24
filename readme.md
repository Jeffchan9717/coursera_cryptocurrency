# TxHandler.java
从交易集合中选出有效交易集合

* boolean isValidTx(Transaction tx) 要依据五个条件validate transaction

		* utxoPool是没有执行的tx集合，可以通过tx的hash值来选取里面的元素
		* 重点是搞清楚input和output的数据是哪些，signature是签在哪里。
	
* Transanction [] handleTxs(Treansaction possibleTx)将给定的possibleTx集合选出validTx集合

		* 调用isValidTx，然后将tx的input和output加入到utxoPool

# CompliantNode.java

模拟网络中正常结点的行为，即如何收发邻居结点的信息，并且尽可能避免恶意结点对自己的影响。我的实现没有考虑到鉴别恶意结点的方法。

* void setFollowees()

		* 我的实现直接将结点的followees设置为给定集合。更好的方法是进一步设置blackList，blackList可以设置为followees中不发送信息的结点。
		
* Set <Transaction\> sendToFollowers()

		* 我将Tx发送出去后，清空了自身的pendingTransactions，老师的代码没有清空。
		
* void receiveFromFollowees（）

		* 统计各个followees发送了几个tx，因为每个结点的transactions没有清空，每个followees的tx发送量只能增多，如果出现减少，则判断为恶意结点，加入blackList。
		
# BlockChain.java

实现blockChain的数据结构和方法，重点是定义结点，加入新的block时判定有效性和维护数据结构。

* blockNode(Block, UTXOPool)

		* block 块
		* parent 父结点
		* child[] 字节点
		* height 高度，genesis高度为1
		* utxoPool 未执行tx集合

* void addCoinbaseToUTXOPool(Block, UTXOPool)

		* coinbase是特殊的tx
		* 从Block取出coinbase加入utxopool

* boolean addBlock(Block)

		* 检查block有父结点，且父结点在区块链里面，否则返回false
		* 检查高度合适，否则返回false
		* 通过父结点的utxoPool和调用handleTxs来检查block的交易都是有效的，否则返回false
		* 加入新的block结点，并更新高度等。