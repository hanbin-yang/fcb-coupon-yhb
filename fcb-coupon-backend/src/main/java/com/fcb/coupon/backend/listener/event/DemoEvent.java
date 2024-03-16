package com.fcb.coupon.backend.listener.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:54:00
 */
public class DemoEvent extends ApplicationEvent {

    private String message;


    public DemoEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
