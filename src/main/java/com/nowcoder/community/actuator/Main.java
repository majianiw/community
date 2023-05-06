package com.nowcoder.community.actuator;

import java.util.Date;

import static java.lang.Thread.sleep;

/**
 * @Date: create in 15:18 2023/4/19
 * @describe:
 */
public class Main {
    static int a = 10000;
    public static void main(String[] args) throws InterruptedException {
        up();
        down();
    }
    public  static void up() throws InterruptedException {
        sleep(5000);
        down();
        System.out.println("我不好");
    }

    public synchronized static void down(){
        System.out.println("你好");
    }
}
