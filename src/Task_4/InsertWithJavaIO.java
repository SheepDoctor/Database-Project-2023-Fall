package Task_4;

import java.io.*;

public class InsertWithJavaIO {
    public static void main(String[] args) {
        // 输入CSV文件路径
        String sourceCSVFile = "source_file/users_left.csv";

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 10; i++) {
            try {
                //每次都输出一个单独的目录，以排除上一次的影响
                String destinationCSVFile = "source_file/user_deleted(" + i + ").csv";

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 记录结束时间
        long endTime = System.currentTimeMillis();

        // 计算用时
        long executionTime = endTime - startTime;
        System.out.println("CSV file copy completed in " + executionTime + " milliseconds.");
        System.out.println("The speed is " + 1000 * 10 * 1000L / executionTime + " records per second. ");
    }
}
