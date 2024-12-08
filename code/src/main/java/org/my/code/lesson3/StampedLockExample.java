package org.my.code.lesson3;

import java.util.ArrayList;
import java.util.concurrent.locks.StampedLock;

public class StampedLockExample {


    private final ArrayList<Integer> dataList = new ArrayList<>();
    private final StampedLock lock = new StampedLock();



    public int[] readDataOptimistically() {


        long stamp = lock.tryOptimisticRead();
        int[] snapshot = new int[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            snapshot[i] = dataList.get(i);
        }
        if (!lock.validate(stamp)) {
            // 验证乐观读期间是否有写操作，如果有，需要重新获取读锁进行读取
            stamp = lock.readLock();
            try {
                for (int i = 0; i < dataList.size(); i++) {
                    snapshot[i] = dataList.get(i);
                }
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return snapshot;
    }

}
