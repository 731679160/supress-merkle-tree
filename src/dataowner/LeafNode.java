package dataowner;

import java.math.BigInteger;

import static utils.Parameter.M;

public class LeafNode extends TreeNode {
    public long[] data = new long[M];
    public LeafNode nextLeaf = null;
    public void addData(BigInteger hashData,long data){
        if(dataNumber == M){
            System.out.println("this node is full");
        }
        this.hashData[dataNumber] = hashData;
        this.data[dataNumber] = data;
        dataNumber++;
    }

}
