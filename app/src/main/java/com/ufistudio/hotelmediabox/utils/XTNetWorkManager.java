package com.ufistudio.hotelmediabox.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.provider.Settings;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * @author cengt 2016/11/14 12:18
 * @version v1.0
 */

public class XTNetWorkManager {
    private static final String TAG = XTNetWorkManager.class.getSimpleName();

    private static final String ETHERNET_SERVICE = "ethernet";

    private static final String ETHERNET_OPERSTATE_PATH = "/sys/class/net/eth0/operstate";

    public static XTNetWorkManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /** 网络是否正常(包括所有网络) */
    public boolean isNetWorkOn(Context context) {
        if (context != null) {
            ConnectivityManager cm = getConnectivityManager(context);
            NetworkInfo activedNetworkInfo = cm.getActiveNetworkInfo();
            return (activedNetworkInfo != null && activedNetworkInfo.isConnected());
        }
        return false;
    }

    /** 网线是否插入 */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean isEthernetPlugin() {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(ETHERNET_OPERSTATE_PATH);
            byte[] buff = new byte[fin.available()];
            fin.read(buff);
            String str = new String(buff, StandardCharsets.UTF_8);
            return "UP".equalsIgnoreCase(str.trim());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean isEthernetConnected(Context context) {
        Network network = getEthernetNetwork(context);
        if (network != null) {
            NetworkInfo info = getConnectivityManager(context).getNetworkInfo(network);
            return info != null && info.isConnected();
        }
        return false;
    }

    /** 网线是否使用DHCP方式获取IP */
    public boolean isEthernetUseDHCP(Context context) {
        if (context == null) {
            Log.e(TAG,"invaild argument");
            return false;
        }

        EthernetManager em = getEthernetManager(context);
        return em != null && em.getConfiguration().getIpAssignment() == IpConfiguration.IpAssignment.DHCP;
    }

    /** 设置网线使用DHCP方式获取IP */
    public void enableEthernetDHCP(Context context) {
        Log.d(TAG,"enableEthernetDHCP");

        if (context == null) {
            Log.e(TAG,"invaild argument");
            return;
        }

        EthernetManager em = getEthernetManager(context);
        IpConfiguration configuration = em.getConfiguration();
        configuration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
        em.setConfiguration(configuration);
    }

    /** 设置网线使用静态方式获取IP */
    public void enableEthernetStaticIP(Context context, XTHost host) {
        Log.d(TAG,"enableEthernetStaticIP: host = " + host+", IP = "+host.getIp()+", Gateway = "+host.getGateway()+", Netmask = "+host.getNetmask());

        if (context == null || host == null) {
            Log.e(TAG,"invaild argument");
            return;
        }

        EthernetManager em = getEthernetManager(context);
        IpConfiguration configuration = em.getConfiguration();
        configuration.setIpAssignment(IpConfiguration.IpAssignment.STATIC);

        StaticIpConfiguration test = configuration.getStaticIpConfiguration();
        if(test != null)
            Log.e(TAG,"test:"+test.ipAddress+", "+test.domains+", "+test.gateway+", "+test.dnsServers);

//        StaticIpConfiguration staticIpConfiguration = configuration.getStaticIpConfiguration();
        StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
        staticIpConfiguration.gateway = NetworkUtils.numericToInetAddress(host.getGateway());
        InetAddress ipAddress = NetworkUtils.numericToInetAddress(host.getIp());
        int prefixLength = netmask2PrefixLength(host.getNetmask());
        staticIpConfiguration.ipAddress = createLinkAddress(ipAddress, prefixLength);
        // 使用网关作为DNS地址
        staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(host.getGateway()));

        configuration.setStaticIpConfiguration(staticIpConfiguration);
        em.setConfiguration(configuration);


    }

    /** 获取网线连接信息 */
    public XTHost getEthernetInfo(Context context) {
        ConnectivityManager cm = getConnectivityManager(context);
        Network ethernetNetwork = getEthernetNetwork(context);
        if (ethernetNetwork != null) {
            LinkProperties activedLinkProperties = cm.getLinkProperties(ethernetNetwork);
            if (activedLinkProperties != null) {
                XTHost host = new XTHost();

                // 查找IP和子网掩码
                List<LinkAddress> addresses = activedLinkProperties.getLinkAddresses();
                if (addresses.size() > 0) {
                    LinkAddress addr = addresses.get(0);
                    host.setIp(addr.getAddress().getHostAddress());
                    host.setNetmask(prefixLength2Netmask(addr.getPrefixLength()));
                } else {
                    Log.e(TAG,"linkaddresses size <= 0");
                }

                // 查找默认的网关
                boolean foundDefaultRoute = false;
                List<RouteInfo> routes = activedLinkProperties.getRoutes();
                for (RouteInfo r : routes) {
                    if (r.isDefaultRoute()) {
                        host.setGateway(r.getGateway().getHostAddress());
                        foundDefaultRoute = true;
                        break;
                    }
                }
                // 没找到默认的网关则使用第一个
                if (!foundDefaultRoute && routes.size() > 0) {
                    host.setGateway(routes.get(0).getGateway().getHostAddress());
                }

                return host;
            }
        } else {
            Log.e(TAG,"error get ethernet network");
        }
        return null;
    }

    public XTNetWorkManager() {}

    @SuppressWarnings("WrongConstant")
    private EthernetManager getEthernetManager(Context context) {
        return (EthernetManager) context.getSystemService(ETHERNET_SERVICE);
    }

    private ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    }

    private Network getEthernetNetwork(Context context) {
        ConnectivityManager cm = getConnectivityManager(context);
        for (Network network : cm.getAllNetworks()) {
            NetworkInfo info = cm.getNetworkInfo(network);
            if (info != null && info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return network;
            }
        }
        return null;
    }

    /** 掩码长度转为掩码字符串 */
    private String prefixLength2Netmask(int prefixLength) {
        int value = 0xFFFFFFFF << (32 - prefixLength);
        int a = 0xFF & (value >> 24);
        int b = 0xFF & (value >> 16);
        int c = 0xFF & (value >> 8);
        int d = 0xFF & value;
        return String.format(Locale.getDefault(), "%d.%d.%d.%d", a, b, c, d);
    }

    /** 掩码字符串转为掩码长度 */
    private int netmask2PrefixLength(String netmask) {
        int sum = 0;
        String[] values = netmask.split("\\.");
        for (String s : values) {
            sum += NetworkUtils.netmaskIntToPrefixLength(Integer.parseInt(s));
        }
        return sum;
    }

    private LinkAddress createLinkAddress(InetAddress ipAddress, int prefixLength) {
        try {
            Class<?> clazz = Class.forName("android.net.LinkAddress");
            Class<?>[] parTypes = new Class<?>[2];
            parTypes[0] = InetAddress.class;
            parTypes[1] = int.class;
            Constructor<?> constructor = clazz.getConstructor(parTypes);

            Object[] pars = new Object[2];
            pars[0] = ipAddress;
            pars[1] = prefixLength;
            return (LinkAddress) constructor.newInstance(pars);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class SingletonHolder {
        private static final XTNetWorkManager INSTANCE = new XTNetWorkManager();
    }

    public class XTHost {
        public XTHost(){

        }

        public XTHost(String ip,String gateway,String netmask){
            setIp(ip);
            setGateway(gateway);
            setNetmask(netmask);
        }

        /** IP */
        private String ip;

        /** 网关 */
        private String gateway;

        /** 子网掩码 */
        private String netmask;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public String getNetmask() {
            return netmask;
        }

        public void setNetmask(String netmask) {
            this.netmask = netmask;
        }
    }
}

