package com.example.viapidemo;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.aliyun.ocr.Client;
import com.aliyun.ocr.models.Config;
import com.aliyun.ocr.models.RecognizeIdentityCardAdvanceRequest;
import com.aliyun.ocr.models.RecognizeIdentityCardResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author joffre
 * @date 2020/4/6
 */
@Service
public class OcrService {

    private Client ocrClient;
    private RuntimeOptions runtime;

    @Value("${viapi.accessKeyId}")
    private String accessKeyId;
    @Value("${viapi.accessKeySecret}")
    private String accessKeySecret;

    @PostConstruct
    private void init() throws Exception {
        Config config = new Config();
        config.type = "access_key";
        config.regionId = "cn-shanghai";
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.endpoint = "ocr.cn-shanghai.aliyuncs.com";

        ocrClient = new Client(config);
        runtime = new RuntimeOptions();
    }

    public Map<String, String> RecognizeIdCard(String filePath, String side) throws Exception {

        RecognizeIdentityCardAdvanceRequest request = new RecognizeIdentityCardAdvanceRequest();
        request.imageURLObject = Files.newInputStream(Paths.get(filePath));
        request.side = side;
        RecognizeIdentityCardResponse response = ocrClient.recognizeIdentityCardAdvance(request, runtime);

        if ("face".equals(side)) {
            return JSON.parseObject(JSON.toJSONString(response.data.frontResult), new TypeReference<Map<String, String>>() {});
        } else {
            return JSON.parseObject(JSON.toJSONString(response.data.backResult), new TypeReference<Map<String, String>>() {});
        }
    }
}
