package ru.sergalas.hosting;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HostingApplication {


    public static void main(String[] args) throws JsonProcessingException {
        String firstQuestion = "Какой у меня пульс за последние 6 дней? Я буду жить ?";
        String secondQuestion = "Как я себя чувствую, я буду жить?";
        String thirdQuestion = "Учитывая мое здоровье за последние 5 дней могу ли я бежать марафон?";
        Host host = SpringApplication.run(HostingApplication.class, args).getBean(Host.class);
        host.printAnswerToUser(firstQuestion);
        host.printAnswerToUser("как дела ?");
        host.printAnswerToUser(secondQuestion);
        host.printAnswerToUser(thirdQuestion);
    }

}
