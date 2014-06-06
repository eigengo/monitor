package org.eigengo.monitor.output.dtrace;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            new DtraceCounterInterface().incrementCounter("akka://foo");
            Thread.sleep(1000L);
        }
    }

}
