//Jackson Lee

import java.io.*;
import java.util.*;

public class H3 {
    //implements a linked list stored in a binary file
    //the list is stored in ascending order based on the keys
    
    int keySize;
    int dataSize;
    RandomAccessFile list;
    long head;
    long free;
    
    private class Node {
        private int[] key = new int[keySize];
        private char[] data = new char[dataSize];
        private long next;
        
        private Node(int[] k, char[] d, long n) {
            key = k;
            data = d;
            next = n;
        }
        
        private Node(long addr) throws IOException {
            //creates an in memory cop of a node stored in the file at address addr
            
            list.seek(addr);
            
            for(int i = 0; i < keySize; i++) {
                key[i] = list.readInt();
            }
            
            for(int i = 0; i < dataSize; i++) {
                data[i] = list.readChar();
            }
            
            next = list.readLong();
        }
        
        private void writeNode(long addr) throws IOException {
            //write the node to the file at address addr
            
            list.seek(addr);
            
            for(int i = 0; i < keySize; i++) {
                list.writeInt(key[i]);
            }
            
            for(int i = 0; i < dataSize; i++) {
                list.writeChar(data[i]);
            }
            
            list.writeLong(next);
        }
    }
    
    public H3(int ks, int ds, String filename) throws IOException {
        //ks is the key size; the number of ints that form the key
        //ds the maximum number of characters in the data that accompanies the key
        //filename is the name of the file in which the list elements are stored
        //this constructor creates a new linked list (i.e. a new empty file will be created)
        //if a file with the given name (i.e. the value of filename) exists, it should be deleted
        
        keySize = ks;
        dataSize = ds;
        File path = new File(filename);
        
        if(path.exists()) {
            path.delete();
        }
        
        list = new RandomAccessFile(path, "rw");
        
        head = 0;
        free = 0;
        
        list.writeLong(head);
        list.writeLong(free);
        list.writeInt(keySize);
        list.writeInt(dataSize);
    }
    
    public H3(String filename) throws IOException {
        //filename is the name of a file that stores a linked list
        //this constructor creates an H3 object that uses a linked list that was previously created
        
        File path = new File(filename);
        
        list = new RandomAccessFile(path, "rw");
        
        list.seek(0);
        
        head = list.readLong();
        free = list.readLong();
        keySize = list.readInt();
        dataSize = list.readInt();
    }
    
    public void insert(int[] key, char[] d) throws IOException {
        //inesrt a new element into the list so the list remains sorted
        Node temp;
        
        if(head == 0 || compareKey((temp = new Node(head)).key, key) >= 0) {
            long addr = getFree();
            temp = new Node(key, d, head);
            temp.writeNode(addr);
            head = addr;
            return;
        }
        
        long tempAddr = head;
        Node temp1;
        Node newNode;
        long addr = getFree();
        
        while(temp.next != 0 && compareKey((temp1 = new Node(temp.next)).key, key) <= 0) {
            tempAddr = temp.next;
            temp = new Node(temp.next);
        }
                
        newNode = new Node(key, d, temp.next);
        newNode.writeNode(addr);
        temp.next = addr;
        temp.writeNode(tempAddr);
    }
    
    public void remove(int[] key) throws IOException {
        //remove all elements in the list that have key value key
        
        long addr;
        long addr1;
        Node temp;
        Node temp1;
        
        while(head != 0 && compareKey((temp = new Node(head)).key, key) == 0) {
            addr = temp.next;
            addFree(head, temp);
            head = addr;
        }
        
        if(head == 0) {
            return;
        }
        
        temp = new Node(head);
        addr = head;
        
        while(temp.next != 0) {
            temp1 = new Node(temp.next);
            addr1 = temp.next;
            
            if(compareKey(temp1.key, key) == 0) {
                temp.next = temp1.next;
                addFree(addr1, temp1);
            } else {
                temp.writeNode(addr);
                temp = temp1;
                addr = addr1;
            }
        }
        
        temp.writeNode(addr);
    }
    
    public void print() throws IOException {
        //print the contents of the list to standard output
        //each element (an element is all the ints in the key and the data) should be printed on a separate line
        
        long addr = head;
        Node temp;
        
        while(addr != 0) {
            temp = new Node(addr);
            for(int i = 0; i < keySize; i++) {
                System.out.print(temp.key[i] + " ");
            }
            
            for(int i = 0; i < dataSize; i++) {
                System.out.print(temp.data[i]);
            }
            
            System.out.println();
            addr = temp.next;
        }
    }
    
    public String[] find(int[] key) throws IOException {
        //return the data (there could be more than one) associated with the key value
        //if the key is not in the list return null;
        
        long addr = head;
        Node temp;
        int count = 0;
        String[] tempArr = new String[dataSize];
        
        while(addr != 0) {
            if(compareKey((temp = new Node(addr)).key, key) == 0) {
                String s = "";
                
                for(int i = 0; i < dataSize; i++) {
                    s = s + temp.data[i];
                }
                
                tempArr[count] = s;
                count++;
            } 
            addr = temp.next;
        }
        
        if(tempArr[0] == null) {
            return null;
        }
        
        int newSize = 0;
        
        for(int i = 0; i < dataSize; i++) {
            if(tempArr[i] != null) {
                newSize++;
            }
        }
        
        String[] arr = new String[newSize];
        
        for(int i = 0; i < newSize; i++) {
            arr[i] = tempArr[i];
        }
        
        return arr;
    }
    
    public void close() throws IOException {
        //close the linked list (i.e. close the binary file storing the linked list)
        //the object should not be used after close is called
        
        list.seek(0);
        list.writeLong(head);
        list.writeLong(free);
        list.close();
    }
    
    private long getFree() throws IOException {
        long addr = 0;
        Node temp;
        
        if(free == 0) {
            addr = list.length();
        } else {
            addr = free;
            temp = new Node(free);
            free = temp.next;
        }
        
        return addr;
    }
    
    private void addFree(long newFree, Node temp) throws IOException {
        temp.next = free;
        temp.writeNode(newFree);
        free = newFree;
    }
    
    private int compareKey(int[] tempKey, int[] key) throws IOException {
        for(int i = 0; i < keySize; i++) {
            if(tempKey[i] > key[i]) {
                return 1;
            } else if(tempKey[i] < key[i]) {
                return -1;
            }
        }
        
        return 0;
    }
}