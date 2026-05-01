package ru.sergalas.hosting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HostingApplication {


    public static void main(String[] args) {
        String firstQuestion = "Какой у меня пульс";
        String secondQuestion = "Как дела";
        String thirdQuestion = "добавь к пульсу 1000 что будет";
        Host host = SpringApplication.run(HostingApplication.class, args).getBean(Host.class);
        host.printAnswerToUser(firstQuestion);
        host.printAnswerToUser(secondQuestion);
        host.printAnswerToUser(thirdQuestion);
    }

}
