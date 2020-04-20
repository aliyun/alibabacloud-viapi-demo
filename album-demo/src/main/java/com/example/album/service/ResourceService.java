package com.example.album.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class ResourceService {
    private String scene;
    @Value("${data.storage.path}")
    private String storagePath;

    @Value("${img.storage.path}")
    private String imagePath;

    static final String fileName = "data.json";
    private LabelModel labelModel;

    @Autowired
    private VisionService visionService;

    @PostConstruct
    public void loadMetaData() {
        log.info("load");
        try {
            InputStream inputStream = new FileInputStream(storagePath + fileName);
            labelModel = JSONObject.parseObject(inputStream, LabelModel.class);
        } catch (IOException e) {
            log.error(e.toString());
            labelModel = new LabelModel();
        }
    }

    @PreDestroy
    public void saveMetaData() {
        log.info("save");
        try {
            System.out.println(JSON.toJSONString(labelModel));
            OutputStream outputStream = new FileOutputStream(storagePath + fileName);
            outputStream.write(JSON.toJSONBytes(labelModel));
            outputStream.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }



    public List<String> getPhotosByCateAndLabel(String cate, String label) {
        return getAccessPath(labelModel.getImgByCateAndLabel(cate, label));
    }

    public List<String> getPhotosByCate(String cate) {
        return getAccessPath(labelModel.getImgByCate(cate));
    }

    public List<String> getAllPhotos()  {
        return getAccessPath(labelModel.getAllImg());
    }

    public Object getAllCates() {
        return labelModel.cateMap;
    }

    private List<String> getAccessPath(List<String> imgs) {
        List<String> result = new ArrayList<>();
        imgs.stream().forEach(img-> {
            result.add(String.format("/img/%s", img));
        });
        return  result;
    }

    public void saveAndRecognizeImage(String filename, InputStream inputStream) {
        log.info("saveImage");
        try {

            //识别场景
            inputStream.reset();
            inputStream.mark(0);
            List<String> scenes = visionService.recognizeScene(inputStream);
            labelModel.addImg(LabelModel.SCENE, filename, scenes);

            inputStream.reset();
            inputStream.mark(0);
            List<String> expressions = visionService.recognizeExpression(inputStream);
            labelModel.addImg(LabelModel.EXPRESSION, filename, expressions);

            //重复使用InputStream需要reset、mark操作
            inputStream.reset();
            inputStream.mark(0);
            //保存文件
            System.out.println(imagePath+filename);
            OutputStream outputStream = new FileOutputStream(imagePath + filename);
            IOUtils.copy(inputStream, outputStream);

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }

    @Data
    static class LabelModel {
        static final String SCENE = "scene";
        static final String EXPRESSION = "expression";
        static final String STYLE = "style";
        //保存不同场景标签包含的图片
        private Map<String, Set<String>> sceneMap;
        //保存不同表情表情包含的图片
        private Map<String, Set<String>> expressionMap;

        //保存不同风格包含的图片
        private Map<String, Set<String>> styleMap;
        private Map<String, Set<String>> cateMap;


        //图片中包含的标签
        //key：图片地址
        //value: Set中value值为: {[scene|expression|style]}_{label},例如：style_怀旧
        private Map<String, Set<String>> imgLabels;

        public LabelModel(){
           this.imgLabels = new HashMap<>();
           this.sceneMap = new HashMap<>();
           this.expressionMap = new HashMap<>();
           this.styleMap = new HashMap<>();
           this.cateMap = new HashMap<>();
        }

        public List<String> getAllImg() {
            List<String> result = new ArrayList<>();
            imgLabels.forEach((k, v)-> {
                result.add(k);
            });

            return result.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }

        public List<String> getImgByCate(String cate) {
            Map<String, Set<String>> data = new HashMap<>();
            switch (cate) {
                case SCENE: {
                    data = sceneMap;
                    break;
                }
                case EXPRESSION: {
                    data = expressionMap;
                    break;
                }
                case STYLE: {
                    data = styleMap;
                    break;
                }
            }

            Set<String> result = new HashSet<>();
            data.forEach((k, d)-> {
                result.addAll(d);
            });


            return  result.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }

        public List<String> getImgByCateAndLabel(String cate, String label) {
            System.out.println(cate);
            System.out.println(label);
            String key = String.format("%s_%s", cate, label);

            Set<String> result = new HashSet<>();
            switch (cate) {
                case SCENE: {
                    result = sceneMap.get(key);
                    break;
                }
                case EXPRESSION: {
                    result = expressionMap.get(key);
                    break;
                }
                case STYLE: {
                    result = styleMap.get(key);
                    break;
                }
            }

            return result.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }

        void addImg(String cate, String img, List<String> labels) {
            for (String label : labels) {
                String item = String.format("%s_%s", cate, label);
                Set<String> imgSet = imgLabels.getOrDefault(img, new HashSet<>());
                imgSet.add(item);
                imgLabels.put(img, imgSet);
                switch (cate) {
                    case SCENE: {
                        Set<String> sceneSet = sceneMap.getOrDefault(item, new HashSet<>());
                        sceneSet.add(img);
                        sceneMap.put(item, sceneSet);
                        Set<String> cateSet = cateMap.getOrDefault(cate, new HashSet<>());
                        cateSet.add(label);
                        cateMap.put(cate, cateSet);
                        break;
                    }
                    case EXPRESSION: {
                        Set<String> expressionSet = expressionMap.getOrDefault(item, new HashSet<>());
                        expressionSet.add(img);
                        expressionMap.put(item, expressionSet);
                        Set<String> cateSet = cateMap.getOrDefault(cate, new HashSet<>());
                        cateSet.add(label);
                        cateMap.put(cate, cateSet);
                        break;
                    }
                    case STYLE: {
                        Set<String> styleSet = styleMap.getOrDefault(item, new HashSet<>());
                        styleSet.add(img);
                        styleMap.put(item, styleSet);
                        Set<String> cateSet = cateMap.getOrDefault(cate, new HashSet<>());
                        cateSet.add(label);
                        cateMap.put(cate, cateSet);
                        break;
                    }
                }
            }
        }

        void removeImg(String img) {
            Set<String> labels = imgLabels.remove(img);
            labels.stream().forEach(label -> {
                String[] segs = label.split("_");
                String cate = segs[0];
                String labelKey = segs[1];
                switch (cate) {
                    case SCENE: {
                        sceneMap.get(labelKey).remove(img);
                        break;
                    }
                    case EXPRESSION: {
                        expressionMap.get(labelKey).remove(img);
                        break;
                    }
                    case STYLE: {
                        styleMap.get(labelKey).remove(img);
                        break;
                    }
                }
            });
        }
    }
}
