package com.ming.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * @author clownMing
 * 分组ID随机生成器
 */
public final class RandomGenerator {
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成 6 位随机分组ID
     * @return
     */
    public static String generateRandom() {
        return generateRandom(6);
    }

    /**
     * 生成随机分组ID
     * @param length ID长度
     * @return 随机分组ID
     */
    public static String generateRandom(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
