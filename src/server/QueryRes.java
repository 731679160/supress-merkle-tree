package server;

import dataowner.MerkleInvertedBTree;

import java.util.ArrayList;
import java.util.List;

public class QueryRes {
    private List<RoundData> rounds = new ArrayList<>();//查询时每轮的结果
    private List<Long> result = new ArrayList<>();//存储结果

    public List<RoundData> getRounds() {
        return rounds;
    }

    public List<Long> getResult() {
        return result;
    }

    public QueryRes(List<RoundData> rounds, List<Long> result) {
        this.rounds = rounds;
        this.result = result;
    }
}
