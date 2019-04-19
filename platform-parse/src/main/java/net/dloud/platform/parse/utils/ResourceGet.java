package net.dloud.platform.parse.utils;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.parse.boot.DloudURLClassloader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author QuDasheng
 * @create 2018-09-12 10:38
 **/
@Slf4j
public class ResourceGet {
    private static DloudURLClassloader dloudURLClassloader;


    public static byte[] resource2Byte(ClassPathResource input) {
        log.info("[COMMON] 开始读取文件 {}", input.getPath());
        try (InputStream is = input.getInputStream()) {
            return input2Byte(is);
        } catch (IOException e) {
            throw new InnerException("转换失败", e);
        }
    }

    public static byte[] resourceFile2Byte(String path) {
        return resourceFile2Byte(patternResolver(), path);
    }

    public static byte[] resourceFile2Byte(ClassLoader loader, String path) {
        return resourceFile2Byte(new PathMatchingResourcePatternResolver(loader), path);
    }

    public static byte[] resourceFile2Byte(ResourcePatternResolver resolver, String path) {
        return resourceFile2Byte(resolver, path, 0);
    }

    public static byte[] resourceFile2Byte(ResourcePatternResolver resolver, String path, int idx) {
        try {
            Resource[] resources = resolver.getResources(path);
            if (resources.length <= 0) {
                throw new InnerException("输入路径不存在");
            }

            final Resource resource = resources[idx];
            log.info("[COMMON] 开始读取文件 {}", resource.getURL().getPath());
            if (resource.exists()) {
                return resource2Byte(resource);
            } else {
                return new byte[0];
            }
        } catch (IOException e) {
            throw new InnerException("转换失败", e);
        }
    }

    public static byte[][] resourceMulti2Byte(String path) {
        return resourceMulti2Byte(patternResolver(), path);
    }

    public static byte[][] resourceMulti2Byte(ClassLoader loader, String path) {
        return resourceMulti2Byte(new PathMatchingResourcePatternResolver(loader), path);
    }

    public static byte[][] resourceMulti2Byte(ResourcePatternResolver resolver, String path) {
        try {
            Resource[] resources = resolver.getResources(path);
            if (resources.length <= 0) {
                throw new InnerException("输入路径不存在");
            }

            final byte[][] result = new byte[resources.length][];
            for (int i = 0; i < resources.length; i++) {
                final Resource resource = resources[i];
                log.info("[COMMON] 开始读取文件 {}", resource.getURL().getPath());
                if (resource.exists()) {
                    result[i] = resource2Byte(resource);
                }
            }
            return result;
        } catch (IOException e) {
            throw new InnerException("转换失败", e);
        }
    }

    public static byte[] resource2Byte(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            return input2Byte(is);
        } catch (IOException e) {
            throw new InnerException("转换失败", e);
        }
    }

    public static byte[] file2Byte(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return input2Byte(is);
        } catch (IOException e) {
            throw new InnerException("转换失败", e);
        }
    }

    public static byte[] input2Byte(InputStream is) throws IOException {
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        return buffer;
    }

    private static PathMatchingResourcePatternResolver patternResolver() {
        if (null != dloudURLClassloader) {
            return new PathMatchingResourcePatternResolver(dloudURLClassloader);
        }

        if (null == StartupConstants.RUN_LIBS) {
            return new PathMatchingResourcePatternResolver();
        } else {
            dloudURLClassloader = DloudURLClassloader
                    .getInstance(StartupConstants.RUN_LIBS, ClassLoader.getSystemClassLoader());
            return new PathMatchingResourcePatternResolver(dloudURLClassloader);
        }
    }
}
