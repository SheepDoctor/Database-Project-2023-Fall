package Task_4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EasySelectWithJavaIO {
    public static void main(String[] args) {
        QWriter qWriter = new QWriter();
        String inputFile = "source_file/users_p.csv";
        int runTime = 100;
        double[] time = new double[runTime];
        for (int i = 0; i < runTime; i++) {
            try {
                long start = System.nanoTime();//开始时间
                //selectLevel1Count(qWriter, inputFile);
                //selectOrderedLevel2(qWriter, inputFile);
                select100MinMidComment(qWriter);
                long end = System.nanoTime();//结束时间
                time[i] = (end - start) / 1.0e9;
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        for (double t : time
        ) {
            qWriter.println(t);
        }
        qWriter.close();
    }

    // 查询所有等级为2的行，并且按mid的升序输出
    private static void selectOrderedLevel2(QWriter qWriter, String file_path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file_path));
        String line = reader.readLine();
        ArrayList<Long> midList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            ArrayList<Object> row = new ArrayList<>();
            if (lineData[4].equals("2")) {
                midList.add(Long.parseLong(lineData[0]));
            }
            midList.sort(null);

        }
        for (long mid : midList
        ) {
            qWriter.println(mid);
        }

    }

    // 查询所有level为1的列的数量
    private static void selectLevel1Count(QWriter qWriter, String file_path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file_path));
        String line = reader.readLine();
        int[] counts = new int[7];
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            counts[Integer.parseInt(lineData[4])]++;
        }
        for (int i = 1; i < 2; i++) {//int i = 0; i < counts.length; i++
            qWriter.println("level " + i + " : " + counts[i]);
        }
    }

    // 查询所有users表中mid最小的100个列在comment表格中发送的弹幕
    private static void select100MinMidComment(QWriter qWriter) throws IOException {
        String file_path = "source_file/users_p.csv", file_path1 = "source_file/danmu_p.csv";
        BufferedReader reader = new BufferedReader(new FileReader(file_path)), reader1 = new BufferedReader(new FileReader(file_path1));
        String line = reader.readLine();
        String line1 = reader1.readLine();
        ArrayList<Long> midList = new ArrayList<>();
        ArrayList<String> outputList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] lineData = line.split(",");
            midList.add(Long.parseLong(lineData[0]));
            //arrayList.add(lineData);
        }
        midList.sort(null);
        List<Long> first100Elements = midList.subList(0, 100);
        while ((line1 = reader1.readLine()) != null) {
            String[] lineData = line1.split("\7");
            if (first100Elements.contains(Long.parseLong(lineData[1]))) {
                outputList.add(lineData[1] + " " + lineData[3]);
            }
        }
        for (String output : outputList
        ) {
            qWriter.println(output);
        }
        //qWriter.println(outputList.size() + " results have been found.");
    }
}