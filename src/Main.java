import dataowner.DO;
import dataowner.MerkleInvertedBTree;
import server.QueryRes;
import server.SP;
import utils.WriteVO;
import utils.queryAndVerifyRes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

import static utils.WriteVO.voToStr;
import static utils.WriteVO.writeVOToLocal;

public class Main {
    public static HashMap readForwardData(String path) throws Exception{
        HashMap<Integer, List<Integer>> forwardIndexMap = new HashMap<>();
        File file = new File(path);
        if(file.isFile()&&file.exists()){
            InputStreamReader fla = new InputStreamReader(new FileInputStream(file));
            BufferedReader scr = new BufferedReader(fla);
            String str = null;
            while((str = scr.readLine()) != null){
                String[] data = str.split(" ");
                List<Integer> keywords = new ArrayList<Integer>();
                for(int i = 1;i < data.length;i++){//第三个才是文档id
                    keywords.add(Integer.valueOf(data[i]));
                }
                keywords.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                });
                forwardIndexMap.put(Integer.valueOf(data[0]), keywords);
            }
            scr.close();
            fla.close();
        }
        return forwardIndexMap;
    }
    static HashSet<Integer> queryKeywords = new HashSet<>();
    public static long[] getQuery (HashMap<Integer, List<Integer>> allForwardIndex, boolean hasRes, int keywordSize) {
        long[] res = new long[keywordSize];
        int idSize = allForwardIndex.size();
        int id = (int)(Math.random() * idSize);
        List<Integer> keywordList = allForwardIndex.get(id);
        if (hasRes) {
            while(keywordList.size() < keywordSize) {
                id = (int)(Math.random() * idSize);
                keywordList = allForwardIndex.get(id);
            }
            HashSet<Integer> tempSet = new HashSet<>();
            for (int i = 0; i < keywordSize; i++) {
                int randIndex = (int) (Math.random() * keywordList.size());
                while (tempSet.contains(randIndex)) {
                    randIndex = (int) (Math.random() * keywordList.size());
                }
                tempSet.add(randIndex);
                res[i] = keywordList.get(randIndex);
                queryKeywords.add(keywordList.get(randIndex));
            }
        } else {
            for (int i = 0; i < keywordSize; i++) {
                int rand;
                do {
                    rand = (int)(Math.random() * 2000) + 1;
                } while (queryKeywords.contains(rand));
                queryKeywords.add(rand);
                res[i] = rand;
            }
        }
        return res;
    }



    public static void printTime(int round, int keywordSize, HashMap<Integer, List<Integer>> allForwardIndex, DO dataOwner, SP serviceProvider) {
        queryAndVerifyRes[] query = new queryAndVerifyRes[round];
        for (int i = 0; i < query.length; i += 2) {
            query[i] = queryAndVerifyTime(getQuery(allForwardIndex, true, keywordSize), dataOwner, serviceProvider);
            while (!query[i].isPass) {
                query[i] = queryAndVerifyTime(getQuery(allForwardIndex, true, keywordSize), dataOwner, serviceProvider);
            }
            query[i + 1] = queryAndVerifyTime(getQuery(allForwardIndex, false, keywordSize), dataOwner, serviceProvider);
            while (!query[i + 1].isPass) {
                query[i + 1] = queryAndVerifyTime(getQuery(allForwardIndex, false, keywordSize), dataOwner, serviceProvider);
            }
        }
        long sumQuery = 0;
        long sumVerify = 0;
        long sumVOSize = 0;
        double l = query.length;
        for (int i = 0; i < l; i++) {
            sumQuery += query[i].queryTime;
            sumVerify += query[i].verifyTime;
            sumVOSize += query[i].VOSize;
        }
        System.out.println("查询关键字数量" + keywordSize + "---" + "查询时间：" + (sumQuery / l) / 1000000 + "ms" + "," + "验证时间：" + (sumVerify / l) / 1000000 + "ms" + "," + "VO大小：" + (sumVOSize / l) / 1024 + "kb");
    }
    public static void main(String[] args) throws Exception {
        String path = "D:\\mycode\\mywork\\Dataset_processing\\test_dataset\\invertedIndex100000id2000keyword.txt";
//        String path = "D:\\mycode\\mywork\\Dataset_processing\\test_dataset\\invertedIndex100000id2000keyword_Uniform_new_k2000.txt";
        DO dataOwner = new DO(path);
        dataOwner = new DO(path);
//        SP serviceProvider = new SP(dataOwner.getTreesMap());
//        HashMap<Integer, List<Integer>> allForwardIndex = readForwardData(forwardPath);
//        queryAndVerifyRes query;
//        long[] keywords = new long[]{2688, 911};
//        query = queryAndVerifyTime(keywords, dataOwner, serviceProvider);
//        System.out.println("test-------------------------------------");
//        printTime(10, 2, allForwardIndex, dataOwner, serviceProvider);
//        System.out.println("real-------------------------------------");
//        printTime(10, 2, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 3, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 4, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 5, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 6, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 7, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 8, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 9, allForwardIndex, dataOwner, serviceProvider);
//        printTime(10, 10, allForwardIndex, dataOwner, serviceProvider);


//        queryAndVerifyRes[] query2 = new queryAndVerifyRes[4];
//        long[] keywords21 = new long[]{2688, 911};
//        query2[0] = queryAndVerifyTime(keywords21, dataOwner, serviceProvider);
//        long[] keywords22 = new long[]{1049, 4152};
//        query2[1] = queryAndVerifyTime(keywords22, dataOwner, serviceProvider);
//        long[] keywords23 = new long[]{91, 92};
//        query2[2] = queryAndVerifyTime(keywords23, dataOwner, serviceProvider);
//        long[] keywords24 = new long[]{75, 76};
//        query2[3] = queryAndVerifyTime(keywords24, dataOwner, serviceProvider);
//        System.out.print("查询2关键字:");
//        printTime(query2);
//
//        queryAndVerifyRes[] query3 = new queryAndVerifyRes[4];
//        long[] keywords31 = new long[]{2298, 3241, 1988};
//        query3[0] = queryAndVerifyTime(keywords31, dataOwner, serviceProvider);
//        long[] keywords32 = new long[]{4326, 3002, 3214};
//        query3[1] = queryAndVerifyTime(keywords32, dataOwner, serviceProvider);
//        long[] keywords33 = new long[]{482, 44, 443};
//        query3[2] = queryAndVerifyTime(keywords33, dataOwner, serviceProvider);
//        long[] keywords34 = new long[]{705, 706, 1002};
//        query3[3] = queryAndVerifyTime(keywords34, dataOwner, serviceProvider);
//        System.out.print("查询3关键字:");
//        printTime(query3);
//
//        queryAndVerifyRes[] query4 = new queryAndVerifyRes[4];
//        long[] keywords41 = new long[]{1115, 775, 711, 2662};
//        query4[0] = queryAndVerifyTime(keywords41, dataOwner, serviceProvider);
//        long[] keywords42 = new long[]{2115, 4339, 400, 4538};
//        query4[1] = queryAndVerifyTime(keywords42, dataOwner, serviceProvider);
//        long[] keywords43 = new long[]{263, 44, 20, 11};
//        query4[2] = queryAndVerifyTime(keywords43, dataOwner, serviceProvider);
//        long[] keywords44 = new long[]{262, 96, 207, 12};
//        query4[3] = queryAndVerifyTime(keywords44, dataOwner, serviceProvider);
//        System.out.print("查询4关键字:");
//        printTime(query4);
//
//        queryAndVerifyRes[] query5 = new queryAndVerifyRes[4];
//        long[] keywords51 = new long[]{4450, 375, 3947, 1801, 395};
//        query5[0] = queryAndVerifyTime(keywords51, dataOwner, serviceProvider);
//        long[] keywords52 = new long[]{4942, 2102, 3441, 2270, 423};
//        query5[1] = queryAndVerifyTime(keywords52, dataOwner, serviceProvider);
//        long[] keywords53 = new long[]{263, 3044, 2030, 1401, 120};
//        query5[2] = queryAndVerifyTime(keywords53, dataOwner, serviceProvider);
//        long[] keywords54 = new long[]{262, 946, 2307, 1042,103};
//        query5[3] = queryAndVerifyTime(keywords54, dataOwner, serviceProvider);
//        System.out.print("查询5关键字:");
//        printTime(query5);
//
//        queryAndVerifyRes[] query6 = new queryAndVerifyRes[4];
//        long[] keywords61 = new long[]{4581, 308, 3459, 2267, 4676, 4839};
//        query6[0] = queryAndVerifyTime(keywords61, dataOwner, serviceProvider);
//        long[] keywords62 = new long[]{791, 3637, 989, 1883, 994, 2174};
//        query6[1] = queryAndVerifyTime(keywords62, dataOwner, serviceProvider);
//        long[] keywords63 = new long[]{1700, 1001, 1102, 1106, 1039, 1600};
//        query6[2] = queryAndVerifyTime(keywords63, dataOwner, serviceProvider);
//        long[] keywords64 = new long[]{4000, 1106, 200, 1100, 4700, 2102};//
//        query6[3] = queryAndVerifyTime(keywords64, dataOwner, serviceProvider);
//        System.out.print("查询6关键字:");
//        printTime(query6);
//
//        queryAndVerifyRes[] query7 = new queryAndVerifyRes[4];
//        long[] keywords71 = new long[]{4439, 2492, 1075, 4509, 3188, 838, 939};
//        query7[0] = queryAndVerifyTime(keywords71, dataOwner, serviceProvider);
//        long[] keywords72 = new long[]{145, 1634, 3420, 2670, 104, 2646, 4489};
//        query7[1] = queryAndVerifyTime(keywords72, dataOwner, serviceProvider);
//        long[] keywords73 = new long[]{279, 4872, 4971, 747, 3700, 2726, 3079};//
//        query7[2] = queryAndVerifyTime(keywords73, dataOwner, serviceProvider);
//        long[] keywords74 = new long[]{4000, 1016, 2020, 1010, 4700, 2012, 153};
//        query7[3] = queryAndVerifyTime(keywords74, dataOwner, serviceProvider);
//        System.out.print("查询7关键字:");
//        printTime(query7);
//
//        queryAndVerifyRes[] query8 = new queryAndVerifyRes[4];
//        long[] keywords81 = new long[]{933, 3101, 880, 4394, 2081, 2696, 3933, 919};
//        query8[0] = queryAndVerifyTime(keywords81, dataOwner, serviceProvider);
//        long[] keywords82 = new long[]{4737, 727, 1334, 3663, 1242, 1724, 4126, 1159};
//        query8[1] = queryAndVerifyTime(keywords82, dataOwner, serviceProvider);
//        long[] keywords83 = new long[]{1003, 1018, 2076, 1300, 1270, 700, 302, 606};
//        query8[2] = queryAndVerifyTime(keywords83, dataOwner, serviceProvider);
//        long[] keywords84 = new long[]{4190, 1016, 2172, 1700, 805, 4230, 4020, 807};
//        query8[3] = queryAndVerifyTime(keywords84, dataOwner, serviceProvider);
//        System.out.print("查询8关键字:");
//        printTime(query8);
//
//        queryAndVerifyRes[] query9 = new queryAndVerifyRes[4];
//        long[] keywords91 = new long[]{1352, 3623, 4779, 4982, 4648, 3365, 634, 2998, 2414};
//        query9[0] = queryAndVerifyTime(keywords91, dataOwner, serviceProvider);
//        long[] keywords92 = new long[]{2324, 856, 1459, 3245, 4095, 4446, 692, 2216, 738};
//        query9[1] = queryAndVerifyTime(keywords92, dataOwner, serviceProvider);
//        long[] keywords93 = new long[]{123, 1128, 572, 1320, 1272, 722, 322, 3226, 122};
//        query9[2] = queryAndVerifyTime(keywords93, dataOwner, serviceProvider);
//        long[] keywords94 = new long[]{410, 1256, 2272, 2200, 1925, 4227, 4222, 827, 2222};
//        query9[3] = queryAndVerifyTime(keywords94, dataOwner, serviceProvider);
//        System.out.print("查询9关键字:");
//        printTime(query9);
//
//        queryAndVerifyRes[] query0 = new queryAndVerifyRes[4];
//        long[] keywords01 = new long[]{2190, 4175, 4530, 2432, 4659, 1938, 2855, 1956, 1364, 2318};
//        query0[0] = queryAndVerifyTime(keywords01, dataOwner, serviceProvider);
//        long[] keywords02 = new long[]{2900, 3508, 2055, 3641, 2355, 2937, 3532, 2164, 823, 3375};
//        query0[1] = queryAndVerifyTime(keywords02, dataOwner, serviceProvider);
//        long[] keywords03 = new long[]{1222, 138, 2330, 1340, 3110, 3398, 3302, 3857, 3340, 3334};
//        query0[2] = queryAndVerifyTime(keywords03, dataOwner, serviceProvider);
//        long[] keywords04 = new long[]{2401, 1148, 3540, 1420, 3410, 4398, 4212, 847, 3440, 4934};
//        query0[3] = queryAndVerifyTime(keywords04, dataOwner, serviceProvider);
//        System.out.print("查询10关键字:");
//        printTime(query0);
    }

    public static queryAndVerifyRes queryAndVerifyTime(long[] queryRequest, DO dataOwner, SP server) {
        //查询
        long startTime = System.nanoTime();
        QueryRes queryRes = server.query(queryRequest);
        long endTime = System.nanoTime();

        //获取查询关键字树的根哈希
        Map<Long, BigInteger> VOSmartContract = new HashMap<>();
        for(int i = 0;i < queryRequest.length;i++) {
            VOSmartContract.put(queryRequest[i],dataOwner.getTreesMap().get(queryRequest[i]).rootHash);
        }
        //验证
        long startTime1 = System.nanoTime();
        boolean res = dataOwner.verify(queryRes.getRounds(), VOSmartContract, queryRequest);
//        System.out.println(res);
        long endTime1 = System.nanoTime();
        long size = writeVOToLocal(voToStr(queryRes));

        queryAndVerifyRes queryAndVerifyRes = new queryAndVerifyRes((endTime - startTime), (endTime1 - startTime1), size, res);
//        System.out.println(queryAndVerifyRes.toString());
//        System.out.println();
        return queryAndVerifyRes;
    }
}
