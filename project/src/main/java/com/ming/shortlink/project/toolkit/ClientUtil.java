package com.ming.shortlink.project.toolkit;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ip 工具类
 */
public class ClientUtil {
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";

    /**
     * 获取用户ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP.equalsIgnoreCase(ip) || LOCALHOST_IPV6.equalsIgnoreCase(ip)) {
                InetAddress iNet = null;
                try {
                    iNet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (iNet != null) {
                    ip = iNet.getHostAddress();
                }
            }
        }

        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }

        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : ip;
    }

    public static String getClientOS(HttpServletRequest request) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        return userAgent.getOperatingSystem().getName();
    }


    public static String getBrowser(HttpServletRequest request) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        return userAgent.getBrowser().toString();
    }

    public static String getDeviceType(HttpServletRequest request) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        DeviceType deviceType = userAgent.getOperatingSystem().getDeviceType();
        return deviceType.toString();
    }
}
