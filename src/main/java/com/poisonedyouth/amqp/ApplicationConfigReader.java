package com.poisonedyouth.amqp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfigReader {
	@Value("${app.exchange.name}")
	private String appExchange;
	@Value("${app.queue.name}")
	private String appQueue;
	@Value("${app.routing.key}")
	private String appRoutingKey;
	// All getters and setters

	public String getAppExchange() {
		return appExchange;
	}

	public void setAppExchange(String appExchange) {
		this.appExchange = appExchange;
	}

	public String getAppQueue() {
		return appQueue;
	}

	public void setAppQueue(String appQueue) {
		this.appQueue = appQueue;
	}

	public String getAppRoutingKey() {
		return appRoutingKey;
	}

	public void setAppRoutingKey(String appRoutingKey) {
		this.appRoutingKey = appRoutingKey;
	}
}