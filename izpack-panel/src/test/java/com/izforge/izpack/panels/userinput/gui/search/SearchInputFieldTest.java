package com.izforge.izpack.panels.userinput.gui.search;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class SearchInputFieldTest {
	
	@Test
	public void testResolveEnvValue() {
//		Map<String, String> env = System.getenv();
		Map<String, String> env = new HashMap<String, String>();
		env.put("JAVA_HOME", "C:\\Program Files\\Java\\jdk1.7.0");
		env.put("PUBLIC", "C:\\Users\\Public");
		
		assertEquals( "C:\\Program Files\\Java\\jdk1.7.0", SearchInputField.resolveEnvValue("%JAVA_HOME%", env) );
		assertEquals( "--C:\\Program Files\\Java\\jdk1.7.0++", SearchInputField.resolveEnvValue("--%JAVA_HOME%++", env) );
		assertEquals( "1;C:\\Program Files\\Java\\jdk1.7.0;C:\\Users\\Public;3", 
					SearchInputField.resolveEnvValue("1;%JAVA_HOME%;%PUBLIC%;3", env) );
	}

}
