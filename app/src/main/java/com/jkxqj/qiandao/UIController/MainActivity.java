package com.jkxqj.qiandao.UIController;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jkxqj.qiandao.R;
import com.jkxqj.qiandao.model.QianDao;
import com.jkxqj.qiandao.model.QianTui;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends Activity {

    private TextView hello;
    public static String IP;
    public static String MAC;
    public static String MAC_WIFI;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hello = (TextView) findViewById(R.id.hello);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String realName = bundle.getString("realName");

        hello.setText("你好，" + realName);
    }

    //检查连接的是什么网络
    public  Integer checkWifi(Context context) {
    ConnectivityManager ConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo =  ConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return 1;  //返回1，连接的是移动网络
                } else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return 2;  //返回2，连接的是wifi
                }
            } else {
                return 3; //返回3，没有连接。
            }
        return 3;
    }
//获取本地IP地址
    public String getLocalIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {

            ex.printStackTrace();
        }


        return "";
        
    }
//获取本手机MAC
    public String getLocalMacAddress() {

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue; //只有真机有wlan0地址，模拟器为空

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";

    }





//获取连接的WIFI的MAC地址
    public static String getConnectedWifiMacAddress(Context context) {
        String connectedWifiMacAddress = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> wifiList;

        if (wifiManager != null) {
            wifiList = wifiManager.getScanResults();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (wifiList != null && info != null) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult result = wifiList.get(i);
                    if (info.getBSSID().equals(result.BSSID)) {
                        connectedWifiMacAddress = result.BSSID;
                    }
                }
            }
        }
        return connectedWifiMacAddress;
    }


    public void Dao(View view) {
        if (checkWifi(MainActivity.this) == 1) {
            Toast.makeText(MainActivity.this, "您连接的是移动网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 3) {
            Toast.makeText(MainActivity.this, "您没有连接网络，签到失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 2) {
            MAC=getLocalMacAddress();
            IP=getLocalIpAddress();
            MAC_WIFI = getConnectedWifiMacAddress(MainActivity.this);
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            final String stime=sdf.format(date);
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String realName = bundle.getString("realName");
            String account = bundle.getString("account");

            QianDao qiandao=new QianDao();
            qiandao.setAccount(account);
            qiandao.setRealName(realName);
            qiandao.setDaoTime(stime);
            qiandao.setIP(IP);
            qiandao.setMAC(MAC);
            qiandao.setMAC_WIFI(MAC_WIFI);
            qiandao.save(MainActivity.this, new SaveListener(){
                @Override
                public void onSuccess() {
                Toast.makeText(MainActivity.this, "签到成功！\n IP:"+IP+"\n本机MAC 地址:"+MAC+"\n时间："+ stime, Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(int code, String arg0) {
                Toast.makeText(MainActivity.this, "签到失败!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void Tui(View view) {
        if (checkWifi(MainActivity.this) == 1) {
            Toast.makeText(MainActivity.this, "您连接的是移动网络，签退失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 3) {
            Toast.makeText(MainActivity.this, "您没有连接网络，签退失败！", Toast.LENGTH_LONG).show();
        } else if (checkWifi(MainActivity.this) == 2) {
            MAC=getLocalMacAddress();
            IP=getLocalIpAddress();
            MAC_WIFI = getConnectedWifiMacAddress(MainActivity.this);

            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            final String ttime=sdf.format(date);
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String realName = bundle.getString("realName");
            String account = bundle.getString("account");

            QianTui qiantui=new QianTui();
            qiantui.setAccount(account);
            qiantui.setRealName(realName);
            qiantui.setTuiTime(ttime);
            qiantui.setIP(IP);
            qiantui.setMAC(MAC);
            qiantui.setMAC_WIFI(MAC_WIFI);
            qiantui.save(MainActivity.this, new SaveListener(){
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "签退成功！\n IP:"+IP+"\n本机MAC 地址:"+MAC+"\n时间："+ttime, Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(int code, String arg0) {
                    Toast.makeText(MainActivity.this, "签退失败!", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public void Info_Dao(View view) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String account = bundle.getString("account");
        BmobQuery<QianDao> query=new BmobQuery<>();
        query.addWhereEqualTo("account",account);
        query.findObjects(MainActivity.this,new FindListener<QianDao>() {
            @Override
            public void onSuccess(List<QianDao> qianDaos) {

                String str="";
                for(QianDao a:qianDaos){
                     str+="时间:"+a.getDaoTime()+"\nMAC:"+a.getMAC()+"\nIP:"+a.getIP()+"\nMAC_WIFI:"+a.getMAC_WIFI()+"\n\n";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("签到详情");
                builder.setMessage(str);
                builder.create().show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(MainActivity.this,"查询失败！"+s,Toast.LENGTH_LONG);
            }
        });
    }

    public void Info_Tui(View view) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String account = bundle.getString("account");
        BmobQuery<QianTui> query=new BmobQuery<>();
        query.addWhereEqualTo("account",account);
        query.findObjects(MainActivity.this,new FindListener<QianTui>() {
            @Override
            public void onSuccess(List<QianTui> QianTuis) {
                String str="";
                for(QianTui a:QianTuis){
                     str+="时间:"+a.getTuiTime()+"\nMAC:"+a.getMAC()+"\nIP:"+a.getIP()+"\nMAC_WIFI:"+a.getMAC_WIFI()+"\n\n";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("签退详情");
                builder.setMessage(str);
                builder.create().show();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(MainActivity.this,"查询失败！"+s,Toast.LENGTH_LONG);
            }
        });
    }

    public void Quit(View view) {

        this.finish();

    }

}
