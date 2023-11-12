package Task_4;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PreTreatForUsers {
    //预处理文件，将所有回车转义为"\\n"
    public static void main(String[] args) {
        String inputFilePath = "source_file/users.csv";  // 输入文件路径
        String outputFilePath = "source_file/users_p.csv";  // 输出文件路径
        try {
            CSVReader reader = new CSVReader((new FileReader(inputFilePath)));
            List<String[]> lines = reader.readAll();
            reader.close();
            CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));
            for (String[] strings : lines) {
                for (int j = 0; j < strings.length; j++) {
                    strings[j] = strings[j].replace("\n", "\\n");
                    strings[j] = strings[j].replace(",", "，");
                }
                writer.writeNext(Arrays.copyOfRange(strings, 0, strings.length - 2), false);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



