package com.fcb.coupon.common.excel.support;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:39:00
 */
@Slf4j
public class DefaultImportValidator implements ImportValidator {

    private Validator validator;

    public DefaultImportValidator(Validator validator) {
        this.validator = validator;
    }

    private static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }

    private static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    @Override
    public void validate(Object bean) {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        validate(violations, bean);
        if (!violations.isEmpty()) {
            log.error("验证失败: {}, 原因: {}", bean.getClass(), violations);
            throw new ConstraintViolationException("验证失败: " + bean.getClass() + ", 原因: " + violations, violations);
        }
    }

    private void validate(Set<ConstraintViolation<?>> violations, Object arg) {
        if (arg != null && !isPrimitives(arg.getClass())) {
            if (arg instanceof Object[]) {
                for (Object item : (Object[]) arg) {
                    validate(violations, item);
                }
            } else if (arg instanceof Collection) {
                for (Object item : (Collection<?>) arg) {
                    validate(violations, item);
                }
            } else if (arg instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) arg).entrySet()) {
                    validate(violations, entry.getKey());
                    validate(violations, entry.getValue());
                }
            } else {
                violations.addAll(validator.validate(arg));
            }
        }
    }
}
