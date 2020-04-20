package com.example.viapidemo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;

import com.aliyun.tea.TeaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author joffre
 * @date 2020/4/6
 */
@Controller
@RequestMapping("/")
public class MainController {

    private String uploadDirectory;
    private OcrService ocrService;
    private List<String> faceImages;
    private List<String> backImages;
    private List<Map<String, String>> faceResults;
    private List<Map<String, String>> backResults;

    public MainController(@Value("${file.upload.path}") String uploadDirectory, OcrService ocrService) {
        this.uploadDirectory = uploadDirectory;
        this.ocrService = ocrService;
        faceImages = new ArrayList<>();
        backImages = new ArrayList<>();
        faceResults = new ArrayList<>();
        backResults = new ArrayList<>();
    }

    private String saveFile(MultipartFile file) throws Exception {
        String suffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
        String filename = UUID.randomUUID().toString() + "." + suffix;
        Path path = Paths.get(uploadDirectory + filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    @RequestMapping()
    public String index(Model model) {
        if (faceImages.size() != backImages.size()) {
            faceImages.clear();
            backImages.clear();
            faceResults.clear();
            backResults.clear();
        }
        if (!CollectionUtils.isEmpty(faceImages) && faceImages.size() == backImages.size()) {
            model.addAttribute("faceImage", faceImages.get(faceImages.size() - 1));
            model.addAttribute("faceResult", faceResults.get(faceResults.size() - 1));
            model.addAttribute("backImage", backImages.get(backImages.size() - 1));
            model.addAttribute("backResult", backResults.get(backResults.size() - 1));
        }
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("face") MultipartFile face, @RequestParam("back") MultipartFile back, RedirectAttributes attributes) {
        if (face.isEmpty() || back.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        String errorMessage = null;
        try {
            Path dir = Paths.get(uploadDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            if (!face.isEmpty()) {
                String filename = saveFile(face);
                Map<String, String> res = ocrService.RecognizeIdCard(uploadDirectory + filename, "face");
                faceImages.add("/images/" + filename);
                faceResults.add(res);
            }
            if (!back.isEmpty()) {
                String filename = saveFile(back);
                Map<String, String> res = ocrService.RecognizeIdCard(uploadDirectory + filename, "back");
                backImages.add("/images/" + filename);
                backResults.add(res);
            }
        } catch (TeaException e) {
            e.printStackTrace();
            errorMessage = JSON.toJSONString(e.getData());
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            attributes.addFlashAttribute("message", errorMessage);
        }
        return "redirect:/";
    }
}
