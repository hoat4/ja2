/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.env;

import javainterpreter.clazz.JavaObject;
import static javainterpreter.vm.Bytecodes.toInt;

/**
 *
 * @author attila
 */
public class FileData extends INode {

    public byte[] content;
    private final WriteFile writer;

    public FileData(DirectoryData parent, String name) {
        super(parent, name);
        writer = null;
    }

    public FileData(DirectoryData parent, String name, WriteFile writer) {
        super(parent, name);
        this.writer = writer;
    }

    public WriteFile createWriter() {
        if (writer == null)
            return new WriteFile(this);
        else
            return writer;
    }
    
    public static class WriteFile {

        private FileData f;

        public WriteFile() {
        }

        public WriteFile(FileData file) {
            f = file;
        }

        public void write(JavaObject.JArray jarray, int index, int len) {
            for (int i = index; i < index + len; i++)
                write((byte) toInt(jarray.array[i]));
        }

        public void write(byte b) {
            byte[] modified = new byte[f.content.length + 1];
            for (int i = 0; i < modified.length - 1; i++)
                modified[i] = f.content[i];
            f.content = modified;
        }
    }

}
