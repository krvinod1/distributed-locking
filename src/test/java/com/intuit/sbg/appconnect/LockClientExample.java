package com.intuit.sbg.appconnect;

/**
 * Created by vkumar21 on 3/15/18.
 */
public class LockClientExample {

    public static void main(String[] args) {
        Thread reader1 = new Thread(new ReadLock("reader1"));
        Thread reader2 = new Thread(new ReadLock("reader2"));

        Thread writer1 = new Thread(new WriteLock("Writer1"));
        Thread writer2 = new Thread(new WriteLock("writer2"));

        reader1.start();
        reader2.start();
        writer1.start();
        writer2.start();
    }


public static class ReadLock implements Runnable {
    int counter = 0;
    private String name;

    public ReadLock(String name) {
        this.name = name;
    }

    @Override

    public void run() {
        try {
            LockService readLockService = new LockService("reader");
            boolean isLockAcquired = readLockService.lockForRead();
            if (isLockAcquired) {
                System.out.println("Read Lock acquired for: " + name);
                while (counter < 5) {
                    System.out.println("name  " + name + ": "+ counter++ );
                    Thread.sleep(100L);
                }
                readLockService.release(true);
                System.out.println("Read Lock release for:" + name);
            }
        } catch (Exception e) {

        }
    }
}

    public static class WriteLock implements Runnable {
        int counter = 200;
        private String name;

        public WriteLock(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                LockService readLockService = new LockService("writer");
                boolean isLockAcquired = readLockService.lockForWrite(10);
                if (isLockAcquired) {
                    System.out.println("Write Lock acquired for: " + name);
                    while (counter < 205) {
                        System.out.println("name  " + name + ": "+ counter++ );
                        Thread.sleep(100L);
                    }
                    readLockService.release(true);
                    System.out.println("Write Lock release for " + name);
                } else {
                    System.out.println("Write Lock not acquired for: " + name);
                }
            } catch (Exception e) {

            }
        }
    }
}