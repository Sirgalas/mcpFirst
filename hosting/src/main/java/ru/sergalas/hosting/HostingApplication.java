package ru.sergalas.hosting;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HostingApplication {


    public static void main(String[] args) throws JsonProcessingException {
        String firstQuestion = "Какой у меня пульс";
        String secondQuestion = "Как дела";
        String thirdQuestion = "Измерь мой пульс и добавь к пульсу 1000 что будет";
        Host host = SpringApplication.run(HostingApplication.class, args).getBean(Host.class);
        host.printAnswerToUser(firstQuestion);
        host.printAnswerToUser(secondQuestion);
        host.printAnswerToUser(thirdQuestion);
    }

}
