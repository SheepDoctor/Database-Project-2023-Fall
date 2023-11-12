package Task_4;

import java.io.*;
import java.util.*;

public class UpdateWithJavaIO {
    public static void main(String[] args) {
        String sourceCSVFile = "source_file/users_generated.csv";
        String destinationCSVFile = "source_file/users_updated.csv";
        for (int j = 0; j < 15; j++) {
            long startTime = System.nanoTime();
            List<Integer> linesWithEmptySign = new ArrayList<>();
            int linesToUpdate = 1000;

            // 第一遍扫描，找出所有简介为空的行号
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceCSVFile))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length > 5 && "\"\"".equals(columns[5])) {
                        linesWithEmptySign.add(lineNumber);
                        //System.out.print("#");
                    }
                    lineNumber++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 随机选择行号
            Collections.shuffle(linesWithEmptySign);
            Set<Integer> selectedLineNumbers = new HashSet<>();
            for (int i = 0; i < Math.min(linesToUpdate, linesWithEmptySign.size()); i++) {
                selectedLineNumbers.add(linesWithEmptySign.get(i));
            }

            // 第二遍扫描，更新选中的行
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceCSVFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(destinationCSVFile, false))) {

                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    if (selectedLineNumbers.contains(lineNumber)) {
                        String[] columns = line.split(",");
                        columns[5] = "该用户太懒了，没有留下任何介绍哦。"; // 更新简介列
                        line = String.join(",", columns);
                    }
                    writer.write(line);
                    writer.newLine();
                    lineNumber++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1.0e9;
            //System.out.printf("CSV file update completed in %.3f seconds. ", executionTime);
            //System.out.printf("Updated %d records. Speed:%.0f records per second.", selectedLineNumbers.size(), selectedLineNumbers.size() / executionTime);
            System.out.printf("%.3f\n", selectedLineNumbers.size() / executionTime); //为减小求平均数时的误差，保留3位小数
        }
    }
}
