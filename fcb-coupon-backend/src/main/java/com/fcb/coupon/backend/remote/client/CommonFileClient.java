package com.fcb.coupon.backend.remote.client;

import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "hdb-common-file")
public interface CommonFileClient {


    /**
     * 文件上传,文件名可以指定，需要再请求获取资源
     *
     * @param file
     * @param uploadFileDir
     * @return
     */
    @PostMapping(value = "/webapi/image/uploadImageByFixedFileName", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseDto<String> uploadFileByFixedFileName(@RequestPart(value = "file") MultipartFile file, @RequestParam(value = "uploadFileDir") String uploadFileDir);

}
