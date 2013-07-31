package com.izforge.izpack.core.variable;

import org.junit.Assert;
import org.junit.Test;

import com.izforge.izpack.util.OsVersion;

public class RegistryValueTest {
	
	@Test
	public void testResolve() throws Exception {
		if( OsVersion.IS_WINDOWS ) {
			String regRoot = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control";
			// CompilerConfig and ConfigurationInstallerListener both check for the existance of regKey - this must be provided
			String regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control";
			String regValue = "CurrentUser";
			Assert.assertEquals("USERNAME", new RegistryValue(null, regKey, regValue).resolve());
			Assert.assertEquals("USERNAME", new RegistryValue(regRoot, regKey, regValue).resolve());
			
			Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(regKey, regKey + "\\CrashControl", "DumpFile").resolve());
			
// This won't work			
//			Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(null, regKey, "CrashControl\\DumpFile").resolve());
			
			regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\DOES_NOT_EXIST";
			regValue = "ImagePath";
			Assert.assertEquals(null, new RegistryValue(null, regKey, regValue).resolve());
		}
	}
}
