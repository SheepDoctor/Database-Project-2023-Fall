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
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHINESE_CHARACTERS = "我是一个测试字符串中文";

    private static final String[] BILIBILI_NAME = {"小明", "Vtuber233", "干杯", "咸鱼日常", "ACGN控", "宅男女神", "风花雪月", "喵星人", "程序猿", "甜品制造机", "樱花落尽"
            , "异世界旅人", "守望先锋", "音乐达人", "二次元", "火锅粉", "鱼塘主人", "星空下的诗人", "摄影师小白", "漫画迷", "奈何桥旁", "未来科学家", "呆萌少女", "梦游者", "电子竞技"
            , "轻小说", "独角兽", "星际穿越", "食堂大妈", "猫耳朵", "代码机器", "魔法少女", "穿越者", "日系少女", "文艺青年", "火影忍者", "宅舞大佬", "黑白键的诗", "美食猎人", "古风"
            , "卡布奇诺", "手游高手", "巧克力控", "漫展狂魂", "二货", "霓虹灯下", "蓝色忧郁", "热爱生活", "清新文艺", "剑三玩家", "古剑奇谭", "王者荣耀", "潮流前线", "舞蹈达人", "网络红人"
            , "汉服爱好者", "自由撰稿人", "虚拟歌姬", "双子星", "创意工坊", "摇滚青年", "游戏宅", "奶茶控", "宠物达人", "旅行者", "书法家", "复古风", "爱生活", "动漫制作", "网络安全"
            , "萌宠家族", "甜甜圈", "吉他手", "极客", "天文爱好者", "穿搭达人", "烘焙师", "唱见", "夜的钢琴曲", "悬疑探险", "滑板少年", "架子鼓手", "电影迷", "健身达人", "平行世界", "炫舞者"
            , "摄影爱好者", "设计师小兔", "轻小说作者", "旅行摄影", "萌新", "文艺复兴", "死宅", "魔法师", "桌游玩家", "虚拟偶像", "二次元少女", "独立音乐人", "模型制作师", "星座研究员",
            "占星术师", "二次元世界", "漫画家", "数码达人", "科技迷", "电子音乐", "绘画家", "书虫", "模拟城市", "梦想家", "拼图达人", "舞台剧迷"};


    public static void main(String[] args) throws IOException {
        Long startTime = System.currentTimeMillis();
        Set<Long> mids = generateUniqueRandomNumbers(NUM_ROWS, (long) Math.pow(2, 32));
        Set<String> names = new HashSet<>();
        try (CSVWriter writer = new CSVWriter(new FileWriter("source_file/user_generated.csv"))) {
            // 写入标题行
            writer.writeNext(new String[]{"mid", "name", "sex", "birthday", "level", "sign", "identity"});
            // 生成并写入数据行
            for (long mid : mids) {
                String[] row;
                do {
                    row = generateRow(mid);
                } while (!names.add(row[1])); //确保名字是唯一的
                writer.writeNext(row, false);
            }
            Long endTime = System.currentTimeMillis();
            System.out.printf("共生成%d条数据，用时%f秒。", NUM_ROWS, (endTime - startTime) / 1000f);
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
        String name = generateRandomName(random.nextInt(16));
        String gender = GENDERS[random.nextInt(GENDERS.length)];
        String birthday = random.nextInt(100) < 20 ? generateRandomBirthday() : "";
        int level = random.nextInt(7);
        String sign = random.nextInt(100) < 15 ? generateRandomMixedText(random.nextInt(255)) : "";
        String identity = random.nextInt(100) < 10 ? IDENTITIES[1] : IDENTITIES[0];
        return new String[]{String.valueOf(mid), name, gender, birthday, String.valueOf(level), sign, identity};
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

    private static String generateRandomName(int length) {
        StringBuilder builder = new StringBuilder();
        if (random.nextBoolean()) builder.append(BILIBILI_NAME[random.nextInt(BILIBILI_NAME.length)]);
        while (builder.toString().length() <= length) {
            builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
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
