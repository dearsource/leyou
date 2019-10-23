package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {
    @Autowired
    private OSSProperties prop;

    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg", "image/bmp");
    /**
     * 本地上传图片
     * 返回 图片地址
     * @param file
     * @return
     */
    public String upload(MultipartFile file) {

        String contentType = file.getContentType();
        if (!suffixes.contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        // 2)校验图片内容
        try {
            BufferedImage read = ImageIO.read(file.getInputStream());
            if(read == null){
                throw  new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw  new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

//源文件名
        String originalName = file.getOriginalFilename();
        //后缀
        String suffix = file.getOriginalFilename().substring(originalName.indexOf("."));
        //上传后的文件名
        String imageName = UUID.randomUUID().toString()+suffix;
        String dirPath = "D:\\coding-software\\nginx-1.12.2\\html";
        //上传的目录
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            file.transferTo(new File(dir,imageName));
        } catch (IOException e) {
            e.printStackTrace();
            throw  new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        String imageUrl = "http://image.leyou.com/"+imageName;
        return imageUrl;
    }

    @Autowired
    private OSS client;

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
