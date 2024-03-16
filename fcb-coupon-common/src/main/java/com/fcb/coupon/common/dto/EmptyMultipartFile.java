package com.fcb.coupon.common.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description 空文件参数类
 * @createTime 2021年06月16日 12:04:00
 */
public class EmptyMultipartFile implements Serializable {

    @JSONField(serialize = false)
    @JsonIgnore
    private MultipartFile src;

    public EmptyMultipartFile(MultipartFile src) {
        this.src = src;
    }

    public String getName() {
        return this.src.getName();
    }

    public String getOriginalFilename() {
        return this.src.getOriginalFilename();
    }

    public String getContentType() {
        return this.src.getContentType();
    }

    public boolean isEmpty() {
        return this.src.isEmpty();
    }

    public long getSize() {
        return this.src.getSize();
    }

    public byte[] getBytes() {
        return new byte[0];
    }

}
