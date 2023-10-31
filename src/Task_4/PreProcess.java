package Task_4;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PreProcess {//预处理文件，将所有回车其转义为\\n
    public static void main(String[] args) {
        String inputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_to_be_tested.csv";
        String outputCsvFile = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\users_1000001.csv";
        try {
            CSVReader reader = new CSVReader((new FileReader(inputCsvFile)));
            List<String[]> lines = reader.readAll();
            reader.close();
            CSVWriter writer = new CSVWriter(new FileWriter(outputCsvFile));
            for (String[] strings : lines) {
                for (int j = 0; j < strings.length; j++) {
                    strings[j] = strings[j].replace("\n", "\\n");
                }
                writer.writeNext(strings);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
