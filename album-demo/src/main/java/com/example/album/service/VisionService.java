package com.example.album.service;

import com.aliyun.common.models.RuntimeObject;
import com.aliyun.facebody.Client;
import com.aliyun.facebody.models.RecognizeExpressionAdvanceRequest;
import com.aliyun.facebody.models.RecognizeExpressionResponse;
import com.aliyun.imagerecog.models.*;
import com.aliyuncs.exceptions.ClientException;

import com.example.album.common.ExpressionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class VisionService {
    @Value("${aliyun.accessKeyId}")
    private String accessKey;

    @Value("${aliyun.accessSecret}")
    private String accessSecret;

    static String faceBodyEndpoint = "facebody";
    static String imageRecogEndpoint = "imagerecog";
    static String objectDetEndpoint = "objectdet";


    private com.aliyun.facebody.Client getFaceBodyClient(String endpoint) throws Exception {
        com.aliyun.facebody.models.Config config = new com.aliyun.facebody.models.Config();
        config.accessKeyId = accessKey;
        config.accessKeySecret = accessSecret;
        config.type = "access_key";
        config.regionId = "cn-shanghai";
        config.endpointType = "internal";
        config.endpoint = String.format("%s.%s", endpoint, "cn-shanghai.aliyuncs.com");
        config.protocol = "http";
        return new com.aliyun.facebody.Client(config);
    }

    private com.aliyun.imagerecog.Client getImageRecogClient(String endpoint) throws Exception {
        com.aliyun.imagerecog.models.Config config = new com.aliyun.imagerecog.models.Config();
        config.accessKeyId = accessKey;
        config.accessKeySecret = accessSecret;
        config.type = "access_key";
        config.regionId = "cn-shanghai";
        config.endpointType = "internal";
        config.endpoint = String.format("%s.%s", endpoint, "cn-shanghai.aliyuncs.com");
        config.protocol = "http";
        return new com.aliyun.imagerecog.Client(config);
    }


    public static InputStream getImageStream(String url) throws Exception {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(10 * 1000);
        conn.setReadTimeout(10 * 1000);
        return conn.getInputStream();
    }

    public List<String> recognizeScene(InputStream inputStream) throws Exception {
        RecognizeSceneAdvanceRequest request = new RecognizeSceneAdvanceRequest();
        request.imageURLObject = inputStream;

        List<String> labels = new ArrayList<>();
        try {
            com.aliyun.imagerecog.Client client = getImageRecogClient(imageRecogEndpoint);
            RecognizeSceneResponse resp = client.recognizeSceneAdvance(request, new RuntimeObject());
            for (RecognizeSceneResponse.RecognizeSceneResponseDataTags tag: resp.data.tags) {
                labels.add(tag.value);
            }
        } catch (ClientException e) {
            log.error("ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        }
        return labels;
    }

    public List<String> recognizeExpression(InputStream inputStream) throws Exception {
        RecognizeExpressionAdvanceRequest request = new RecognizeExpressionAdvanceRequest();
        request.imageURLObject = inputStream;

        List<String> labels = new ArrayList<>();
        try {
            Client client = getFaceBodyClient(faceBodyEndpoint);
            RecognizeExpressionResponse resp = client.recognizeExpressionAdvance(request, new RuntimeObject());
            for (RecognizeExpressionResponse.RecognizeExpressionResponseDataElements element : resp.data.elements) {
                labels.add(ExpressionEnum.getNameByNameEn(element.expression));
            }
        } catch (ClientException e) {
            log.error("ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        }
        return labels;
    }

//    public List<String> recognizeStyle(InputStream inputStream) throws Exception {
//        RecognizeImageStyleAdvanceRequest request = new RecognizeImageStyleAdvanceRequest();
//        request.urlObject = inputStream;
//
//        List<String> labels = new ArrayList<>();
//        try {
//            com.aliyun.imagerecog.Client client = getImageRecogClient(imageRecogEndpoint);
//            RecognizeImageStyleResponse resp = client.recognizeImageStyleAdvance(request, new RuntimeObject());
//        } catch (ClientException e) {
//            log.error("ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
//        }
//        return labels;
//    }

}
