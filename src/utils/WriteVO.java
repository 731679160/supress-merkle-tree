package utils;

import server.QueryRes;
import server.RoundData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class WriteVO {
    public static long writeVOToLocal(String vo) {
        try {
            File writeName = new File("./src/vo.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(vo);
            }
            return (writeName.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String voToStr(QueryRes res) {
        StringBuffer str = new StringBuffer();
        List<Long> r = res.getResult();
        List<RoundData> rounds = res.getRounds();
        str.append(r.toString());
        str.append("\n");
        str.append(rounds.toString());
        return str.toString();
    }

}
