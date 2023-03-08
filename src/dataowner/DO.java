package dataowner;

import server.RoundData;
import server.VOData;
import sun.reflect.generics.tree.Tree;
import utils.SHA;
import utils.WriteVO;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import static utils.Parameter.M;

public class DO {
    private HashMap<Long, MerkleInvertedBTree> treesMap = new HashMap<>();
    public DO(String path) {
        HashMap<Long, List<Long>> inverseData = readGeneratedData(path);
        long startTime = System.currentTimeMillis();
        buildTree(inverseData);
        long endTime = System.currentTimeMillis();
        System.out.println("索引建立时间：" + (endTime - startTime) + "ms");
        getIndexSize();
    }

    private String treeToStr(TreeNode node) {
        if (node == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        for (BigInteger bigInteger : node.hashData) {
            stringBuffer.append(bigInteger + " ");
        }
        if (node.child != null) {
            for (TreeNode treeNode : node.child) {
                stringBuffer.append(treeToStr(treeNode) + ",");
            }
        }

        stringBuffer.append(">");
        return stringBuffer.toString();
    }


    public void getIndexSize() {
        StringBuffer index = new StringBuffer();
        for (Map.Entry<Long, MerkleInvertedBTree> entry : treesMap.entrySet()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(entry.getKey());
            stringBuffer.append(" ");
            MerkleInvertedBTree value = entry.getValue();
            stringBuffer.append(value.rootHash + " ");
            stringBuffer.append(treeToStr(value.root));
            stringBuffer.append("\n");
            index.append(stringBuffer.toString());
        }
        long size = WriteVO.writeVOToLocal(index.toString());
        System.out.println("索引大小：" + size / 1024.0 / 1024.0 + "MB");
    }

    public HashMap<Long, MerkleInvertedBTree> getTreesMap() {
        return treesMap;
    }

    private HashMap<Long, List<Long>> readGeneratedData(String path){
        HashMap<Long, List<Long>> dataSet = new HashMap<>();
        File file = new File(path);
        try {
            if(file.isFile()&&file.exists()){
                InputStreamReader fla = new InputStreamReader(new FileInputStream(file));
                BufferedReader scr = new BufferedReader(fla);
                String str = null;
                while((str = scr.readLine()) != null){
                    String[] data = str.split(" ");
                    List<Long> row = new ArrayList<Long>();
                    for(int i = 1;i < data.length;i++){
                        row.add((Long.valueOf(data[i])));
                    }
                    dataSet.put(Long.parseLong(data[0]), row);
                }
                scr.close();
                fla.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dataSet;
    }

    public void buildTree(HashMap<Long, List<Long>> inverseData){

//导入数据

        HashMap<Long, MerkleInvertedBTree> invertedTree = new HashMap<>();

        for (Map.Entry<Long, List<Long>> entry : inverseData.entrySet()) {
            Long keyword = entry.getKey();
            MerkleInvertedBTree tree = new MerkleInvertedBTree(keyword, entry.getValue());
            treesMap.put(keyword, tree);
        }
    }

    //服务器端传入的是多轮数据，智能合约传的是各组关键字与其索引树的根哈希的map
    //对于服务器端的数据，首先验证各轮数据是否正确，并得到各个关键字树的验证信息。然后对得到的验证信息重新组成一颗Merkle树，并计算树根
    //并传入查询的关键字，注意关键字顺序不能错
    public boolean verify(List<RoundData> VOServiceProvider, Map<Long, BigInteger> VOSmartContract, long[] keywords){
        //验证各论数据，并进行组合成Merkle树的验证信息
        Map<Long, List<VOData>> MerkleVO = new HashMap<>();//用来存储关键字与相应Merkle树验证信息的对于关系
        //初始化各关键字的MerkleVO
        for (long keyword : keywords) {
            MerkleVO.put(keyword, new ArrayList<>());
        }

        //依次验证各轮信息，并更新MerkleVO
        int length = keywords.length;
        int tag = 1;//用来跟踪nextRound关键字
        //将第一轮的target加入结果，因为后面每轮中不需要将target加入验证信息
        List<VOData> nextRoundVO = MerkleVO.get(keywords[tag % length]);//下轮关键字所对应的验证信息
        MerkleVO.get(keywords[0]).add(new VOData(SHA.HASHData(String.valueOf(VOServiceProvider.get(0).target)),0));
        for (RoundData thisRoundData : VOServiceProvider) {
            //将每轮的每组匹配信息加入到VO;
            long maxRightBoundary = 0;
            int relatedTag = -1;
            for (int i = 0; i < thisRoundData.VO.size(); i++) {
                List<VOData> thisGroupVO = thisRoundData.VO.get(i);
                //将该组所有的VOData都加入验证信息
                for (VOData voData : thisGroupVO) {
                    if (voData != null) {
                        nextRoundVO.add(voData);
                    }
                }
                //最后剩余的轮次里boundary的大小为0
                if (thisRoundData.boundary.size() > i) {
                    //验证target在boundary范围内
                    if (thisRoundData.target < thisRoundData.boundary.get(i)[0] || (thisRoundData.target >= thisRoundData.boundary.get(i)[1] && thisRoundData.boundary.get(i)[1] != -1)) {
                        return false;
                    }
                    //将boundary加入验证信息
                    //左边界加入要求:1、左边界不为-1。2、左边界不与其上一个验证信息相同
                    if (thisRoundData.boundary.get(i)[0] != -1) {
                        BigInteger dataHash = SHA.HASHData(String.valueOf(thisRoundData.boundary.get(i)[0]));
                        if (nextRoundVO.size() == 0 || !Objects.equals(dataHash, nextRoundVO.get(nextRoundVO.size() - 1).data)) {
                            nextRoundVO.add(new VOData(dataHash, 0));
                        }
                        //当有边界时，更新relatedTag
                        if ((maxRightBoundary <= thisRoundData.boundary.get(i)[1] && maxRightBoundary != -1) || thisRoundData.boundary.get(i)[1] == -1) {
                            maxRightBoundary = thisRoundData.boundary.get(i)[1];
                            relatedTag = (tag + 1) % length;
                        }
                    }
                    //右边界加入要求:右边界不为-1。
                    if (thisRoundData.boundary.get(i)[1] != -1) {
                        nextRoundVO.add(new VOData(SHA.HASHData(String.valueOf(thisRoundData.boundary.get(i)[1])), 0));
                    }
                }
                tag = (tag + 1) % length;
                nextRoundVO = MerkleVO.get(keywords[tag]);
            }
            if (relatedTag != -1) {
                tag = relatedTag;
                nextRoundVO = MerkleVO.get(keywords[relatedTag]);
            }
        }

        //依次验证每个关键字的是否正确
        for (long theKeyword : keywords) {
            BigInteger rootSM = VOSmartContract.get(theKeyword);
            //用MerkleVO重新构造MerkleVO树，计算根哈希
            BigInteger rootSP = computeRootHash(MerkleVO.get(theKeyword));
            if (!(Objects.equals(rootSP, rootSM))) {
                return false;
            }
        }
        return true;
    }

    private BigInteger computeRootHash(List<VOData> MerkleVO){
        while(MerkleVO.size() > 1){
            mergeVO(MerkleVO,0);
        }
        return MerkleVO.get(0).data;
    }

    private void mergeVO(List<VOData> MerkleVO, int label){
        StringBuilder data = new StringBuilder(String.valueOf(MerkleVO.get(label).data));//用来合并这个等级的数据
        int level = MerkleVO.get(label).level;//当前数据的等级
        //将接下来的最多M - 1个数据都进行合并
        for(int i = 0;i < M - 1;i++){
            //当剩下的数据不够合并或者接下来的数据等级比自身高，提前结束循环
            if((label + 1) >= MerkleVO.size() || MerkleVO.get(label + 1).level > level){
                break;
            }
            while(MerkleVO.get(label + 1).level < level){
                mergeVO(MerkleVO,label + 1);
            }
            data.append(MerkleVO.get(label + 1).data);
            MerkleVO.remove(label + 1);
        }
        MerkleVO.set(label,new VOData(SHA.HASHData(String.valueOf(data)),level + 1));
    }




}
