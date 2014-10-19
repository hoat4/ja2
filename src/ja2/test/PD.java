/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.test;

import java.util.Arrays;

/**
 *
 * @author attila
 */
public class PD {
    public static void main(String[] args) {
        System.out.println(Arrays.asList(PD.class.getProtectionDomain().getPrincipals()));
        System.out.println(PD.class.getProtectionDomain().getClass());
        System.out.println(PD.class.getProtectionDomain().getPermissions().getClass());
    }
}
