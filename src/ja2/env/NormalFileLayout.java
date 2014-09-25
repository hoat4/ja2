/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import ja2.platform.desktop.Main;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author attila
 */
public class NormalFileLayout {

    public static Files create() {
        try {
            Files fs = new Files();
            fs.mkdir("/lib");
            DirectoryData nativedir = fs.mkdir("/lib/native");
            nativedir.content.addAll(natives(nativedir));
            fs.mkdir("/lib/ext");
            fs.mkdir("/lib/classes");
            fs.root.content.add(new MirrorDir(fs.root, "user", Main.getStringConfig("vm.file.userdir")));
            fs.mkdir("/remote");
            DirectoryData datadir = fs.mkdir("/data");
            datadir.content.add(createStdout(datadir));
            datadir.content.add(createStderr(datadir));
            datadir.content.add(createStdin(datadir));
            fs.writers.add(fs.file("/data/stdin").createWriter());
            fs.writers.add(fs.file("/data/stdout").createWriter());
            fs.writers.add(fs.file("/data/stderr").createWriter());
            return fs;
        } catch (FileException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Collection<? extends INode> natives(DirectoryData dir) {
        FileData jz = new FileData(dir, "zip.lib");
        jz.content = new byte[]{0, 1};
        return Arrays.asList(jz);
    }

    private static INode createStdout(DirectoryData datadir) {
        return new FileData(datadir, "stdout", null, new FileData.WriteFile() {

            @Override
            public void write(byte b) {
                System.out.write(b);
                System.out.flush();
            }

        });
    }

    private static INode createStderr(DirectoryData datadir) {
        return new FileData(datadir, "stderr", null, new FileData.WriteFile() {

            @Override
            public void write(byte b) {
                System.err.write(b);
                System.err.flush();
            }

        });
    }

    private static INode createStdin(DirectoryData datadir) {
        return new FileData(datadir, "stdin", new FileData.ReadFile() {
            
        }, new FileData.WriteFile() {

            @Override
            public void write(byte b) {
                System.out.write(b);
                System.out.flush();
            }

        });
    }

}
