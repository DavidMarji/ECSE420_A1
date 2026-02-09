package ca.mcgill.ecse420.a1;

public class DeadlockExperiment {

  private static final Object resourceA = new Object();
  private static final Object resourceB = new Object();

  public static void main(String[] args) {
    Thread t1 = new DeadlockTask(resourceA, resourceB, "Thread-A");
    Thread t2 = new DeadlockTask(resourceB, resourceA, "Thread-B");
    t1.start();
    t2.start();
  }

  private static class DeadlockTask extends Thread {
    private final Object first;
    private final Object second;

    DeadlockTask(Object first, Object second, String name) {
      super(name);
      this.first = first;
      this.second = second;
    }

    public void run() {
      synchronized (first) {
        System.out.println(getName() + " acquired first lock");
        try {
          // Allow the other thread to acquire its first lock.
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        synchronized (second) {
          System.out.println(getName() + " acquired second lock");
        }
      }
    }
  }
}