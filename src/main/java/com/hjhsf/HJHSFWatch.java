package com.hjhsf;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class HJHSFWatch implements Watcher {


    @Autowired
    HJHSFConfigServer hjhsfConfigServer;

    // zookeeper 地址, 多个地址以逗号割开
    private static String zkServer = "127.0.0.1:2181";

    // 会话超时时间
    private static int sessionTimeout = 6000;

    private static String HJHSF_NODE = "/HJHSF_NODE";

    ZooKeeper zooKeeper;

    Map<String, Set<String>> metaInfo = new ConcurrentHashMap<>();

    public HJHSFWatch(){
        try {
            this.connect();
            this.createPersistentNode(HJHSF_NODE, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createService(String pathInfo) {
        try {
            this.createPersistentNode(HJHSF_NODE + "/" + pathInfo, false);
            this.watch(HJHSF_NODE + "/" + pathInfo);
            //todo 这里似乎获取的永远是127.0.0.1

            String value = InetAddress.getLocalHost().getHostAddress() + "|" + hjhsfConfigServer.getPort();
            this.createEphemeralNode(HJHSF_NODE + "/" + pathInfo + "/" + value, false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }


    public Set<String> getMetaInfo(String serviceName) {
        return this.metaInfo.get(serviceName);
    }


    public static void main(String[] args) throws Exception {
        System.out.println(InetAddress.getLocalHost().getHostAddress());

//        HJHSFWatch testWatch = new HJHSFWatch();
//
//
//        // 线程休眠, 否则不能监控到数据
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.next().equals("bye")){
//            break;
//        }

    }


    private void createPersistentNode(String path, boolean watcher) throws InterruptedException, KeeperException {
        Stat exists = this.zooKeeper.exists(path, watcher);
        if (exists == null) {
            try {
                this.zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (Exception e) {
                System.out.println("服务" + path + "已经创建");
            }
        }

        String serviceName = path.substring(path.lastIndexOf("/") + 1);
        if (!serviceName.equals(HJHSF_NODE.substring(1))) {
            metaInfo.computeIfAbsent(serviceName, k -> Collections.synchronizedSet(new HashSet<String>()));
        }
    }

    private void createEphemeralNode(String path, boolean watcher) throws InterruptedException, KeeperException {
        Stat exists = this.zooKeeper.exists(path, watcher);
        if (exists == null) {
            this.zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }
    }

    private void connect() throws Exception {
        this.zooKeeper = new ZooKeeper(zkServer, sessionTimeout, this);
    }

    private void disConnect() throws InterruptedException {
        this.zooKeeper.close();
    }

    private void watch(String path) throws InterruptedException, KeeperException {
        try {
            System.out.println("监视节点" + path);
            List<String> children = this.zooKeeper.getChildren(path, true);
            System.out.println("子节点有" + children.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            System.out.println("监视节点" + event.getPath());
            List<String> children = null;
            try {
                children = this.zooKeeper.getChildren(event.getPath(), true);
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("子节点有" + children.toString());
            System.out.println("注入服务名为" + getService(event.getPath()));
            Set<String> strings = metaInfo.get(getService(event.getPath()));
            strings.clear();
            for (String child : children) {
                System.out.println("ip port --->" + child);
                strings.add(child);
            }
        }else if (event.getType() == Event.EventType.None){
            System.out.println("消费者开始同步数据");
            try {
                List<String> children = this.zooKeeper.getChildren(HJHSF_NODE, false);
                for (String child : children) {
                    Set<String> strings = metaInfo.computeIfAbsent(child, k -> Collections.synchronizedSet(new HashSet<String>()));
                    List<String> ip_port = this.zooKeeper.getChildren(HJHSF_NODE + "/" + child, false);
                    for (String s : ip_port) {
                        strings.add(s);
                    }

                    //todo 消费者也要监听服务 跟新服务地址
                    System.out.println("服务" + child + "提供者地址为--->"  + ip_port);
                }
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static String getService(String path) {
        int i = path.lastIndexOf("/");
        path = path.substring(i + 1);
        return path;
    }

    private static String getLast(String path) {
        int i = path.lastIndexOf("/");
        return path.substring(i + 1);
    }

    private static boolean isNewService(String path) {
        return path.lastIndexOf("/") == 0;
    }

}
