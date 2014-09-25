/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import ja2.env.FileData.ReadFile;
import java.util.ArrayList;
import java.util.List;
import ja2.env.FileData.WriteFile;

/**
 *
 * @author attila
 */
public class Files {

    DirectoryData root = new DirectoryData(null, "/");

    public List<ReadFile> readers = new ArrayList<>();
    public List<WriteFile> writers = new ArrayList<>();
    private String wd = "/user";

    public INode inode(String path) throws FileException {
        path = refinePath(path);
        return root.inode(path);
    }

    public FileData file(String path) throws FileException {
        return (FileData) inode(path);
    }

    public FileData createEmpty(String path) throws FileException {
        path = refinePath(path);
        int index = path.lastIndexOf('/');
        DirectoryData dir = root;
        if (index >= 0)
            dir = (DirectoryData) inode(path.substring(0, index));
        FileData file = new FileData(root, path.substring(index + 1));
        dir.content.add(file);
        return file;
    }

    public DirectoryData mkdir(String path) throws FileException {
        path = refinePath(path);
        int index = path.lastIndexOf('/');
        DirectoryData dir = root;
        if (index >= 0)
            dir = (DirectoryData) inode("/" + path.substring(0, index));
        DirectoryData result = new DirectoryData(root, path.substring(index + 1));
        dir.content.add(result);
        return result;
    }

    private String refinePath(String path) {
        String op = path;
        if (!path.startsWith("/"))
            path = wd + "/" + path;
        path = path.substring(1);
        System.out.println(op + "->" + path);
        return path;
    }

    public boolean exists(String fpath) {
        try {
            inode(fpath);
            return true;
        } catch (FileException ex) {
            return false;
        }
    }

}
