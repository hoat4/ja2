/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.env;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author attila
 */
public class FilesTest {

    @Test
    public void rootDirPath() {
        assertEquals("/", new Files().root.path());
    }
    
    @Test
    public void createFileObject() {
        Files fs = new Files();
        assertNotNull(new FileData(fs.root, "c.dat").parent);
    }
    
    @Test
    public void createEmptyFile() {
        Files fs = new Files();
        FileData file = new FileData(fs.root, "a.txt");
        fs.root.content.add(file);
    }
    
    @Test
    public void dirParentIsRoot() throws Exception {
        Files fs = new Files();
        assertEquals(fs.root, fs.mkdir("/b.txt").parent);
    }

    @Test
    public void testInode() throws Exception {
        Files fs = new Files();
        FileData xdat = fs.createEmpty("/x.dat");
        assertEquals(xdat, fs.inode("/x.dat"));
        DirectoryData folder = fs.mkdir("/testfolder");
        assertEquals(folder, fs.inode("/testfolder"));
    }

    @Test
    public void testCreateEmpty() throws Exception {
        Files fs = new Files();
        assertEquals(fs.root, fs.createEmpty("/b.txt").parent);
    }

    @Test
    public void testMkdir() throws Exception {
        Files fs = new Files();
        assertEquals(fs.root, fs.mkdir("/testfolder").parent);
    }
    
    @Test
    public void testDefaultDirs() {
        
    }
}
