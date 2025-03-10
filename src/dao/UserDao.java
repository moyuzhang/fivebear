import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDao {
    public void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS User (" +
                "Id INT AUTO_INCREMENT PRIMARY KEY, " +
                "用户名 VARCHAR(255) NOT NULL, " +
                "密码 VARCHAR(255) NOT NULL, " +
                "创建时间 TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "用户类型 VARCHAR(50), " +
                "父类id INT, " +
                "FOREIGN KEY (父类id) REFERENCES User(Id)" +
                ")";

        try (Connection connection = DatabaseUtil.getConnection(); // 确保这里引用的是 DatabaseUtil
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("User 表创建成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}