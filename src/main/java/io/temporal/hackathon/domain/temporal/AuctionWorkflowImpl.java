package io.temporal.hackathon.domain.temporal;

import io.temporal.hackathon.domain.auction.AuctionStats;
import io.temporal.hackathon.domain.timer.Timer;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import java.time.Duration;

public class AuctionWorkflowImpl implements AuctionWorkflow {

	private final Logger logger = Workflow.getLogger(AuctionWorkflowImpl.class);

	private static final Duration THIRTY_SECONDS = Duration.ofSeconds(30);
	private final Timer timer = new Timer();
	private boolean hasEnded;
	private long currentPrice;

    @Override
    public long startAuction(String auctionId) {
		while (!hasEnded) {
			timer.sleep(Workflow.currentTimeMillis() + THIRTY_SECONDS.toMillis());
			if (timer.getSleepTime() > THIRTY_SECONDS.toMillis()) {
				logger.info("Ending auction, no bid received in last 30 seconds");
				hasEnded = true;
			}
		}
        return currentPrice;
    }

	@Override
	public void bid(String userId, Long amount) {
		logger.info("Bid received");

		// use validator instead?
		if (amount <= currentPrice || hasEnded) {
			logger.info("Bid rejected. Amount {} is too low", amount);
			return;
		}

		logger.info("Bid accepted. Price is now {}", amount);
		currentPrice = amount;
		timer.updateWakeUpTime(Workflow.currentTimeMillis() + THIRTY_SECONDS.toMillis());
		timer.sleep(Workflow.currentTimeMillis() + THIRTY_SECONDS.toMillis());
		logger.info("Timer updated. Current sleep time is {}", timer.getSleepTime());
	}

	@Override
	public AuctionStats getStats(String auctionId) {
		return null;
	}

	@Override
	public void end() {
		hasEnded = true;
	}
}
