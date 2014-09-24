/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javainterpreter.JavaInterpreter;
import javainterpreter.callback.VmCallback;
import javainterpreter.clazz.JavaObject;
import javainterpreter.vm.VmContext;

/**
 *
 * @author attila
 */
public class NativeLibs {

    public static List<Runnable> loaders = new ArrayList<>();
    public static Map<String, VmCallback<VmContext>> libraries = new HashMap<>();

    public static void load(VmContext ctx, Files fs, JavaObject.JClassInstance l) {
        try {
            // j/l/CL$NativeLibrary
            FileData f = (FileData) fs.inode(JavaInterpreter.toString((JavaObject.JClassInstance) l));
            int lid = ((int) f.content[0]) << 8;
            lid |= ((int) f.content[1]);
            if (loaders.size() <= lid)
                JavaInterpreter.error(ctx.thread, "java/lang/UnsatisfiedLinkError", "No such native loader #" + lid);
            loaders.get(lid).run();
        } catch (FileException ex) {
            JavaInterpreter.error(ctx.thread, "java/lang/InternalError", "Can't load native lib");
        }
    }
}
