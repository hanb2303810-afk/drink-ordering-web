package com.group04;

import java.io.*;
import java.util.*;

/**
 * 
 */
public interface Preparable {

    /**
     * @return mo ta cac buoc pha che
     */
    String prepare();

    /**
     * @return thoi gian pha che tinh bang mili giay
     */
    int getPrepTimeMs();

}

