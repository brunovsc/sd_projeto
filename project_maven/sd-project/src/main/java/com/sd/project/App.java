package com.sd.project;

import io.atomix.*;
import java.net.InetAddress;
import java.net.Socket;
import io.atomix.catalyst.transport.Address;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
	AtomixReplica.Builder builder = AtomixReplica.builder(new Address("localhost", 8700));
	AtomixClient client;
    }
}
