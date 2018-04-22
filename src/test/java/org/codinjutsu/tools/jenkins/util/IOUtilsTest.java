package org.codinjutsu.tools.jenkins.util;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * Created by Cezary on 2015-10-18.
 */
public class IOUtilsTest {

    public static final char SEPARATOR = '/';
    private String POLISH_TEST_STRING = "\u007a\u0061\u017c\u00f3\u0142\u0107 \u0067\u0119\u015b\u006c\u0105 \u006a\u0061\u017a\u0144\u005c\u0072\u005c\u006e" +
          "\u005a\u0041\u017b\u00d3\u0141\u0106 \u0047\u0118\u015a\u004c\u0104 \u004a\u0041\u0179\u0143";

    @Test
    public void testToStringUTF8() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.utf8");
        //when
        final String result = IOUtils.toString(inputStream, "UTF-8");
        //then
        assertThat(result, CoreMatchers.equalTo(POLISH_TEST_STRING));
    }

    @Test
    public void testToStringCP1250() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.cp1250");
        //when
        final String result = IOUtils.toString(inputStream, "CP1250");
        //then
        assertThat(result, CoreMatchers.equalTo(POLISH_TEST_STRING));
    }

    private InputStream getTestResourceInputStream(String resource) {
        final Class<? extends IOUtilsTest> aClass = getClass();
        return aClass.getClassLoader().getResourceAsStream(aClass.getPackage().getName().replace('.', SEPARATOR) + SEPARATOR + "IOTest" + SEPARATOR + resource);
    }
}