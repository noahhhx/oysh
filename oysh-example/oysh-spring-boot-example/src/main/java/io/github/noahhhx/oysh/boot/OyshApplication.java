package io.github.noahhhx.oysh.boot;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableOyshShell
public class OyshApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(OyshApplication.class, args);
        new CountDownLatch(1).await();
    }
}
