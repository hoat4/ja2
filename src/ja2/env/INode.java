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
public class INode {

    public DirectoryData parent;
    public String name;
    public boolean hidden;

    public INode(DirectoryData parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String path() {
        StringBuilder sb = new StringBuilder();
        INode current = this;
        while (current != null) {
            sb.insert(0, '/').insert(1, current.name.equals("/") ? "" : "/");
            current = current.parent;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return path();
    }

}
