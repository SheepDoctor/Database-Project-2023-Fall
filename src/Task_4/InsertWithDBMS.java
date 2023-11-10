package Task_4;

import utils.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class InsertWithDBMS {
    public static void main(String[] args) {
        for (int i = 0; i < 15; i++) {
            Properties prop = new Properties();
            prop.put("host", "localhost");
            prop.put("user", "postgres");
            prop.put("password", "123456");
            prop.put("database", "Project");

            String file_path = "D:\\文件\\学习\\大二上\\数据库原理\\小组\\data\\user_generated_2.csv";
            String[] queue = {"Long", "String", "String", "Date", "Int", "String", "String"};
            String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
            Database database = new Database(prop);

            Loader loader = new Loader();
            System.out.println("USER导入......");
            loader.write_data(file_path, queue, database, sql, false, 1000000.0);
        }
    }
}

class Loader {

    private final int BATCH_SIZE = 500;//initial 500
    private Connection con = null;
    private PreparedStatement stmt = null;


    private void loadData(ArrayList<Object> row, String[] type) throws SQLException {
        int index = 1;
        for (int i = 0; i < row.size(); i++) {
            try {
                String data;

                if (row.get(i) != null) {
                    data = row.get(i).toString();
                } else {
                    data = null;
                }
                switch (type[i].charAt(0)) {
                    case 'L' -> {
                        if (row.get(i) == null) {
                            stmt.setLong(index++, -1);
                            continue;
                        }
                        assert data != null;
                        stmt.setLong(index++, Long.parseLong(data));
                    }
                    case 'S' -> {
                        if (row.get(i) == null) {
                            stmt.setString(index++, null);
                            continue;
                        }
                        stmt.setString(index++, data);
                    }
                    case 'D' -> {
                        if (row.get(i) == null) {
                            stmt.setDate(index++, null);
                            continue;
                        }
                        assert data != null;
                        stmt.setDate(index++, new Date(Integer.parseInt(data.split("-")[0]) - 1900,
                                Integer.parseInt(data.split("-")[1]),
                                Integer.parseInt(data.split("-")[2])));
                    }
                    case 'I' -> {
                        if (row.get(i) == null) {
                            stmt.setInt(index++, -1);
                            continue;
                        }
                        assert data != null;
                        stmt.setInt(index++, Integer.parseInt(data));
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        stmt.addBatch();
    }


    public void write_data(String file_path, String[] queue, Database database, String sql, Boolean adder, double num) {
        con = database.open();
        int cnt = 0;

        try {
            stmt = con.prepareStatement(sql);
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            database.close(stmt);
            System.exit(1);
        }

        try {
            long start = System.currentTimeMillis(); // 开始时间
            FileReader fileReader = new FileReader(file_path);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;

            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] lineData = line.split(",");
                ArrayList<Object> row = new ArrayList<>();

                for (String data : lineData) {
                    if (data.equals("")) {
                        data = null;
                    }
                    row.add(data);
                }

                if (adder) {
                    row.add(cnt);
                }

                loadData(row, queue);

                // 执行批处理

                cnt++;

                if (cnt % BATCH_SIZE == 0) {
                    if (num == 0) {
                        System.out.println("当前进度：" + cnt + " 条");
                    } else {
                        System.out.printf("导入进度：%.3f%%\n", cnt / num * 100);
                    }
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }

            stmt.executeBatch();
            stmt.clearBatch();

            try {
                con.commit(); // 提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
                long end = System.currentTimeMillis(); // 结束时间
                System.out.println(cnt + " records successfully loaded");
                System.out.println("TIME : " + (end - start) / 1000f + "s");
                System.out.println("Loading speed : " + cnt * 1000f / (end - start) + " records/s");
            } catch (Exception e) {
                System.err.println("Fatal error: " + e.getMessage());
                try {
                    con.rollback();
                    stmt.close();
                } catch (Exception e2) {
                    System.out.println(e2);
                }
                database.close(stmt);
                System.exit(1);
            }
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
            try {
                con.rollback();
                stmt.close();
            } catch (Exception ignored) {
            }
            database.close(stmt);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        database.close(stmt);
    }
}