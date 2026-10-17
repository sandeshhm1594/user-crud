package com.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@GetMapping("/home")
	public String home()
	{
		System.out.println("Hello");
		return "Welcome to Devops";
	}
}
