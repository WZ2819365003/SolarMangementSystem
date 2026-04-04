package cn.techstar.pvms.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.techstar.pvms.backend")
public class PvmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PvmsBackendApplication.class, args);
    }
}

