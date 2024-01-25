package com.ming.shortlink.admin.test;

/**
 * @author clownMing
 */
public class UserTableShardingTest {
    public static final String SQL = "CREATE TABLE `t_link_goto_%d` (\n" +
            "  `id` bigint NOT NULL COMMENT 'ID',\n" +
            "  `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',\n" +
            "  `full_short_url` varchar(255) DEFAULT NULL COMMENT '完整短链接',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

    public static void main(String[] args) {
        for(int i = 0; i < 16; i ++) {
            System.out.println(String.format(SQL, i));
        }
    }
}
