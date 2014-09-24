/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.env;

import java.util.ArrayList;
import java.util.List;
import javainterpreter.env.FileData.WriteFile;

/**
 *
 * @author attila
 */
public class Files {

    DirectoryData root = new DirectoryData(null, "/");
    public List<WriteFile> writers = new ArrayList<>();

    public INode inode(String path) throws FileException {
        if (path.startsWith("/"))
            path = path.substring(1);
        return root.inode(path);
    }

    public FileData file(String path) throws FileException {
        return (FileData) inode(path);
    }

    public FileData createEmpty(String path) throws FileException {
        if (path.startsWith("/"))
            path = path.substring(1);
        int index = path.lastIndexOf('/');
        DirectoryData dir = root;
        if (index >= 0)
            dir = (DirectoryData) inode(path.substring(0, index));
        FileData file = new FileData(root, path.substring(index + 1));
        dir.content.add(file);
        return file;
    }

    public DirectoryData mkdir(String path) throws FileException {
        if (path.startsWith("/"))
            path = path.substring(1);
        int index = path.lastIndexOf('/');
        DirectoryData dir = root;
        if (index >= 0)
            dir = (DirectoryData) inode(path.substring(0, index));
        DirectoryData result = new DirectoryData(root, path.substring(index + 1));
        dir.content.add(result);
        return result;
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
