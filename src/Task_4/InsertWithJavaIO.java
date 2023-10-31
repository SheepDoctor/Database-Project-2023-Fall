package Task_4;

import java.io.*;

public class InsertWithJavaIO {
    public static void main(String[] args) {
        // 输入CSV文件路径
        String sourceCSVFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_test_10000.csv";
        // 输出CSV文件路径
        String destinationCSVFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_1000000.csv";

        for (int i = 0; i < 100; i++) {
            try {
                // 记录开始时间
                long startTime = System.currentTimeMillis();

                // 创建输入流和输出流
                BufferedReader reader = new BufferedReader(new FileReader(sourceCSVFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(destinationCSVFile, true));

                String line;
                while ((line = reader.readLine()) != null) {
                    // 逐行读取源CSV文件并写入目标CSV文件
                    writer.write(line);
                    writer.newLine(); // 写入换行符
                }

                // 关闭输入流和输出流
                reader.close();
                writer.close();

                // 记录结束时间
                long endTime = System.currentTimeMillis();

                // 计算用时
                long executionTime = endTime - startTime;
                System.out.println("CSV file copy completed in " + executionTime + " milliseconds.");
                System.out.println("The speed is " + 10000 * 1000L / executionTime + " records per second. ");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
