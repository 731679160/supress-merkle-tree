package server;

import java.math.BigInteger;

public class VOData {
    public int level;
    public BigInteger data;
    public VOData(BigInteger data, int level){
        this.data = data;
        this.level = level;
    }

    @Override
    public String toString() {
        return "{" + data.toString() + '}';
    }
}
