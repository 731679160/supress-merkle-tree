package server;

import dataowner.LeafNode;
import dataowner.MerkleInvertedBTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.Parameter.M;

public class SP {
    private HashMap<Long, MerkleInvertedBTree> treesMap;

    public SP(HashMap<Long, MerkleInvertedBTree> treesMap) {
        this.treesMap = treesMap;
    }

    public QueryRes query(long[] queryRequest) {
        int length = queryRequest.length;
        if(length == 0){
            return null;
        }
        MerkleInvertedBTree[] keywordTrees = new MerkleInvertedBTree[length];
        for (int i = 0; i < length; i++) {
            keywordTrees[i] = treesMap.get(queryRequest[i]);
        }
        //如果有关键字没有数据，直接返回空的结果
        for (MerkleInvertedBTree merkleInvertedBTree : keywordTrees) {
            if (merkleInvertedBTree == null) {
                return null;
            }
        }

        List<Long> result = new ArrayList<>();//存储结果
        List<RoundData> rounds = new ArrayList<>();//查询时每轮的结果
        int thisRoundTag = 0;
        int nextRoundTag = 1;
        long targetId = keywordTrees[0].leafRoot.data[0];
        //每棵树都有个标记，从而标记每轮该树所扫描到的位置
        LeafNode[] recordLeaf = new LeafNode[length];
        int[] recordTag = new int[length];
        //需要初始化第一个关键字的标记，否则第一个关键字的标记可能丢失
        recordLeaf[0] = keywordTrees[0].leafRoot;
        recordTag[0] = 0;
        //开始进行每轮循环，直到扫描到某一棵树的边界跳出循环
        boolean isEnd = false;//标注是否扫描完毕需要结束
        while(!isEnd){
            RoundData thisRound = new RoundData();
            thisRound.keyword = keywordTrees[thisRoundTag].keyword;
            thisRound.target = targetId;

            //当target与左边界相等时一直循环，直到发现当前target不是查询结果
            long maxRightBound = 0;//当有多个边界信息时，选取最大的边界所对应的关键字树为下一轮对象
            int maxTargetTag = 0;
            while(true){
                LeafNode p,q = null;
                int tag1,tag2 = 0;
                //搜索当前round的边界信息
                long[] boundary = new long[2];
                LeafNode scan = recordLeaf[nextRoundTag];//用来当次扫描
                p = scan;
                int t = recordTag[nextRoundTag];//节点内部数据tag
                LeafNode pre = scan;//记录扫描当前数据前的状态
                //初始化
                if(scan == null){//如果当前树还未被扫描过
                    scan = keywordTrees[nextRoundTag].leafRoot;
                    t = 0;
                }
                tag1 = t;
                while(true){//开始搜索
                    if(t == 0 && scan.data[0] > thisRound.target){//如果该叶节点第一个数据为右边界
                        boundary[1] = scan.data[0];
                        if((maxRightBound <= boundary[1] && maxRightBound != -1) || boundary[1] == -1 ){
                            maxRightBound = boundary[1];
                            maxTargetTag = nextRoundTag;
                        }
                        if(pre == null){//树的第一个叶节点第一个数据为右边界
                            boundary[0] = -1;
                        }else{//不为第一个叶节点则为上一个节点的最后一个数据为左边界
                            boundary[0] = pre.data[M - 1];
                            q = pre;
                            tag2 = M - 1;
                        }
                        recordLeaf[nextRoundTag] = scan;
                        recordTag[nextRoundTag] = 0;
                        break;
                    }
                    boolean isFind = false;
                    for(;t < scan.dataNumber;t++){
                        if(t == 1){
                            pre = scan;//更新pre
                        }
                        if(scan.data[t] > thisRound.target){
                            isFind = true;//在该节点内部找到边界
                            boundary[1] = scan.data[t];
                            if((maxRightBound <= boundary[1] && maxRightBound != -1) || boundary[1] == -1){
                                maxRightBound = boundary[1];
                                maxTargetTag = nextRoundTag;
                            }
                            boundary[0] = scan.data[t - 1];
                            recordLeaf[nextRoundTag] = scan;
                            recordTag[nextRoundTag] = t;
                            q = scan;
                            tag2 = t - 1;
                            break;
                        }
                    }
                    if(isFind){
                        break;
                    }else{//若未找到需要扫描下一节点
                        if(t != M){//如果此时t经过上面for(;t < scan.dataNumber;t++)循环后跳出并未达到M，表示此时scan已经到达最终一个节点，此时要更新pre
                            pre = scan;
                        }
                        scan = scan.nextLeaf;
                        t = 0;
                        if(scan == null){//若到达树的末尾
                            boundary[1] = -1;//表示没有右边界
                            boundary[0] = pre.data[pre.dataNumber - 1];
                            q = pre;
                            tag2 = pre.dataNumber - 1;
                            recordLeaf[nextRoundTag] = pre;
                            recordTag[nextRoundTag] = M;
                            isEnd = true;
                            //右边界为-1时，更新最大右边界
                            maxRightBound = -1;
                            maxTargetTag = nextRoundTag;
                            break;
                        }
                    }
                }
                thisRound.boundary.add(boundary);

                //搜索当前round的VO信息
                List<VOData> thisRoundVO = keywordTrees[nextRoundTag].findHash(p,q,tag1,tag2);
                thisRound.VO.add(thisRoundVO);
                //当target与左边界相等的情况下，继续循环，否则进入下一轮
                if(thisRound.target == boundary[0]){
                    nextRoundTag = (nextRoundTag + 1) % length;
                    if(nextRoundTag == thisRoundTag){//此时所有关键字已经扫描完毕
                        result.add(thisRound.target);
                        targetId = maxRightBound;//将进入下一轮更新targetId
                        thisRoundTag = maxTargetTag;
                        break;
                    }
                }else{
                    targetId = maxRightBound;//将进入下一轮更新targetId
                    thisRoundTag = nextRoundTag;
                    break;
                }
            }
            //需要进入下一轮，更新thisRoundTag和nextRoundTag
            nextRoundTag = (thisRoundTag + 1) % length;
            rounds.add(thisRound);
        }
        //nextRoundTag的关键字树已经扫描完毕，将其后所有关键字树遍历，将没有遍历完的树的VO依次加入到结果中
        for (int i = nextRoundTag;i != (nextRoundTag - 1 + length) % length;i = (i + 1) % length){
            RoundData remainTree = new RoundData();
            remainTree.keyword = keywordTrees[i].keyword;
            LeafNode p = recordLeaf[i];
            LeafNode q = p;
            for(;q.nextLeaf != null;q = q.nextLeaf){
            }
            remainTree.VO.add(keywordTrees[nextRoundTag].findHash(p,q,recordTag[i],M));
            rounds.add(remainTree);
        }
        return new QueryRes(rounds, result);
    }
}
