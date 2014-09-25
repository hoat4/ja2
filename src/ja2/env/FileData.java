/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import ja2.clazz.JavaObject;
import static ja2.vm.Bytecodes.toInt;

/**
 *
 * @author attila
 */
public class FileData extends INode {

    public byte[] content;
    private final ReadFile reader;
    private final WriteFile writer;

    public FileData(DirectoryData parent, String name) {
        super(parent, name);
        reader = null;
        writer = null;
    }

    public FileData(DirectoryData parent, String name, ReadFile reader, WriteFile writer) {
        super(parent, name);
        this.reader = reader;
        this.writer = writer;
    }

    public ReadFile createReader() {
        if (reader == null)
            return new ReadFile(this);
        else
            return reader;
    }

    public WriteFile createWriter() {
        if (writer == null)
            return new WriteFile(this);
        else
            return writer;
    }

    public static class ReadFile {

        private FileData f;
        private int pos;

        public ReadFile() {
        }

        public ReadFile(FileData file) {
            f = file;
        }

        public byte read() {
            if (pos >= f.content.length - 1)
                return -1;
            else
                return f.content[pos++];
        }

        public int read(JavaObject.JArray jArray, int index, int len) {
            int c = 0;
            for (int i = index; i < index+len; i++) {
                int read = read();
                if(read == -1) {
                    break;
                }
                jArray.array[i] = read;
                c++;
            }
            return c;
        }
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
