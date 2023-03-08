package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoundData {
    public long target;
    public long keyword;
    public List<long[]> boundary = new ArrayList<>();
    public List<List<VOData>> VO = new ArrayList<>();

    @Override
    public String toString() {
        String boundStr = "";
        for (int i = 0; i < boundary.size(); i++) {
            boundStr += Arrays.toString(boundary.get(i));
        }
        return "{" + boundStr + VO.toString() + "}";
    }
}
