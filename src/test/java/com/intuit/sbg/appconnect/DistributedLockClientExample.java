package com.intuit.sbg.appconnect;

/**
 * Created by vkumar21 on 3/15/18.
 */
public class DistributedLockClientExample {

    public static void main(String[] args) {
        Thread reader1 = new Thread(new ReadLock("reader1"));
        Thread reader2 = new Thread(new ReadLock("reader2"));

        Thread writer1 = new Thread(new WriteLock("writer1"));
        Thread writer2 = new Thread(new WriteLock("writer2"));

        reader1.start();
        reader2.start();
        writer1.start();
        writer2.start();
    }


public static class ReadLock implements Runnable {
    private String name;

    public ReadLock(String name) {
        this.name = name;
    }

    @Override

    public void run() {
        try {
            DistributedLock distributedLock = new DistributedLock("reader");
            boolean isLockAcquired = distributedLock.lockForRead();
            if (isLockAcquired) {
                System.out.println("Read Lock acquired for: " + name);
                Thread.sleep(2000L);
                distributedLock.release(true);
                System.out.println("Read Lock release for:" + name);
            }
        } catch (Exception e) {

        }
    }
}

    public static class WriteLock implements Runnable {
        private String name;
        public WriteLock(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                DistributedLock distributedLock = new DistributedLock("writer");
                boolean isLockAcquired = distributedLock.lockForWrite(10);
                if (isLockAcquired) {
                    System.out.println("Write Lock acquired for: " + name);
                    Thread.sleep(2000L);
                    distributedLock.release(true);
                    System.out.println("Write Lock release for " + name);
                } else {
                    System.out.println("Write Lock not acquired for: " + name);
                }
            } catch (Exception e) {

            }
        }
    }
}