package com.infuq.springframe;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeMonitorAspect {

    @Pointcut("execution(* com.infuq.springframe.QueryComputerService.*(..))")
    public void pointCut(){}

    @Around("pointCut()")
    public Object aroundTimeCounter(ProceedingJoinPoint jpx){
        long start = System.currentTimeMillis();
        Object proceed = null;
        try {
            proceed = jpx.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        long end = System.currentTimeMillis();
		System.out.println(end - start);
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