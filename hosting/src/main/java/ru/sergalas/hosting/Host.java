package ru.sergalas.hosting;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Host {

    @Autowired
    ChatClient chatClient;


    public void printAnswerToUser(String question){
       chatClient.prompt().system()




    }
}
