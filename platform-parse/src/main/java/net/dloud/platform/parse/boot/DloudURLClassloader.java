package net.dloud.platform.parse.boot;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.exception.InnerException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2019-04-04 14:16
 **/
@Slf4j
public class DloudURLClassloader extends URLClassLoader {

    public DloudURLClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public synchronized static DloudURLClassloader getInstance(String path, ClassLoader parent) {
        if (null == path) {
            throw new InnerException("path must not be null");
        }

        final URL[] urls;
        final File dir = Paths.get(path).toFile();
        try {
            if (dir.isFile()) {
                System.out.println(dir.getAbsolutePath());
                urls = new URL[]{jarURL(dir.getAbsolutePath())};
            } else {
                final String[] names = dir.list();
                if (null == names || names.length <= 0) {
                    throw new InnerException("path is empty");
                }

                final List<URL> list = new ArrayList<>(16);
                for (int i = 0; i < names.length; i++) {
                    final String name = names[i];
                    if (name.endsWith(".jar") && name.contains("-client-")) {
                        list.add(jarURL(dir.getAbsolutePath() + "/" + name));
                    }
                }
                if (list.isEmpty()) {
                    throw new InnerException("path not contains client");
                }

                urls = new URL[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    urls[i] = list.get(i);
                }
            }
        } catch (IOException ex) {
            throw new InnerException("resource get error");
        }

        log.info("add jar: {}", Arrays.toString(urls));
        return new DloudURLClassloader(urls, parent);
    }

    private static URL jarURL(String name) throws IOException {
        return new URL("jar:file:" + name + "!/");
    }
}
