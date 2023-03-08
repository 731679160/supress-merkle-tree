package dataowner;

import java.math.BigInteger;

import static utils.Parameter.M;


public class TreeNode {
    public TreeNode parent = null;
    public TreeNode[] child = null;
    public BigInteger[] hashData = new BigInteger[M];
    public int dataNumber = 0;
    public void addData(BigInteger hashData){
        if(dataNumber == M){
            System.out.println("this node is full");
        }
        this.hashData[dataNumber] = hashData;
        dataNumber++;
    }
    public boolean isFull(){
        if(dataNumber == M){
            return true;
        }else{
            return false;
        }
    }
}
