package com.damai.damaiticket.controller;

import com.damai.damaiticket.common.R;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    // ✅ 项目根目录下的 uploads
    private static final File BASE_DIR;

    static {
        try {
            // JVM 启动目录（IDEA 中 = 后端项目根目录）
            BASE_DIR = new File("uploads").getCanonicalFile();
        } catch (Exception e) {
            throw new RuntimeException("初始化上传目录失败", e);
        }
    }

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return R.fail("文件为空");
            }

            String original = file.getOriginalFilename();
            String ext = StringUtils.getFilenameExtension(original);
            if (ext == null) ext = "png";
            ext = ext.toLowerCase();

            if (!("png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "webp".equals(ext))) {
                return R.fail("仅支持 png / jpg / jpeg / webp");
            }

            // 确保 uploads 目录存在
            Files.createDirectories(BASE_DIR.toPath());

            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path dest = BASE_DIR.toPath().resolve(fileName);

            file.transferTo(dest.toFile());

            // 返回前端可访问路径
            return R.ok("/uploads/" + fileName);
        } catch (Exception e) {
            return R.fail("上传失败：" + e.getMessage());
        }
    }

    // 给静态映射使用
    public static String getUploadPath() {
        return BASE_DIR.getAbsolutePath();
    }
}
