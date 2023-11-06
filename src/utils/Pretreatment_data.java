package utils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pretreatment_data
{
    public static void main(String[] args)
    {
        String inputFilePath = "source_file/danmu.csv";  // 输入文件路径
        String outputFilePath = "source_file/danmu_p.csv";  // 输出文件路径
        int num = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath)))
        {
            String line;
            line = reader.readLine();
            while ((line = reader.readLine()) != null)
            {
                line  = line.replace("\\", "/_reversed");
                String pattern = "BV[A-Za-z0-9]+,[0-9]+,[0-9]+\\.[0-9]+,";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(line);
                String modifiedLine = line;
                if (m.find())
                {
                    modifiedLine = modifyLine(m.group(0)) + line.substring(m.group(0).length()) + '\n';  // 修改行内容的方法
                }
                writer.write(modifiedLine);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static String modifyLine(String line)
    {
        String str = line.replaceAll(",", "\7");
        return str;
    }
}
