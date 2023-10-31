package Task_4;

import java.io.*;
import java.util.*;

public class DeleteWithJavaIO {
    public static void main(String[] args) {
        String inputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_1000000.csv"; // 原始 CSV 文件
        String outputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\output.csv"; // 新的 CSV 文件
        int linesToDelete = 1000; // 要删除的行数
        for (int i = 0; i < 100; i++) {
            List<String> linesToKeep = new ArrayList<>();
            try {
                long startTime = System.currentTimeMillis();

                // 0 .随机选择要删除的行
                Random random = new Random();
                Set<Integer> recordsToDeleteIndices = new HashSet<>();
                while (recordsToDeleteIndices.size() < linesToDelete) {
                    int randomIndex = random.nextInt(1000000);
                    recordsToDeleteIndices.add(randomIndex);
                }
                long startTime1 = System.currentTimeMillis();

                // 1. 从原始 CSV 文件读取数据，同时跳过要删除的行
                BufferedReader reader = new BufferedReader(new FileReader(inputCsvFile));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (!recordsToDeleteIndices.contains(count++)) {
                        linesToKeep.add(line);
                    }
                }
                reader.close();
                long startTime2 = System.currentTimeMillis();

                // 2. 打开新的 CSV 文件
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvFile));
                long startTime3 = System.currentTimeMillis();

                // 3. 将保留的数据写入新文件
                for (String lineToKeep : linesToKeep) {
                    writer.write(lineToKeep);
                    writer.newLine(); // 添加换行符
                }
                writer.close();
                long endTime = System.currentTimeMillis();
                System.out.println(linesToDelete + " lines removed and saved to " + outputCsvFile);
                System.out.println("Speed : " + (linesToDelete * 1000L) / (endTime - startTime) + " records/s");
                System.out.printf("%d %d %d %d \n", startTime1 - startTime, startTime2 - startTime, startTime3 - startTime, endTime - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
