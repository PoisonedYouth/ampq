package com.poisonedyouth.amqp.service;

import com.poisonedyouth.amqp.dto.Message;
import com.poisonedyouth.amqp.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MessageService {

	private MessageRepository messageRepository;

	public MessageService(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	public void persistMessage(String messageContent) {
		Message message = new Message();
		message.setCreated(new Date());
		message.setMessage(messageContent);
		messageRepository.save(message);
	}
}
