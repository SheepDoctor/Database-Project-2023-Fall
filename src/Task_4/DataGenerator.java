package Task_4;


import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DataGenerator {

    private static final int NUM_ROWS = 1000000;
    private static final String[] GENDERS = {"男", "女", "保密"};
    private static final String[] IDENTITIES = {"user", "superuser"};
    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHINESE_CHARACTERS = "我是一个测试字符串中文English123456";

    public static void main(String[] args) throws IOException {
        Set<Long> mids = generateUniqueRandomNumbers(NUM_ROWS, (long) Math.pow(2, 32));
        Long startTime = System.currentTimeMillis();
        try (CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\hlh\\Desktop\\user_generated.csv"))) {
            // 写入标题行
            writer.writeNext(new String[]{"mid", "name", "gender", "birthday", "level", "profile", "identity"});
            // 生成并写入数据行
            for (long mid : mids) {
                writer.writeNext(generateRow(mid));
            }
            Long endTime = System.currentTimeMillis();
            System.out.printf("共生成%d条数据，用时%f3秒。", NUM_ROWS, (endTime - startTime) / 1000f);
        }
    }

    private static Set<Long> generateUniqueRandomNumbers(int num, long range) {
        Set<Long> numbers = new HashSet<>();
        while (numbers.size() < num) {
            numbers.add((long) (random.nextDouble() * range));
        }
        return numbers;
    }

    private static String[] generateRow(long mid) {
        String name = generateRandomMixedText(random.nextInt(16));
        String gender = GENDERS[random.nextInt(GENDERS.length)];
        String birthday = random.nextInt(100) < 20 ? generateRandomBirthday() : "";
        int level = 1 + random.nextInt(6);
        String profile = random.nextInt(100) < 15 ? generateRandomMixedText(random.nextInt(255)) :"";
        String identity = random.nextInt(100) < 10 ? IDENTITIES[1] : IDENTITIES[0];
        return new String[]{String.valueOf(mid), name, gender, birthday, String.valueOf(level), profile, identity};
    }

    private static String generateRandomMixedText(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            } else {
                builder.append(CHINESE_CHARACTERS.charAt(random.nextInt(CHINESE_CHARACTERS.length())));
            }
        }
        return builder.toString();
    }

    private static String generateRandomBirthday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1920 + random.nextInt(104));
        calendar.set(Calendar.DAY_OF_YEAR, 1 + random.nextInt(calendar.getActualMaximum(Calendar.DAY_OF_YEAR)));
        return dateFormat.format(calendar.getTime());
    }
}
