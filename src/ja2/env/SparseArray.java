/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import ja2.vm.BytecodeInstruction;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author attila
 */
public class SparseArray {

    private final int BLOCK_SIZE;
    private final Map<Long, byte[]> blocks = new HashMap<Long, byte[]>();

    public SparseArray() {
        this(512);
    }

    public SparseArray(int blockSize) {
        BLOCK_SIZE = blockSize;
    }

    public byte get(long location) {
        long blockid = location / BLOCK_SIZE;
        byte[] block = blocks.get(blockid);
        if (block == null)
            return 0;
        return block[(int) (location % BLOCK_SIZE)];
    }

    public void put(long location, byte value) {
        long blockid = location / BLOCK_SIZE;
        byte[] block = blocks.get(blockid);
        if (block == null)
            blocks.put(blockid, block = new byte[BLOCK_SIZE]);
        block[(int) (location % BLOCK_SIZE)] = value;
    }

    public void check(long start, long end) {
    }
}
