/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author attila
 */
public class MirrorFile extends FileData {

    public MirrorFile(DirectoryData parent, String name, String mirror) throws FileException {
        super(parent, name);
        try {
            InputStream fis = new BufferedInputStream(new FileInputStream(mirror));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(int ch; (ch = fis.read()) != -1;)
                baos.write(ch);
            content = baos.toByteArray();
        } catch (IOException ex) {
            throw new FileException(ex.getMessage());
        }
        
    }

    
}
