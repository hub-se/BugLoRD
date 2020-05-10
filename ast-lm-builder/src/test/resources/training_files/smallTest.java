package org.apache.commons.lang;

public class smallTest {

    public smallTest() {
        super();
    }

    public int doSth(float number) {
        int i = 0;
        if (i == 3) {
            ++i;
        } else {
            --i;
        }
        switch (i) {
            case 1:
                break;
            case 0:
                break;
        }
        return i;
    }

}
