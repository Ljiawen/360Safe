package com.ithiema.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

	public static String decodeStream(InputStream is) throws IOException {
		ByteArrayOutputStream bao=new ByteArrayOutputStream();
		int len;
		byte[] arr= new byte[1024];
		while((len=is.read(arr))!=-1){
			bao.write(arr,0,len);
		}
		is.close();
		String string = bao.toString();
		bao.close();
		return string;
		
	}

}
