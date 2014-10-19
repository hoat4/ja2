/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.env;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author attila
 */
public class SparseArrayTest {

    @Test
    public void testZero() {
        SparseArray array = new SparseArray();
        assertEquals(0, array.get(0));
        assertEquals(0, array.get(100000));
        array.put(0, (byte) 0);
        assertEquals(0, array.get(0));
        array.put(100000, (byte) 0);
        assertEquals(0, array.get(100000));
    }

    @Test
    public void testNonzero() {
        SparseArray array = new SparseArray();
        array.put(100000, (byte) 1);
        assertEquals(1, array.get(100000));
        array.put(1, (byte) 10);
        assertEquals(10, array.get(1));
    }
}
