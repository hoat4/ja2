/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import java.io.File;

/**
 *
 * @author attila
 */
class MirrorDir extends DirectoryData {
    private File mirror;
    public MirrorDir(DirectoryData parent, String name, String mirrorPath) {
        super(parent, name);
        mirror = new File(mirrorPath);
    }

    @Override
    protected INode inodeByName(String name) throws FileException {
        File f= new File(mirror, name);
        if(f.isDirectory()) {
            return new MirrorDir(this, f.getName(), f.getAbsolutePath());
        }else return new MirrorFile(this, f.getName(), f.getAbsolutePath());
    }
    
}
