package com.lab.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 设备图片上传工具（安全加固版）。
 *
 * <p>上传目录位于 webapp 下，属于"可被容器直接访问"的位置，
 * 因此必须做三重校验，否则攻击者上传 {@code xxx.jsp} 会被 Tomcat 当脚本执行：
 * <ol>
 *   <li>后缀白名单：只允许 jpg / jpeg / png / gif / webp；</li>
 *   <li>真实内容校验：用 ImageIO 解码，非图片（如伪装的 jsp/php）直接拒绝；</li>
 *   <li>大小与尺寸上限：防止超大文件拖垮磁盘与页面加载。</li>
 * </ol>
 */
public class UploadUtil {

    /** 上传图片存放目录（webapp 下 /upload 文件夹）。 */
    private static final String UPLOAD_FOLDER = "/upload";

    /** 允许的后缀白名单。 */
    private static final Set<String> ALLOW_SUFFIX =
            new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

    /** 单张图片大小上限（2MB）。 */
    private static final long MAX_SIZE = 2L * 1024 * 1024;

    /** 允许的最大像素宽/高，超出拒绝，避免超大图拖慢列表页。 */
    private static final int MAX_PIXEL = 4000;

    /**
     * 单文件图片上传。
     *
     * @param part    前端上传文件对象
     * @param request 请求对象，用于获取项目真实磁盘路径
     * @return 可直接用于页面回显的图片相对路径；文件为空或校验不通过返回空字符串
     */
    public static String uploadImage(Part part, HttpServletRequest request) {
        if (part == null || part.getSize() <= 0) {
            return "";
        }
        // 1. 大小校验：超过 2MB 直接拒绝，不落盘
        if (part.getSize() > MAX_SIZE) {
            return "";
        }

        String originalFileName = part.getSubmittedFileName();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "";
        }

        // 2. 后缀白名单校验（统一转小写，防止 Shell.JSP、shell.Jsp 绕过）
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        String suffix = originalFileName.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!ALLOW_SUFFIX.contains(suffix)) {
            return "";
        }

        File target = null;
        try {
            // 3. 真实内容校验：必须是能被 ImageIO 解码的图片，且尺寸在允许范围内。
            //    这一步能拦掉"改后缀的 jsp/php 木马"——它们无法被解码为图片。
            BufferedImage image;
            try (InputStream in = part.getInputStream()) {
                image = ImageIO.read(in);
            }
            if (image == null) {
                return "";
            }
            if (image.getWidth() > MAX_PIXEL || image.getHeight() > MAX_PIXEL) {
                return "";
            }

            // 4. 落盘：文件名用 UUID 重新生成，杜绝重名覆盖与路径穿越
            String realPath = request.getServletContext().getRealPath(UPLOAD_FOLDER);
            File uploadDir = new File(realPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String uniqueFileName = UUID.randomUUID() + suffix;
            target = new File(uploadDir, uniqueFileName);
            part.write(target.getAbsolutePath());

            // 返回浏览器可访问的相对路径 /upload/xxx.jpg
            return UPLOAD_FOLDER + "/" + uniqueFileName;
        } catch (IOException e) {
            // 落盘失败时清理可能产生的半截文件，避免垃圾堆积
            if (target != null && target.exists()) {
                target.delete();
            }
            return "";
        }
    }
}
