package com.poisonedyouth.amqp.repository;

import com.poisonedyouth.amqp.dto.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {
}
