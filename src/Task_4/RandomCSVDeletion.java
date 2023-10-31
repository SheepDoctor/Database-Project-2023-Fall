package Task_4;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RandomCSVDeletion {
    public static void main(String[] args) {
        int recordsToDelete = 10000;
        // 原始 CSV 文件路径
        String inputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_to_be_tested.csv";
        String outputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_test_10000.csv";
        // 存储删除的记录的新 CSV 文件路径

        // 选择要删除的记录数


        try {
            // 读取原始 CSV 文件
            CSVReader reader = new CSVReader(new FileReader(inputCsvFile));
            List<String[]> csvData = reader.readAll();
            reader.close();

            // 获取原始记录总数
            int totalRecords = csvData.size();

            // 随机选择要删除的记录的索引，要求其不重复
            Random random = new Random();
            List<Integer> recordsToDeleteIndices = new ArrayList<>();
            while (recordsToDeleteIndices.size() < recordsToDelete) {
                int randomIndex = random.nextInt(totalRecords);
                if (!recordsToDeleteIndices.contains(randomIndex)) {
                    recordsToDeleteIndices.add(randomIndex);
                }
            }

            // 创建新 CSV 文件
            String outputDeletedCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\user_27881.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(outputDeletedCsvFile)), writer1 = new CSVWriter((new FileWriter(outputCsvFile)));

            // 写入未删除的记录到新文件
            for (int i = 0; i < totalRecords; i++) {
                if (!recordsToDeleteIndices.contains(i)) {
                    writer.writeNext(csvData.get(i));
                } else {
                    writer1.writeNext(csvData.get(i));
                }
            }
            writer.close();
            writer1.close();
            System.out.println(recordsToDelete + " records deleted from " + outputDeletedCsvFile + " and saved to " + outputCsvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
