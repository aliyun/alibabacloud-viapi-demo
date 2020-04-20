package com.example.album.controller;

import com.example.album.service.ResourceService;
import com.example.album.service.VisionService;
import com.example.album.utils.Md5CaculateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/album/v1")
@Slf4j
public class AlbumController {
    @Autowired
    private VisionService visionService;

    @Autowired
    private ResourceService resourceService;




    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Object getList() throws Exception {
        return resourceService.getAllPhotos();
    }


    @RequestMapping(value = "/allCates", method = RequestMethod.GET)
    public Object getCates() throws Exception {
        return resourceService.getAllCates();
    }


    @RequestMapping(value = "/getPhotosByCateAndLabel", method = RequestMethod.GET)
    public Object getPhotosByCateAndLabel(@RequestParam(name="cate") String cate, @RequestParam(name="tag") String tag) throws Exception {
        return resourceService.getPhotosByCateAndLabel(cate, tag);
    }

    @RequestMapping(value = "/getPhotosByCate", method = RequestMethod.GET)
    public Object getPhotosByCate(@RequestParam(name="cate") String cate) throws Exception {
        return resourceService.getPhotosByCate(cate);
    }

    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file) throws Exception {
        //计算上传文件的md5值，作为文件名
        byte[] bytes = file.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        String md5Str = Md5CaculateUtil.getMD5(inputStream);
        inputStream.reset();
        inputStream.mark(0);

        String fileName = file.getOriginalFilename();
        String fType = fileName.substring(fileName.lastIndexOf("."));
        fileName = String.format("%s%s", md5Str, fType);
        resourceService.saveAndRecognizeImage(fileName, inputStream);
        return fileName;
    }
}
