/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author attila
 */
public class DirectoryData extends INode {

    public List<INode> content = new ArrayList<>();

    public DirectoryData(DirectoryData parent, String name) {
        super(parent, name);
    }

    public INode inode(String path) throws FileException {
        int index = path.indexOf('/');
        if (index < 0)
            index = path.length();
        INode inode = inodeByName(path.substring(0, index));
        if (index != path.length())
            return ((DirectoryData) inode).inode(path.substring(index + 1));
        return inode;
    }

    private INode inodeByName(String name) throws FileException {
        for (INode inode : content)
            if (inode.name.equals(name))
                return inode;
        throw new FileException("No such inode \""+name+"\" in "+this);
    }

}
