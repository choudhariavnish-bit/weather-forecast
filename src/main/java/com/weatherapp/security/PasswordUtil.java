package com.weatherapp.security;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public final class PasswordUtil {
  private static final SecureRandom R = new SecureRandom();
  private PasswordUtil(){}
  public static String salt(){ byte[] b=new byte[16]; R.nextBytes(b); return hex(b); }
  public static String hash(String password,String salt){ try { MessageDigest md=MessageDigest.getInstance("SHA-256"); return hex(md.digest((salt+":"+password).getBytes(StandardCharsets.UTF_8))); } catch(Exception e){throw new IllegalStateException(e);} }
  public static boolean matches(String p,String salt,String expected){ return MessageDigest.isEqual(hash(p,salt).getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8)); }
  private static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}
}
