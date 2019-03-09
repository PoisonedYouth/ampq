package com.poisonedyouth.amqp;

import org.apache.commons.text.RandomStringGenerator;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableRabbit
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AmqpApplication extends SpringBootServletInitializer implements RabbitListenerConfigurer {

	@Autowired
	private ApplicationConfigReader applicationConfig;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private MessageSender messageSender;

	public ApplicationConfigReader getApplicationConfig() {
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfigReader applicationConfig, RabbitTemplate rabbitTemplate, MessageSender messageSender) {
		this.applicationConfig = applicationConfig;
		this.rabbitTemplate = rabbitTemplate;
		this.messageSender = messageSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(AmqpApplication.class, args);
	}

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(AmqpApplication.class);
	}

	/* This bean is to read the properties file configs */
	@Bean
	public ApplicationConfigReader applicationConfig() {
		return new ApplicationConfigReader();
	}

	/* Creating a bean for the Message queue Exchange */
	@Bean
	public TopicExchange getAppExchange() {
		return new TopicExchange(getApplicationConfig().getAppExchange());
	}

	/* Creating a bean for the Message queue */
	@Bean
	public Queue getAppQueue() {
		return new Queue(getApplicationConfig().getAppQueue());
	}

	/* Binding between Exchange and Queue using routing key */
	@Bean
	public Binding declareBindingApp() {
		return BindingBuilder.bind(getAppQueue()).to(getAppExchange()).with(getApplicationConfig().getAppRoutingKey());
	}

	/* Bean for rabbitTemplate */
	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
		return new MappingJackson2MessageConverter();
	}

	@Bean
	public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(consumerJackson2MessageConverter());
		return factory;
	}

	@Override
	public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
		registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}

	@Scheduled(fixedDelay = 5000)
	public void sendMessage() {
		RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange(33, 45).build();
		messageSender.sendMessage(rabbitTemplate,
				applicationConfig.getAppExchange(),
				applicationConfig.getAppRoutingKey(),
				randomStringGenerator.generate(20));
	}
}
