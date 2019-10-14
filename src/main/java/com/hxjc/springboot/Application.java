package com.hxjc.springboot;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
//@EnableDubboConfiguration  //开启dubbo的自动配置  开启dubbo配置支持
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
