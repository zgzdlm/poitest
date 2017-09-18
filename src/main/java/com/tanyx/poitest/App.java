package com.tanyx.poitest;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String a = "$+123456";
        System.out.println(a.indexOf("+"));
        System.out.println(a.substring(a.indexOf("+")+1));
        
    }
}
