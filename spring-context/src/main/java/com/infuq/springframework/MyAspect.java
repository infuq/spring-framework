package com.infuq.springframework;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
public class MyAspect {

	@Pointcut("execution(* com.infuq.springframework.ZAnimal.*(..))")
	public void pointCut(){}

	@Around("pointCut()")
	public Object aroundTimeCounter(ProceedingJoinPoint jpx){
		Object proceed = null;
		try {
			proceed = jpx.proceed();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		return proceed;
	}

	@Before("pointCut()")
	public void before() {
		System.out.println("invoke before...");
	}

	@After("pointCut()")
	public void after() {
		System.out.println("invoke after...");
	}



}
