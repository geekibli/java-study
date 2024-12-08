package org.my.code.lesson3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

public class SeeCode {

    public void see(){

        Thread.currentThread().resume();

    }
}
