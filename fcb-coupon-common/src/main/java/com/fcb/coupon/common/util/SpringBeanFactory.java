package com.fcb.coupon.common.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * spring容器代理类，方便从容器中取bean
 * @author YangHanBin
 */
@Component
public class SpringBeanFactory implements BeanFactoryAware {

	private static BeanFactory factory;

	@Override
	public void setBeanFactory(@Nullable BeanFactory beanFactory) throws BeansException {
		factory = beanFactory;
	}

	public static Object getBean(String name) {
		return factory.getBean(name);
	}

	public static <T> T getBean(String name , Class<T> requiredType) {
		return factory.getBean(name, requiredType);
	}

	public static <T> T getBean(Class<T> requiredType) {
		return factory.getBean(requiredType);
	}
}

