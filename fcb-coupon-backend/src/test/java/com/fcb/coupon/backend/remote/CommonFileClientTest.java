package com.fcb.coupon.backend.remote;

import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.file.CommonMultipartFile;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public class CommonFileClientTest extends BaseTest {

    @Autowired
    private CommonFileClient commonFileClient;

    @Test
    public void testUploadFile() throws Exception {
        Resource resource = new ClassPathResource("/test.xls");
        MultipartFile multipartFile = new CommonMultipartFile("file", "test.xls", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", resource.getInputStream());
        ResponseDto<String> responseDto = commonFileClient.uploadFileByFixedFileName(multipartFile, "/excel/export");
        assert StringUtils.isNotBlank(responseDto.getData());
    }

}
