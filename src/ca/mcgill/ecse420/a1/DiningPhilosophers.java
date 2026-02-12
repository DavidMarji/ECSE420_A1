package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

        public static void main(String[] args) {

                int numberOfPhilosophers = 5;
                Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
                Object[] chopsticks = new Object[numberOfPhilosophers];

                // Toggle which part to run:
                // true => 3.1 (deadlock can happen)
                // false => 3.2 (no deadlock + no starvation)
                final boolean RUN_DEADLOCK_VERSION = true;

                // init shared chopsticks (shared monitor objects)
                for (int i = 0; i < numberOfPhilosophers; i++) {
                        chopsticks[i] = new Object();
                }

                // init the waiter used for 3.2 (doesn't affect 3.1)
                Waiter.init(numberOfPhilosophers);

                ExecutorService exec = Executors.newFixedThreadPool(numberOfPhilosophers);

                for (int i = 0; i < numberOfPhilosophers; i++) {
                        int left = i;
                        int right = (i + 1) % numberOfPhilosophers;
                        philosophers[i] = new Philosopher(i, left, right, chopsticks, RUN_DEADLOCK_VERSION);
                        exec.execute(philosophers[i]);
                }

                exec.shutdown();
        }

        // 3.2: Waiter that grants BOTH chopsticks at once (no deadlock) + FIFO
        // ticketing (no starvation)
        public static class Waiter {
                private static final ReentrantLock lock = new ReentrantLock(true); // fair lock
                private static java.util.concurrent.locks.Condition cond;
                private static boolean[] inUse;

                // FIFO ticketing to prevent starvation (bounded waiting)
                private static final java.util.concurrent.atomic.AtomicLong nextTicket = new java.util.concurrent.atomic.AtomicLong(
                                0);
                private static final java.util.concurrent.atomic.AtomicLong serving = new java.util.concurrent.atomic.AtomicLong(
                                0);

                public static void init(int n) {
                        lock.lock();
                        try {
                                cond = lock.newCondition();
                                inUse = new boolean[n];
                                for (int i = 0; i < n; i++)
                                        inUse[i] = false;
                                nextTicket.set(0);
                                serving.set(0);
                        } finally {
                                lock.unlock();
                        }
                }

                public static void pickUpBoth(int left, int right, int id) throws InterruptedException {
                        long my = nextTicket.getAndIncrement();

                        lock.lockInterruptibly();
                        try {
                                while (my != serving.get() || inUse[left] || inUse[right]) {
                                        cond.await();
                                }
                                inUse[left] = true;
                                inUse[right] = true;

                                // Move the FIFO window forward once this philosopher has been granted both
                                // sticks.
                                serving.incrementAndGet();
                                cond.signalAll();
                        } finally {
                                lock.unlock();
                        }
                }

                public static void putDownBoth(int left, int right) {
                        lock.lock();
                        try {
                                inUse[left] = false;
                                inUse[right] = false;
                                cond.signalAll();
                        } finally {
                                lock.unlock();
                        }
                }
        }

        public static class Philosopher implements Runnable {

                private final int id;
                private final int left;
                private final int right;
                private final Object[] chopsticks;
                private final boolean deadlockVersion;
                private int meals = 0;

                public Philosopher(int id, int left, int right, Object[] chopsticks, boolean deadlockVersion) {
                        this.id = id;
                        this.left = left;
                        this.right = right;
                        this.chopsticks = chopsticks;
                        this.deadlockVersion = deadlockVersion;
                }

                @Override
                public void run() {
                        try {
                                while (true) {
                                        think();

                                        if (deadlockVersion) {
                                                // 3.1: naive strategy (can deadlock): always left then right
                                                synchronized (chopsticks[left]) {
                                                        log("picked LEFT " + left);
                                                        // small delay increases the chance of deadlock showing up
                                                        sleepALittle(50, 120);

                                                        synchronized (chopsticks[right]) {
                                                                log("picked RIGHT " + right);
                                                                eat();
                                                                log("put down RIGHT " + right);
                                                        }
                                                        log("put down LEFT " + left);
                                                }
                                        } else {
                                                // 3.2: waiter strategy (no deadlock + FIFO no starvation)
                                                Waiter.pickUpBoth(left, right, id);
                                                log("picked BOTH (" + left + "," + right + ")");
                                                eat();
                                                Waiter.putDownBoth(left, right);
                                                log("put down BOTH (" + left + "," + right + ")");
                                        }
                                }
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                }

                private void think() throws InterruptedException {
                        log("thinking");
                        // kind of just chose this to make it more likely for them to deadlock
                        sleepALittle(1, 3);
                }

                private void eat() throws InterruptedException {
                        meals++;
                        log("eating (meal " + meals + ")");
                        sleepALittle(20, 40);
                }

                private void sleepALittle(int minMs, int maxMs) throws InterruptedException {
                        int d = minMs + (int) (Math.random() * (maxMs - minMs + 1));
                        Thread.sleep(d);
                }

                private void log(String msg) {
                        System.out.println("P" + id + ": " + msg);
                }
        }

}
