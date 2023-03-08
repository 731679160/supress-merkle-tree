package dataowner;

import server.VOData;
import utils.SHA;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static utils.Parameter.M;


public class MerkleInvertedBTree {
    public TreeNode root;
    public LeafNode leafRoot;//第一个叶节点
    public BigInteger rootHash;//树的根哈希
    public long keyword;

    public MerkleInvertedBTree(long keyword, List<Long> ids){
        this.keyword = keyword;
        for (Long id : ids) {
            addData(SHA.HASHData(id.toString()), id);
        }
    }

    //树的插入算法
    public void addData(BigInteger hashData,long data){
        StringBuilder str = new StringBuilder();//存储一个节点内的所有数据的值，从而计算哈希
        if(this.root == null){//树根为空时
            LeafNode node = new LeafNode();
            node.addData(hashData,data);
            this.leafRoot = node;
            this.root = node;
            node.parent = null;
            str.append(hashData);
            this.rootHash = SHA.HASHData(str.toString());
        }else{//树根不为空时
            TreeNode tag = root;
            while(tag.child != null){
                tag = tag.child[tag.dataNumber - 1];
            }//tag追踪到最右边的叶节点

            if(!tag.isFull()){//叶节点不满时
                if(tag instanceof LeafNode){
                    ((LeafNode) tag).addData(hashData,data);//叶节点插入数据
                }else{
                    System.out.println("this leafNode is not right type");
                }
            }else{//叶节点已满
                LeafNode newLeafTag = new LeafNode();//追踪新节点
                newLeafTag.addData(hashData,data);
                str.append(hashData);

               ((LeafNode)tag).nextLeaf = newLeafTag;//连接叶节点
                tag = tag.parent;
                TreeNode newMidTag = newLeafTag;
                boolean first = true;//标注第一次时，孩子节点是leafNode
                //添加新接节点，直到其父节点能存储该节点为止
                while(tag != null && tag.isFull()){
                    TreeNode newMidNode = new TreeNode();
                    newMidNode.addData(SHA.HASHData(str.toString()));
                    str = new StringBuilder(newMidNode.hashData[0].toString()); //更新str
                    if(first){
                        newMidNode.child = new LeafNode[M];
                        newMidNode.child[0] = (LeafNode)newMidTag;
                        first = false;
                    }else {
                        newMidNode.child = new TreeNode[M];
                        newMidNode.child[0] = newMidTag;
                    }
                    newMidTag.parent = newMidNode;
                    newMidTag = newMidNode;
                    tag = tag.parent;
                }
                if(tag == null){//树根已满需要增加树高
                    TreeNode newRoot = new TreeNode();//增加树的高度，新增加根节点
                    newRoot.child = new TreeNode[M];
                    newRoot.child[0] = this.root;
                    this.root.parent = newRoot;
                    newRoot.child[1] = newMidTag;
                    newMidTag.parent = newRoot;
                    newRoot.addData(rootHash);
                    newRoot.addData(SHA.HASHData(str.toString()));
                    str = new StringBuilder("" + newRoot.hashData[0] + newRoot.hashData[1]);
                    this.root = newRoot;
                    this.rootHash = SHA.HASHData(str.toString());
                }else{//有容身的父节点
                    newMidTag.parent = tag;
                    tag.addData(SHA.HASHData(str.toString()));
                    tag.child[tag.dataNumber - 1] = newMidTag;
                }
            }

            //更新该节点到根节点的哈希
            while(tag != null){
                str = new StringBuilder();
                for(int i = 0;i < tag.dataNumber;i++){
                    str.append(tag.hashData[i]);
                }
                if(tag.parent == null){
                    this.rootHash = SHA.HASHData(str.toString());//tag为根节点时,更新这棵树的rootHash
                }else{
                    tag.parent.hashData[tag.parent.dataNumber - 1] = SHA.HASHData(str.toString());//tag为非根节点，改变其父节点最右边的hashData
                }
                tag = tag.parent;
            }
        }
    }

    //生成树的updateVO
    public List<List<BigInteger>> updateVO(long data){
        if(this.root == null){
            return null;//如果这棵树还没有数据，则返回空
        }
        List<List<BigInteger>> VO = new ArrayList<>();
        TreeNode tag = this.root;
        while(tag.child != null){
            List<BigInteger> brotherHash = new ArrayList<>();
            for(int i = 0;i < tag.dataNumber - 1;i++){
                brotherHash.add(tag.hashData[i]);
            }
            VO.add(brotherHash);
            tag = tag.child[tag.dataNumber - 1];
        }
        //叶节点所有内部数据哈希都加入结果
        List<BigInteger> leafHash = new ArrayList<>();
        leafHash.addAll(Arrays.asList(tag.hashData).subList(0, tag.dataNumber));
        VO.add(leafHash);
        //将新加入的节点哈希加入结果
        List<BigInteger> hashData = new ArrayList<>();
        hashData.add(SHA.HASHData(String.valueOf(data)));
        VO.add(hashData);
        return VO;
    }




    //查找两个叶节点之间的需要加入VO的中间节点哈希
    //不包括p，tag1也不包括q，tag2
    //注意：如果q == null 时，表示结尾为空；如果tag2 = M时，表示已经到末尾了
    public List<VOData> findHash(LeafNode p, LeafNode q, int tag1, int tag2){
        List<VOData> result = new ArrayList<VOData>();
        if(q == null){
            return result;
        }
        //将起点向后推移一个数据
        if(p == null){
            p = this.leafRoot;
            tag1 = 0;
        }else{
            if(p.equals(q) && (tag1 == tag2 || (tag1 == M - 1 && tag2 == M))){//当起点和终点相同时返回空结果
                return result;
            }
            if(tag1 == M - 1){
                p = p.nextLeaf;
                tag1 = 0;
            }else{
                tag1 = tag1 + 1;
            }
        }
        //开始找p和q之间的VO
        if(p.equals(q)){//如果此时p已经等于q
            int noticeTag;//注意此时如果tag2为M时，q节点只有q.dataNumber数据
            if(tag2 == M){
                noticeTag = q.dataNumber;
            }else{
                noticeTag = tag2;
            }
            while(tag1 < noticeTag){
                result.add(new VOData(p.hashData[tag1++],0));
            }
            return result;
        }else{//如果q与p之间隔了节点
            if(tag1 != 0){//将p节点里面的数据都加入结果，从而从节点开始寻找数据
                while(tag1 < M){
                    result.add(new VOData(p.hashData[tag1++],0));
                }
                p = p.nextLeaf;
            }
            //从p节点开始寻找
            while(p != null && (!p.equals(q) || (p.equals(q) && tag2 == M))){//找p到q的VO，当r不能向上或者p遇到q时回退后重新进入循环
                TreeNode r = p;
                LeafNode t = p;
                BigInteger loadHash = r.parent.hashData[this.childNumber(r)];
                int level = 1;
                LeafNode load = t.nextLeaf;
                int N = M;
                int n = 0;
                while(true) {
                    while (t != null && (n < N && (!Objects.equals(t, q)) || (t.equals(q) && tag2 == M))) {
                        t = t.nextLeaf;
                        n++;
                        if(t == null){
                            break;
                        }
                    }
                    if (n == N) {
                        if (this.childNumber(r) == 0) {//r可以继续向上
                            N = N * M;
                            level++;
                            r = r.parent;
                            loadHash = r.parent.hashData[this.childNumber(r)];
                            load = t;
                        } else {//此时r不能向上，则更新result,重新开始下一轮
                            result.add(new VOData(loadHash, level));
                            p = load;
                            break;
                        }
                    } else if(tag2 == M){
                        result.add(new VOData(loadHash, level));
                        p = load;
                        break;
                    }else {//p遇到q时回退，并更新result
                        result.add(new VOData(loadHash, level));
                        p = load;
                        break;
                    }
                }
            }
            //将最后一个节点的哈希加入结果
            if(tag2 != M){
                for(int i = 0;i < tag2;i++){
                    result.add(new VOData(p.hashData[i],0));
                }
            }
        }
        return result;
    }
    //查找当前节点是它父节点的哪一个孩子，用于findHash
    private int childNumber(TreeNode node){
        TreeNode parent = node.parent;
        if(parent == null){
            return -1;//当前节点为根节点
        }
        for(int i = 0;i < M;i++){
            if(parent.child[i].equals(node)){
                return i;
            }
        }
        return -2;
    }
}
