package com.poisonedyouth.amqp;

import com.poisonedyouth.amqp.service.MessageService;
import com.poisonedyouth.amqp.util.ApplicationConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Message Listener for RabbitMQ
 */
@Service
public class MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	private ApplicationConfigReader applicationConfigReader;
	private MessageService messageService;

	public MessageListener(ApplicationConfigReader applicationConfigReader, MessageService messageService) {
		this.applicationConfigReader = applicationConfigReader;
		this.messageService = messageService;
	}

	/**
	 * Message listener for app
	 */
	@RabbitListener(queues = "${app.queue.name}")
	public void receiveMessage(String reqObj) {
		log.info("Received message: {} from app queue.", reqObj);
		try {
			log.info("Making REST call to the API");
			messageService.persistMessage(reqObj);
			log.info("<< Exiting receiveMessageCrawlCI() after API call.");
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
				log.info("Delay...");
				try {
					Thread.sleep(ApplicationConstant.MESSAGE_RETRY_DELAY);
				} catch (InterruptedException e) {
					log.error("Sleeping thread interrupted.", e);
					Thread.currentThread().interrupt();
				}
				log.info("Throwing exception so that message will be requed in the queue.");
				// Note: Typically Application specific exception can be thrown below
			} else {
				throw new AmqpRejectAndDontRequeueException(ex);
			}
		} catch (Exception e) {
			log.error("Internal server error occurred in server. Bypassing message requeue {}", reqObj, e);
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}
}
