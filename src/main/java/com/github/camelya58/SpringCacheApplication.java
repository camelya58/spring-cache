package com.github.camelya58;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Class SpringCacheApplication connects cashing using annotation @EnableCashing.
 *
 * @author Kamila Meshcheryakova
 * created 28.08.2020
 */
@EnableCaching
@SpringBootApplication
public class SpringCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheApplication.class, args);
    }
}
