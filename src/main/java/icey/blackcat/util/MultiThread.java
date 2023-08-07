package icey.blackcat.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class MultiThread {

    private int total;
    private int split;
    private boolean show = false;
    private CountDownLatch latch;

    public MultiThread(int total, boolean show) {
        this.total = total;
        this.show = show;
        this.latch = new CountDownLatch(total);
        this.split = (int) (total * 0.05);
        if(this.split == 0){
            this.split = 1;
        }
    }

    public void await(){
        long timeout = 30L;
        log.info("Wait for all tasks to complete. Timeout: {}s", timeout);

    }
}
