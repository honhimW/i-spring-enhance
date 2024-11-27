package io.github.honhimw.util;

import java.net.*;
import java.util.Enumeration;

/**
 * Get local IP
 * @author hon_him
 * @since 2022-05-31
 */
public class IpUtils {

    private static String IPV4;
    private static String IPV6;

    private IpUtils() {
    }

    public static String localIPv4() {
        if (IPV4 == null) {
            IPV4 = getIP(Inet4Address.class);
        }
        return IPV4;
    }

    public static String localIPv6() {
        if (IPV6 == null) {
            IPV6 = getIP(Inet6Address.class);
        }
        return IPV6;
    }

    private static String getIP(Class<? extends InetAddress> type) {
        String localip = null;
        String netip = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress inetAddress = null;
            out: while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    inetAddress = address.nextElement();
                    if (!inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()
                        && type.isAssignableFrom(inetAddress.getClass())) {
                        netip = inetAddress.getHostAddress();
                        break out;
                    } else if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()
                        && type.isAssignableFrom(inetAddress.getClass())) {
                        localip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        if (netip != null && !netip.isEmpty()) {
            return netip;
        } else {
            return localip;
        }
    }

    /**
     * Get first local IP
     */
    public static String firstLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }


}
